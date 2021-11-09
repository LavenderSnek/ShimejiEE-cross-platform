package com.group_finity.mascot;

import com.group_finity.mascot.behavior.Behavior;
import com.group_finity.mascot.environment.Area;
import com.group_finity.mascot.environment.MascotEnvironment;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.image.MascotImage;
import com.group_finity.mascot.image.TranslucentWindow;
import com.group_finity.mascot.sound.Sounds;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
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

    protected DebugWindow debugWindow = null;

    private ArrayList<String> affordances = new ArrayList<>(5);

    public Mascot(final String imageSet) {
        this.id = lastId.incrementAndGet();
        this.imageSet = imageSet;

        log.log(Level.INFO, "Created a mascot ({0})", this);

        // Register the mouse handler
        getWindow().asJWindow().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                Mascot.this.mousePressed(e);
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                Mascot.this.mouseReleased(e);
            }
        });

    }

    @Override
    public String toString() {
        return "mascot" + this.id;
    }

    private void mousePressed(final MouseEvent event) {
        // Switch to drag the animation when the mouse is down
        if (getBehavior() != null) {
            try {
                getBehavior().mousePressed(event);
            } catch (final CantBeAliveException e) {
                log.log(Level.SEVERE, "Fatal Error", e);
                Main.showError(Main.getInstance().getLanguageBundle().getString("SevereShimejiErrorErrorMessage")
                        + "\n" + e.getMessage() + "\n"
                        + Main.getInstance().getLanguageBundle().getString("SeeLogForDetails"));
                dispose();
            }
        }

    }

    private void mouseReleased(final MouseEvent event) {

        if (SwingUtilities.isRightMouseButton(event)) {
            SwingUtilities.invokeLater(() -> showPopup(event.getX(), event.getY()));
        } else {
            if (getBehavior() != null) {
                try {
                    getBehavior().mouseReleased(event);
                } catch (final CantBeAliveException e) {
                    log.log(Level.SEVERE, "Fatal Error", e);
                    Main.showError(Main.getInstance().getLanguageBundle().getString("SevereShimejiErrorErrorMessage") + "\n" + e.getMessage() + "\n" + Main.getInstance().getLanguageBundle().getString("SeeLogForDetails"));
                    dispose();
                }
            }
        }

    }

    private void showPopup(final int x, final int y) {
        final JPopupMenu popup = new JPopupMenu();
        popup.setLightWeightPopupEnabled(false);

        popup.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuCanceled(final PopupMenuEvent e) {}

            @Override
            public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
                setAnimating(true);
            }

            @Override
            public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
                setAnimating(false);
            }
        });

        // Add menu item
        final JMenuItem increaseMenu = new JMenuItem(Main.getInstance().getLanguageBundle().getString("CallAnother"));
        increaseMenu.addActionListener(event -> Main.getInstance().createMascot(getImageSet()));

        // Dismiss menu item
        final JMenuItem disposeMenu = new JMenuItem(Main.getInstance().getLanguageBundle().getString("Dismiss"));
        disposeMenu.addActionListener(e -> dispose());

        // Chase mouse menu item
        final JMenuItem gatherMenu = new JMenuItem(Main.getInstance().getLanguageBundle().getString("FollowCursor"));
        gatherMenu.addActionListener(event -> getManager().setBehaviorAll(Main.getInstance().getConfiguration(getImageSet()), Main.BEHAVIOR_GATHER, getImageSet()));

        // Dismiss others of this image set
        final JMenuItem remainOneSelf = new JMenuItem(Main.getInstance().getLanguageBundle().getString("DismissOthers"));
        remainOneSelf.addActionListener(event -> getManager().remainOne(getImageSet()));

        // Dismiss all others
        final JMenuItem remainOneAll = new JMenuItem(Main.getInstance().getLanguageBundle().getString("DismissAllOthers"));
        remainOneAll.addActionListener(event -> getManager().remainOne(this));

        // Restore IE! menu item
        final JMenuItem restoreMenu = new JMenuItem(Main.getInstance().getLanguageBundle().getString("RestoreWindows"));
        restoreMenu.addActionListener(event -> NativeFactory.getInstance().getEnvironment().restoreIE());

        // Debug menu item
        final JMenuItem debugMenu = new JMenuItem(Main.getInstance().getLanguageBundle().getString("RevealStatistics"));
        debugMenu.addActionListener(event -> {
            if (debugWindow == null) {
                debugWindow = new DebugWindow();
            }
            debugWindow.setVisible(true);
        });

        // Quit menu item
        final JMenuItem closeMenu = new JMenuItem(Main.getInstance().getLanguageBundle().getString("DismissAll"));
        closeMenu.addActionListener(e -> Main.getInstance().exit());

        // Add the Behaviors submenu.  Currently slightly buggy, sometimes the menu ghosts.
        com.group_finity.mascot.menu.JLongMenu submenu = new com.group_finity.mascot.menu.JLongMenu(Main.getInstance().getLanguageBundle().getString("SetBehaviour"), 30);
        // The MenuScroller would look better than the JLongMenu, but the initial positioning is not working correctly.
        // MenuScroller.setScrollerFor(submenu, 30, 125);
        submenu.setAutoscrolls(true);
        JMenuItem item;
        com.group_finity.mascot.config.Configuration config = Main.getInstance().getConfiguration(getImageSet());
        Behavior behaviour;
        for (String behaviorName : config.getBehaviorNames()) {

            final String command = behaviorName;
            try {
                behaviour = Main.getInstance().getConfiguration(getImageSet()).buildBehavior(command);
                if (!behaviour.isHidden()) {
                    item = new JMenuItem(Main.getInstance().getLanguageBundle().containsKey(behaviorName) ?
                            Main.getInstance().getLanguageBundle().getString(behaviorName) :
                            behaviorName.replaceAll("([a-z])(IE)?([A-Z])", "$1 $2 $3").replaceAll("\\s+", " "));

                    item.addActionListener(e -> {
                        try {
                            setBehavior(Main.getInstance().getConfiguration(getImageSet()).buildBehavior(command));
                        } catch (Exception err) {
                            log.log(Level.SEVERE, "Error ({0})", e);
                            Main.showError(Main.getInstance().getLanguageBundle().getString("CouldNotSetBehaviourErrorMessage") + "\n"
                                    + err.getMessage() + "\n" + Main.getInstance().getLanguageBundle().getString("SeeLogForDetails"));
                        }
                    });
                    submenu.add(item);
                }
            } catch (Exception ignored) {
                // just skip if something goes wrong
            }
        }

        popup.add(increaseMenu);
        popup.add(new JSeparator());
        popup.add(gatherMenu);
        popup.add(restoreMenu);
        popup.add(debugMenu);
        popup.add(new JSeparator());
        popup.add(submenu);
        popup.add(new JSeparator());
        popup.add(disposeMenu);
        popup.add(remainOneSelf);
        popup.add(remainOneAll);
        popup.add(closeMenu);

        getWindow().asJWindow().requestFocus();

        popup.show(getWindow().asJWindow(), x, y);
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
                debugWindow.setBehaviour(behavior.toString().substring(9, behavior.toString().length() - 1)
                        .replaceAll("([a-z])(IE)?([A-Z])", "$1 $2 $3").replaceAll("\\s+", " "));

                debugWindow.setShimejiX(anchor.x);
                debugWindow.setShimejiY(anchor.y);

                Area activeWindow = environment.getActiveIE();
                debugWindow.setWindowTitle(environment.getActiveIETitle());
                debugWindow.setWindowX(activeWindow.getLeft());
                debugWindow.setWindowY(activeWindow.getTop());
                debugWindow.setWindowWidth(activeWindow.getWidth());
                debugWindow.setWindowHeight(activeWindow.getHeight());

                Area workArea = environment.getWorkArea();
                debugWindow.setEnvironmentX(workArea.getLeft());
                debugWindow.setEnvironmentY(workArea.getTop());
                debugWindow.setEnvironmentWidth(workArea.getWidth());
                debugWindow.setEnvironmentHeight(workArea.getHeight());
            }
        }
    }

    public void apply() {
        if (isAnimating()) {
            // Make sure there's an image
            if (getImage() != null) {
                // Set the window region
                getWindow().asJWindow().setBounds(getBounds());

                // Set Images
                getWindow().setImage(getImage().getImage());

                // Display
                if (!getWindow().asJWindow().isVisible()) {
                    getWindow().asJWindow().setVisible(true);
                }

                // Redraw
                getWindow().updateImage();
            } else {
                if (getWindow().asJWindow().isVisible()) {
                    getWindow().asJWindow().setVisible(false);
                }
            }

            // play sound if requested
            if (sound != null && !Sounds.getSound(sound).isRunning() && !Sounds.isMuted()) {
                Sounds.getSound(sound).setMicrosecondPosition(0);
                Sounds.getSound(sound).start();
            }
        }
    }

    public void dispose() {
        log.log(Level.INFO, "destroy mascot ({0})", this);

        if (debugWindow != null) {
            debugWindow.setVisible(false);
            debugWindow = null;
        }

        setAnimating(false);
        getWindow().asJWindow().dispose();
        if (getManager() != null) {
            getManager().remove(Mascot.this);
        }
    }

    public Rectangle getBounds() {
        if (getImage() != null) {
            // Central area of the window find the image coordinates and ground coordinates. The centre has already been adjusted for scaling
            final int top = getAnchor().y - getImage().getCenter().y;
            final int left = getAnchor().x - getImage().getCenter().x;

            final int scaling = Integer.parseInt(Main.getInstance().getProperties().getProperty("Scaling", "1"));

            return new Rectangle(left, top, getImage().getSize().width * scaling, getImage().getSize().height * scaling);
        } else {
            // as we have no image let's return what we were last frame
            return getWindow().asJWindow().getBounds();
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
