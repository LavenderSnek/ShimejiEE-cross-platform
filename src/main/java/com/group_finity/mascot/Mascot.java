package com.group_finity.mascot;

import com.group_finity.mascot.behavior.Behavior;
import com.group_finity.mascot.environment.MascotEnvironment;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.image.MascotImage;
import com.group_finity.mascot.image.TranslucentWindow;
import com.group_finity.mascot.ui.contextmenu.MenuItemRep;
import com.group_finity.mascot.ui.contextmenu.MenuRep;
import com.group_finity.mascot.ui.contextmenu.TopLevelMenuRep;
import com.group_finity.mascot.sound.Sounds;
import com.group_finity.mascot.ui.debug.DebugUi;
import com.group_finity.mascot.ui.debug.DebugWindow;

import javax.sound.sampled.Clip;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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

    private final int id;

    private final TranslucentWindow window = NativeFactory.getInstance().newTransparentWindow();

    private int time = 0;
    private boolean animating = true;

    private Manager manager = null;
    private Behavior behavior = null;
    private final Collection<String> affordances = new HashSet<>(5);

    private final MascotEnvironment environment = new MascotEnvironment(this);

    private String imageSet;

    private String sound = null;
    private MascotImage image = null;
    private Point anchor = new Point(0, 0);
    private boolean lookRight = false;

    protected DebugUi debugWindow = null;

    public Mascot(final String imageSet) {
        this.id = lastId.incrementAndGet();
        this.imageSet = imageSet;

        getWindow().setLeftMousePressedAction(this::leftMousePressed);
        getWindow().setLeftMouseReleasedAction(this::leftMouseReleased);
        getWindow().setPopupMenuSupplier(this::createPopupRep);

        log.log(Level.INFO, "Created a mascot ({0})", this);
    }

    @Override
    public String toString() {
        return "mascot" + this.id;
    }

    private void leftMousePressed() {
        // Switch to drag the animation when the mouse is down
        if (getBehavior() != null) {
            try {
                getBehavior().mousePressed(null);
            } catch (final CantBeAliveException e) {
                log.log(Level.SEVERE, "Fatal Error", e);
                Main.showError(Tr.tr("SevereShimejiErrorErrorMessage")
                        + "\n" + e.getMessage()
                        + "\n" + Tr.tr("SeeLogForDetails"));
                dispose();
            }
        }
    }

    private void leftMouseReleased() {
        if (getBehavior() != null) {
            try {
                getBehavior().mouseReleased(null);
            } catch (final CantBeAliveException e) {
                log.log(Level.SEVERE, "Fatal Error", e);
                Main.showError(Tr.tr("SevereShimejiErrorErrorMessage")
                        + "\n" + e.getMessage()
                        + "\n" + Tr.tr("SeeLogForDetails"));
                dispose();
            }
        }
    }

    private TopLevelMenuRep createPopupRep() {
        var config = Main.getInstance().getConfiguration(getImageSet());
        boolean translateNames = Main.getInstance().shouldTranslateBehaviorNames();

        List<MenuItemRep> behaviorItems = new ArrayList<>();
        Behavior behaviour;
        for (String behaviorName : config.getBehaviorNames()) {
            String lblName = translateNames ? Tr.trBv(behaviorName) : behaviorName;
            try {
                behaviour = config.buildBehavior(behaviorName);
                if (!behaviour.isHidden()) {
                    behaviorItems.add(new MenuItemRep(lblName, () -> {
                        try {
                            setBehavior(config.buildBehavior(behaviorName));
                        } catch (Exception err) {
                            log.log(Level.SEVERE, "Error ({0})");
                            Main.showError(Tr.tr("CouldNotSetBehaviourErrorMessage")
                                    + "\n" + err.getMessage()
                                    + "\n" + Tr.tr("SeeLogForDetails"));
                        }
                    }));
                }
            } catch (Exception ignored) {
                behaviorItems.add(new MenuItemRep(lblName, null, false));
            }
        }

        TopLevelMenuRep mainMenu = new TopLevelMenuRep("Shimeji Popup",
                new MenuItemRep(Tr.tr("CallAnother"), () -> Main.getInstance().createMascot(getImageSet())),
                MenuItemRep.SEPARATOR,
                new MenuItemRep(Tr.tr("FollowCursor"), () ->
                        getManager().setBehaviorAll(config, Main.BEHAVIOR_GATHER, getImageSet())
                ),
                new MenuItemRep(Tr.tr("RestoreWindows"), () -> NativeFactory.getInstance().getEnvironment().restoreIE()),
                new MenuItemRep(Tr.tr("RevealStatistics"), () -> {
                    if (debugWindow == null) {
                        debugWindow = new DebugWindow();
                    }
                    debugWindow.setVisible(true);
                }),
                MenuItemRep.SEPARATOR,
                new MenuRep(Tr.tr("SetBehaviour"), behaviorItems.toArray(new MenuItemRep[0])),
                MenuItemRep.SEPARATOR,
                new MenuItemRep(Tr.tr("Dismiss"), this::dispose),
                new MenuItemRep(Tr.tr("DismissOthers"), () -> getManager().remainOne(getImageSet())),
                new MenuItemRep(Tr.tr("DismissAllOthers"), () -> getManager().remainOne(this)),
                new MenuItemRep(Tr.tr("DismissAll"), () -> System.exit(0))
        );

        mainMenu.setOnOpenAction(() -> this.setAnimating(false));
        mainMenu.setOnCloseAction(() -> this.setAnimating(true));

        return mainMenu;
    }

    void tick() {
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

            if (debugWindow != null) {
                debugWindow.setBehaviorName(behavior.toString().substring(9, behavior.toString().length() - 1));
                debugWindow.setMascotAnchor(getAnchor());
                debugWindow.setMascotEnvironment(getEnvironment());
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
            if (sound != null && Main.getInstance().isSoundAllowed()) {
                Clip clip = Sounds.getSound(sound);
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

        if (debugWindow != null) {
            debugWindow.setVisible(false);
            debugWindow.dispose();
            debugWindow = null;
        }

        setAnimating(false);
        getWindow().dispose();
        if (getManager() != null) {
            getManager().remove(Mascot.this);
        }
    }

    /**
     * Calculates the new bounds based on the current state of the mascot.
     */
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
    public Manager getManager() {
        return this.manager;
    }

    public void setManager(final Manager manager) {
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

}
