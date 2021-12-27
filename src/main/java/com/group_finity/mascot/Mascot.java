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
public class Mascot {

    private static final Logger log = Logger.getLogger(Mascot.class.getName());

    private static AtomicInteger lastId = new AtomicInteger();

    private final int id;

    private String imageSet;

    private final TranslucentWindow window = NativeFactory.getInstance().newTransparentWindow();

    private Manager manager = null;

    private Point anchor = new Point(0, 0);

    private MascotImage image = null;

    private boolean lookRight = false;

    private Behavior behavior = null;

    private int time = 0;

    private boolean animating = true;

    private MascotEnvironment environment = new MascotEnvironment(this);

    private String sound = null;

    protected DebugUi debugWindow = null;

    private ArrayList<String> affordances = new ArrayList<>(5);

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
                Main.showError(Main.getInstance().getLanguageBundle().getString("SevereShimejiErrorErrorMessage")
                        + "\n" + e.getMessage() + "\n"
                        + Main.getInstance().getLanguageBundle().getString("SeeLogForDetails"));
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
                Main.showError(Main.getInstance().getLanguageBundle().getString("SevereShimejiErrorErrorMessage") + "\n" + e.getMessage() + "\n" + Main.getInstance().getLanguageBundle().getString("SeeLogForDetails"));
                dispose();
            }
        }
    }

    private TopLevelMenuRep createPopupRep() {
        var main = Main.getInstance();
        var i18n = main.getLanguageBundle();
        var config = Main.getInstance().getConfiguration(getImageSet());
        boolean translateNames = Boolean.parseBoolean(main.getProperties().getProperty("TranslateBehaviorNames", "false"));

        List<MenuItemRep> behaviorItems = new ArrayList<MenuItemRep>();
        Behavior behaviour;
        for (String behaviorName : config.getBehaviorNames()) {
            String lblName = behaviorName;
            if (translateNames && main.getBehaviorNamesBundle().containsKey(behaviorName)) {
                lblName = main.getBehaviorNamesBundle().getString(behaviorName);
            }
            try {
                behaviour = config.buildBehavior(behaviorName);
                if (!behaviour.isHidden()) {
                    behaviorItems.add(new MenuItemRep(lblName, () -> {
                        try {
                            setBehavior(config.buildBehavior(behaviorName));
                        } catch (Exception err) {
                            log.log(Level.SEVERE, "Error ({0})");
                            Main.showError(i18n.getString("CouldNotSetBehaviourErrorMessage")
                                    + "\n" + err.getMessage()
                                    + "\n" + Main.getInstance().getLanguageBundle().getString("SeeLogForDetails"));
                        }
                    }));
                }
            } catch (Exception ignored) {
                behaviorItems.add(new MenuItemRep(lblName, null, false));
            }
        }

        TopLevelMenuRep mainMenu = new TopLevelMenuRep("Shimeji Popup",
                new MenuItemRep(i18n.getString("CallAnother"), () -> main.createMascot(getImageSet())),
                MenuItemRep.SEPARATOR,
                new MenuItemRep(i18n.getString("FollowCursor"), () ->
                        getManager().setBehaviorAll(main.getConfiguration(getImageSet()), Main.BEHAVIOR_GATHER, getImageSet())
                ),
                new MenuItemRep(i18n.getString("RestoreWindows"), () -> getEnvironment().restoreIE()),
                new MenuItemRep(i18n.getString("RevealStatistics"), () -> {
                        if (debugWindow == null) {debugWindow = new DebugWindow();}
                        debugWindow.setVisible(true);
                }),
                MenuItemRep.SEPARATOR,
                new MenuRep(i18n.getString("SetBehaviour"), behaviorItems.toArray(new MenuItemRep[0])),
                MenuItemRep.SEPARATOR,
                new MenuItemRep(i18n.getString("Dismiss"), this::dispose),
                new MenuItemRep(i18n.getString("DismissOthers"), () -> getManager().remainOne(getImageSet())),
                new MenuItemRep(i18n.getString("DismissAllOthers"), () -> getManager().remainOne(this)),
                new MenuItemRep(i18n.getString("DismissAll"), main::exit)
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
                    Main.showError(Main.getInstance().getLanguageBundle().getString("CouldNotGetNextBehaviourErrorMessage") + "\n" + e.getMessage() + "\n" + Main.getInstance().getLanguageBundle().getString("SeeLogForDetails"));
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
            // Make sure there's an image
            if (getImage() != null) {
                // Set the window region
                getWindow().setBounds(getBounds());

                // Set Images
                getWindow().setImage(getImage().getImage());

                // Display
                if (!getWindow().isVisible()) {
                    getWindow().setVisible(true);
                }

                // Redraw
                getWindow().updateImage();
            } else {
                if (getWindow().isVisible()) {
                    getWindow().setVisible(false);
                }
            }

            // play sound if requested
            if (sound != null && !Sounds.isMuted()) {
                Clip clip = Sounds.getSound(sound);
                if (clip != null && !clip.isRunning()) {
                    clip.setMicrosecondPosition(0);
                    clip.start();
                }
            }
        }
    }

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

    public void setBehavior(final Behavior behavior) throws CantBeAliveException {
        this.behavior = behavior;
        this.behavior.init(this);
    }

    public Behavior getBehavior() {
        return this.behavior;
    }

    public Manager getManager() {
        return this.manager;
    }

    public void setManager(final Manager manager) {
        this.manager = manager;
    }

    /**
     * The number of mascots of the same image set being controlled by
     * the same manager
     * */
    public int getCount() {
        return getManager().getCount(imageSet);
    }

    /**
     * The total number of mascots being controlled by the same manager
     * */
    public int getTotalCount() {
        return getManager().getCount();
    }

    /**
     * The window that displays the mascot.
     * <p>
     * do not to use this in actions/scripts. AWT/Swing breaks in many creative ways,
     */
    private TranslucentWindow getWindow() {
        return this.window;
    }

    /**
     * Represents the screen environment of the mascot. Includes thing like screen size and interactive windows.
     * */
    public MascotEnvironment getEnvironment() {
        return environment;
    }

    public ArrayList<String> getAffordances() {
        return affordances;
    }

    /**
     * The location of the mascot on the screen. 0,0 is top left.
     * <p>
     * For xml scripting: The mascot itself can be moved by using the methods in {@link Point} to manipulate location.
     * <p>
     * Where the point is within the mascot is the determined by the {@code ImageAnchor} property of the current image.
     * This image anchor is generally placed where the mascot touches the environment.
     * For example the default standing frame has it placed at bottom middle (64,128) where the feet are.
     */
    public Point getAnchor() {
        return this.anchor;
    }

    /**
     * Can't be used from the xml because it's not possible to create a point object from the xml.
     * <p>
     * To move the mascot, use {@link #getAnchor()} along with the methods in {@link Point} to manipulate location.
     * */
    public void setAnchor(Point anchor) {
        this.anchor = anchor;
    }

    public MascotImage getImage() {
        return this.image;
    }

    public void setImage(final MascotImage image) {
        this.image = image;
    }

    /**
     * Whether the mascot is looking right or left.
     * <p>
     * Note that the default image is treated as facing left.
     */
    public boolean isLookRight() {
        return this.lookRight;
    }

    /**
     * Sets the direction the mascot is facing.
     * <p>
     * When this is set to true the {@code ImageRight} image will be used if present.
     * If not then the flipped version of the default image is used
     *
     * @see com.group_finity.mascot.action.Look
     * */
    public void setLookRight(final boolean lookRight) {
        this.lookRight = lookRight;
    }

    /**
     * Counter that increases with each frame.
     * */
    public int getTime() {
        return this.time;
    }

    private void setTime(final int time) {
        this.time = time;
    }

    private boolean isAnimating() {
        return this.animating;
    }

    private void setAnimating(final boolean animating) {
        this.animating = animating;
    }

    /**
     * The name of the image set this mascot belongs to
     * */
    public String getImageSet() {
        return imageSet;
    }

    /**
     * Do not use directly in scripts as it can have unpredictable results when sharing imageSets
     * <p>
     * There is no guarantee that the new imageSet is loaded even when it is present. To covert to a different
     * imageSet properly, use the {@link com.group_finity.mascot.action.Transform Transform} action.
     * */
    public void setImageSet(final String set) {
        imageSet = set;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(final String name) {
        sound = name;
    }

}
