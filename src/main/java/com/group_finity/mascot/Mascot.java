package com.group_finity.mascot;

import com.group_finity.mascot.animation.Hotspot;
import com.group_finity.mascot.behavior.Behavior;
import com.group_finity.mascot.environment.MascotEnvironment;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.image.MascotImage;
import com.group_finity.mascot.imageset.ImageSet;
import com.group_finity.mascot.imageset.ImageSetStore;
import com.group_finity.mascot.manager.MascotManager;
import com.group_finity.mascot.window.TranslucentWindow;
// todo: not ok
import com.group_finity.mascot.window.TranslucentWindowEventHandler;
import com.group_finity.mascotapp.gui.debug.DebugWindow;

import javax.sound.sampled.Clip;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mascot/Shimeji object; This object is inserted into scripts as {@code mascot}
 * <p>
 * The {@link com.group_finity.mascot.action.Action Action} represents the short-term animation of the mascot.
 * When this short-term action ends, {@link Behavior} picks the next action to be executed,
 * forming the long-term pattern of actions.
 */
public class Mascot implements ScriptableMascot {

    private static final Logger log = Logger.getLogger(Mascot.class.getName());

    private static final AtomicInteger lastId = new AtomicInteger();

    public final int id;

    private final TranslucentWindow window = NativeFactory.getInstance().newTransparentWindow();

    private int time = 0;
    private boolean animating = true;

    private MascotManager manager = null;
    private Behavior behavior = null;
    private final Collection<String> affordances = new HashSet<>(1);

    private final MascotEnvironment environment = new MascotEnvironment(this);

    private String imageSet;

    private String sound = null;
    private MascotImage image = null;
    private Point anchor = new Point(0, 0);
    private boolean lookRight = false;

    private final List<Hotspot> hotspots = new ArrayList<>(5);
    private Point hotspotCursor = null;
    private boolean dragging = false;

    private DebugUi debugUi = null;

    private final MascotPrefProvider prefProvider;
    private final ImageSetStore imageSetStore;

    public Mascot(String imageSet, MascotPrefProvider prefProvider, ImageSetStore imageSetStore) {
        this.id = lastId.incrementAndGet();
        this.imageSet = imageSet;

        this.prefProvider = prefProvider;
        this.imageSetStore = imageSetStore;

        MascotEventHandler eventHandler = new MascotEventHandler(this);
        getWindow().setEventHandler(eventHandler);

        log.log(Level.INFO, "Created a mascot ({0})", this);
    }

    public void startDebugUi() {
        if (debugUi == null) {
            // todo: maybe make this a factory
            debugUi = new DebugWindow();
        }
        // slightly messy
        debugUi.setAfterDisposeAction(() -> debugUi = null);
        debugUi.setVisible(true);
    }

    private void stopDebugUi() {
        if (debugUi != null) {
            debugUi.setVisible(false);
            debugUi.dispose();
        }
    }

    /**
     * Updates the internal state of the mascot based on the config (behaviour/actions)
     */
    public void tick() throws CantBeAliveException {
        if (!isAnimating()) {
            return;
        }

        if (getBehavior() != null) {
            getBehavior().next();
            setTime(getTime() + 1);
        }

        if (debugUi != null) {
            debugUi.update(this);
        }
    }

    /**
     * Updates the mascot's representation to reflect its current internal state. Called after tick()
     */
    public void apply() {
        if (!isAnimating()) {
            return;
        }

        if (getImage() != null) {
            // update
            getWindow().setBounds(getBounds());
            getWindow().setImage(getImage().getImage());

            if (!getWindow().isVisible()) {
                getWindow().setVisible(true);
            }

            // repaint
            getWindow().updateImage();

        } else if (getWindow().isVisible()) {
            getWindow().setVisible(false);
        }

        // play sound if requested
        if (getSound() != null && isSoundAllowed()) {
            Clip clip = Objects.requireNonNull(getOwnImageSet())
                    .getSounds()
                    .get(getSound());
            if (clip != null && !clip.isRunning()) {
                clip.setMicrosecondPosition(0);
                clip.start();
            }
        }
    }

    @Override
    public void dispose() {
        log.log(Level.INFO, "destroy mascot ({0})", this);

        stopDebugUi();
        setAnimating(false);
        getWindow().dispose();

        if (getManager() != null) {
            getManager().remove(Mascot.this);
        }
    }

    @Override
    public Rectangle getBounds() {
        // as we have no image let's return what we were last frame
        if (getImage() == null) {
            return getWindow().getBounds();
        }
        // calculate bounds
        final int top = getAnchor().y - getImage().getCenter().y;
        final int left = getAnchor().x - getImage().getCenter().x;
        return new Rectangle(left, top, getImage().getSize().width, getImage().getSize().height);
    }

    private TranslucentWindow getWindow() { return this.window; }

    @Override public int getTime() { return this.time; }
    private void setTime(int time) { this.time = time; }

    public boolean isAnimating() { return this.animating; }
    public void setAnimating(boolean animating) { this.animating = animating; }

    public void setBehavior(final Behavior behavior) throws CantBeAliveException {
        this.behavior = behavior;
        this.behavior.init(this);
    }

    public Behavior getBehavior() {
        return this.behavior;
    }

    @Override public MascotManager getManager() { return this.manager; }
    public void setManager(final MascotManager manager) { this.manager = manager; }

    @Override public Collection<String> getAffordances() { return affordances; }

    @Override public MascotEnvironment getEnvironment() { return environment; }

    @Override public String getImageSet() { return imageSet; }
    @Override public void setImageSet(String set) { imageSet = set; }

    @Override public String getSound() { return sound; }
    public void setSound(String name) { sound = name; }

    public MascotImage getImage() { return this.image; }
    public void setImage(MascotImage image) { this.image = image; }

    @Override public Point getAnchor() { return this.anchor; }
    @Override public void setAnchor(Point anchor) { this.anchor = anchor; }

    @Override public boolean isLookRight() { return this.lookRight; }
    @Override public void setLookRight(boolean lookRight) { this.lookRight = lookRight; }

    @Override public List<Hotspot> getHotspots() { return hotspots; }

    @Override public boolean isDragging() { return dragging; }
    @Override public void setDragging(boolean dragging) { this.dragging = dragging; }

    @Override public Point getCursorPosition() { return hotspotCursor; }
    @Override public void setCursorPosition(Point position) { hotspotCursor = position; }

    //-----

    @Override public String toString() {
        return "mascot" + this.id;
    }

    // this is where all the garbage from main went
    // not part of the API, please don't call/rely on these from scripts, (maybe these should be mangled?)

    public static Mascot createBlankFrom(Mascot mascot) {
        return new Mascot(mascot.imageSet, mascot.prefProvider, mascot.imageSetStore);
    }

    public double getScaling() {
        return getOwnImageSet().getImagePairs().getScaling();
    }

    public ImageSet getOwnImageSet() {
        return imageSetStore.get(getImageSet());
    }

    public ImageSet getImageSetDependency(String name) {
        return imageSetStore.getAsDependency(name, getImageSet());
    }

    public boolean isIEMovementAllowed() { return prefProvider.isIEMovementAllowed(getImageSet()); }
    public boolean isBreedingAllowed() { return prefProvider.isBreedingAllowed(getImageSet()); }
    public boolean isTransientBreedingAllowed() { return prefProvider.isTransientBreedingAllowed(getImageSet()); }
    public boolean isTransformationAllowed() { return prefProvider.isTransformationAllowed(getImageSet()); }
    public boolean isSoundAllowed() { return prefProvider.isSoundAllowed(getImageSet()); }
}
