package com.group_finity.mascot;

import com.group_finity.mascot.animation.Hotspot;
import com.group_finity.mascot.behavior.Behavior;
import com.group_finity.mascot.environment.MascotEnvironment;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.image.MascotImage;
import com.group_finity.mascot.imageset.ImageSet;
import com.group_finity.mascot.ui.debug.DebugUi;
import com.group_finity.mascot.ui.debug.DebugWindow;
import com.group_finity.mascot.window.TranslucentWindow;

import javax.sound.sampled.Clip;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.*;
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
    private final Collection<String> affordances = new HashSet<>(5);

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

    public Mascot(final String imageSet) {
        this.id = lastId.incrementAndGet();
        this.imageSet = imageSet;

        MascotEventHandler eventHandler = new MascotEventHandler(this);
        getWindow().setEventHandler(eventHandler);

        log.log(Level.INFO, "Created a mascot ({0})", this);
    }

    @Override
    public String toString() {
        return "mascot" + this.id;
    }

    void startDebugUi() {
        if (debugUi == null) {
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

    public void tick() {
        if (isAnimating()) {
            if (getBehavior() != null) {
                try {
                    getBehavior().next();
                } catch (final CantBeAliveException e) {
                    log.log(Level.SEVERE, "Fatal Error.", e);
                    Main.showError(Tr.tr("CouldNotGetNextBehaviourErrorMessage")
                            + "\n" + e.getMessage()
                            + "\n" + Tr.tr("SeeLogForDetails"));
                    dispose();
                }

                setTime(getTime() + 1);
            }

            if (debugUi != null) {
                debugUi.update(this);
            }
        }
    }

    public void apply() {
        if (isAnimating()) {
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
            if (getSound() != null && soundAllowed()) {
                Clip clip = Objects.requireNonNull(getOwnImageSet())
                        .getSounds()
                        .get(getSound());
                if (clip != null && !clip.isRunning()) {
                    clip.setMicrosecondPosition(0);
                    clip.start();
                }
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
        if (getImage() != null) {
            final int top = getAnchor().y - getImage().getCenter().y;
            final int left = getAnchor().x - getImage().getCenter().x;

            return new Rectangle(left, top, getImage().getSize().width, getImage().getSize().height);
        } else {
            // as we have no image let's return what we were last frame
            return getWindow().getBounds();
        }
    }

    /**
     * The native window that displays the mascot.
     */
    private TranslucentWindow getWindow() {
        return this.window;
    }

    @Override
    public int getTime() {
        return this.time;
    }

    private void setTime(int time) {
        this.time = time;
    }

    boolean isAnimating() {
        return this.animating;
    }

    void setAnimating(boolean animating) {
        this.animating = animating;
    }

    public void setBehavior(final Behavior behavior) throws CantBeAliveException {
        this.behavior = behavior;
        this.behavior.init(this);
    }

    public Behavior getBehavior() {
        return this.behavior;
    }

    @Override
    public MascotManager getManager() {
        return this.manager;
    }

    public void setManager(final MascotManager manager) {
        this.manager = manager;
    }

    @Override
    public Collection<String> getAffordances() {
        return affordances;
    }

    @Override
    public MascotEnvironment getEnvironment() {
        return environment;
    }

    @Override
    public String getImageSet() {
        return imageSet;
    }

    @Override
    public void setImageSet(String set) {
        imageSet = set;
    }

    @Override
    public String getSound() {
        return sound;
    }

    public void setSound(String name) {
        sound = name;
    }

    public MascotImage getImage() {
        return this.image;
    }

    public void setImage(MascotImage image) {
        this.image = image;
    }

    @Override
    public Point getAnchor() {
        return this.anchor;
    }

    @Override
    public void setAnchor(Point anchor) {
        this.anchor = anchor;
    }

    @Override
    public boolean isLookRight() {
        return this.lookRight;
    }

    @Override
    public void setLookRight(boolean lookRight) {
        this.lookRight = lookRight;
    }

    @Override
    public List<Hotspot> getHotspots() {
        return hotspots;
    }

    @Override
    public boolean isDragging() {
        return dragging;
    }

    @Override
    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    @Override
    public Point getCursorPosition() {
        return hotspotCursor;
    }

    @Override
    public void setCursorPosition(Point position) {
        hotspotCursor = position;
    }


    // not part of the API, please don't call these from scripts
    // and this is bad- but it really is more of a stepping stone to de-Main-ify this

    public double getScaling() {
        return Objects.requireNonNull(getOwnImageSet())
                .getImagePairs()
                .getScaling();
    }

    public boolean soundAllowed() {
        return Main.getInstance().isSoundAllowed();
    }

    public ImageSet getOwnImageSet() {
        return Main.getInstance().getImageSet(getImageSet());
    }

}
