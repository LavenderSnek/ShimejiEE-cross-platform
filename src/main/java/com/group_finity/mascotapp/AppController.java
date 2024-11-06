package com.group_finity.mascotapp;

import com.group_finity.mascot.*;
import com.group_finity.mascot.config.XmlConfiguration;
import com.group_finity.mascot.config.DefaultPoseLoader;
import com.group_finity.mascot.exception.ConfigurationException;
import com.group_finity.mascot.image.ImagePairLoaderBuilder;
import com.group_finity.mascot.imageset.ImageSet;
import com.group_finity.mascot.imageset.ShimejiProgramFolder;
import com.group_finity.mascot.imageset.ShimejiImageSet;
import com.group_finity.mascot.manager.DefaultManager;
import com.group_finity.mascot.sound.SoundLoader;
import com.group_finity.mascot.window.contextmenu.MenuItemRep;
import com.group_finity.mascot.window.contextmenu.MenuRep;
import com.group_finity.mascot.window.contextmenu.TopLevelMenuRep;
import com.group_finity.mascotapp.gui.chooser.ImageSetChooserUtils;
import com.group_finity.mascotapp.gui.debug.DebugWindow;
import com.group_finity.mascot.imageset.ImageSetManager;
import com.group_finity.mascot.imageset.ImageSetSelectionDelegate;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static java.util.Map.entry;

/**
 * The main app instance,
 * manages/responds to user actions such as changing settings
 */
public final class AppController implements Runnable, MascotPrefProvider, ImageSetSelectionDelegate {
    private static final Logger log = Logger.getLogger(AppController.class.getName());

    // Action that matches the followCursor action
    static final String BEHAVIOR_GATHER = "ChaseMouse";

    private static final String USER_BEHAVIORNAMES_FILE = "user-behaviornames.properties";

    private static final Path SETTINGS_PATH;

    static {
        System.setProperty("java.util.PropertyResourceBundle.encoding", "UTF-8");

        final var logPropsPath = Constants.JAR_DIR.resolve(Path.of("conf","logging.properties"));
        try (var ins = new FileInputStream(logPropsPath.toFile())) {
            LogManager.getLogManager().readConfiguration(ins);
        } catch (final SecurityException | IOException e) {
            e.printStackTrace();
        }

        String settingsPathProp = System.getProperty(Constants.PREF_PROP_PREFIX + "SettingsPath");
        SETTINGS_PATH = settingsPathProp != null
                ? Path.of(settingsPathProp)
                : Constants.JAR_DIR.resolve(Path.of("conf","settings.properties"));
    }

    //--------//
    private ShimejiProgramFolder programFolder = ShimejiProgramFolder.fromFolder(Constants.JAR_DIR);

    private Locale locale = Locale.ENGLISH;
    private final Map<String, Boolean> userSwitches = new ConcurrentHashMap<>(16, 0.75f, 2);
    private final Map<String, String> imageSetDefaults = new ConcurrentHashMap<>(8, 0.75f, 2);

    private final DefaultManager manager = new DefaultManager();
    private final ImageSetManager imageSets = new ImageSetManager(this::loadImageSet, this);

    private void setLocale(Locale newLocale) {
        if (!locale.equals(newLocale)) {
            locale = newLocale;
            Tr.loadLanguage(newLocale);
            //reload tray icon if it exists
            if (SystemTray.isSupported()) {
                SystemTray.getSystemTray().remove(SystemTray.getSystemTray().getTrayIcons()[0]);
                createTrayIcon();
            }
        }
    }

    @Override public boolean isBreedingAllowed() {return userSwitches.getOrDefault("Breeding", true);}
    private void setBreedingAllowed(boolean allowed) {userSwitches.put("Breeding", allowed);}

    @Override public boolean isTransientBreedingAllowed() {return userSwitches.getOrDefault("Transients", true);}
    private void setTransientBreedingAllowed(boolean allowed) {userSwitches.put("Transients", allowed);}

    @Override public boolean isTransformationAllowed() {return userSwitches.getOrDefault("Transformation", true);}
    private void setTransformationAllowed(boolean allowed) {userSwitches.put("Transformation", allowed);}

    @Override public boolean isIEMovementAllowed() {return userSwitches.getOrDefault("Throwing", true);}
    private void setIEMovementAllowed(boolean allowed) {userSwitches.put("Throwing", allowed);}

    @Override public boolean isSoundAllowed() {return userSwitches.getOrDefault("Sounds", true);}
    private void setSoundAllowed(boolean allowed) {userSwitches.put("Sounds", allowed);}

    //----
    private boolean shouldTranslateBehaviours() {return userSwitches.getOrDefault("TranslateBehaviorNames", true);}
    private void setShouldTranslateBehaviors(boolean b) {userSwitches.put("TranslateBehaviorNames", b);}

    private boolean shouldShowChooserAtStart() {return userSwitches.getOrDefault("AlwaysShowShimejiChooser", false);}
    private void setShouldShowChooserAtStart(boolean b) {userSwitches.put("AlwaysShowShimejiChooser", b);}

    private boolean shouldIgnoreImagesetProperties() {return userSwitches.getOrDefault("IgnoreImagesetProperties", false);}
    private void setShouldIgnoreImagesetProperties(boolean b) {userSwitches.put("IgnoreImagesetProperties", b);}

    //----

    private double getScaling() {return Double.parseDouble(imageSetDefaults.getOrDefault("Scaling", "1"));}
    private void setScaling(double scaling) {
        imageSetDefaults.put("Scaling", scaling + "");
        reloadImageSets();
    }

    //-------------------------------------//
    @Override
    public void run() {
        try {
            // init native (needs to be before everything else)
            final String nativeProp = System.getProperty("com.group_finity.mascotnative", Constants.NATIVE_PKG_DEFAULT);
            NativeFactory.init(nativeProp, Constants.NATIVE_LIB_DIR);
            NativeFactory.getInstance().getEnvironment().init();

            // init settings
            loadAllSettings(SETTINGS_PATH);
            Tr.loadLanguage(locale);
            Tr.setCustomBehaviorTranslations(Prefs.readProps(Constants.JAR_DIR.resolve(Path.of("conf", USER_BEHAVIORNAMES_FILE))));
            Runtime.getRuntime().addShutdownHook(new Thread(() -> writeAllSettings(SETTINGS_PATH)));

            // tray icon (optional)
            SwingUtilities.invokeLater(this::createTrayIcon);

            Collection<String> selections = imageSets.getSelected();

            // get selections (show chooser if needed)
            if (selections.isEmpty() || shouldShowChooserAtStart()) {
                showImageSetChooser();
            }

            // start
            manager.start().get();

        } catch (Exception | Error error) {
            log.log(Level.SEVERE, error.getMessage(), error);
            AppController.showError(error.getMessage());
            System.exit(0);
        }
    }

    private static final JFrame frame = new JFrame();

    static void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    //--------imageSet management---------//

    private void showImageSetChooser() {
        ImageSetChooserUtils.askUserForSelection(imageSets::setSelected, imageSets.getSelected(), programFolder);
    }

    /**
     * Loads resources for the specified image set.
     */
    private ImageSet loadImageSet(final String imageSet) {
        HashMap<String, String> settings = new HashMap<>(imageSetDefaults);

        if (!shouldIgnoreImagesetProperties()) {
            try {
                var imgSetPropsPath = programFolder.imgPath()
                        .resolve(imageSet)
                        .resolve("conf/imageset.properties");

                settings.putAll(Prefs.readProps(imgSetPropsPath));

            } catch (Exception e) {
                log.log(Level.WARNING, "Unable to load image set props", e);
            }
        }

        try {
            return loadImageSetFrom(programFolder, imageSet, settings);
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to load image set", e);
            showError(e.getMessage());
        }
        return  null;
    }

    /**
     * Clears all image set data and reloads all selected image sets
     */
    private void reloadImageSets() {
        var selection = imageSets.getSelected();
        imageSets.setSelected(List.of());
        imageSets.setSelected(selection);
    }

    @Override public void imageSetHasBeenAdded(String name, ImageSet imageSet) { createMascot(name); }
    @Override public void dependencyHasBecomeSelection(String name, ImageSet imageSet) { createMascot(name); }

    @Override
    public void imageSetWillBeRemoved(String name, ImageSet imageSet) {
        manager.disposeIf(m -> m.getImageSet().equals(name));
    }

    @Override
    public void imageSetHasBeenRemoved(String name, ImageSet imageSet) {
        if (imageSet instanceof AutoCloseable ims) {
            try {
                ims.close();
            } catch (Exception e) {
                log.log(Level.WARNING, "Unable to dispose of image set", e);
            }
        }
    }

    private static ImageSet loadImageSetFrom(ShimejiProgramFolder pf, String name, Map<String, String> prefs) throws ParserConfigurationException, IOException, SAXException, ConfigurationException {
        double scale = 1.0;
        try {
            scale = Double.parseDouble(prefs.getOrDefault("Scaling", 1.0 + ""));
            scale = scale > 0.0 ? scale : 1.0;
        } catch (Exception ignored) {}

        var imgLoader = new ImagePairLoaderBuilder()
                .setScaling(scale)
                .setLogicalAnchors(Boolean.parseBoolean(prefs.getOrDefault("LogicalAnchors", false + "")))
                .setAsymmetryNameScheme(Boolean.parseBoolean(prefs.getOrDefault("AsymmetryNameScheme", false + "")))
                .setPixelArtScaling( Boolean.parseBoolean(prefs.getOrDefault("PixelArtScaling", false + "")))
                .buildForBasePath(pf.imgPath().resolve(name));

        SoundLoader soundLoader = new SoundLoader(pf, name);
        soundLoader.setFixRelativeGlobalSound(Boolean.parseBoolean(prefs.getOrDefault("FixRelativeGlobalSound", false + "")));

        Path actionsPath = pf.getActionConfPath(name);
        Path behaviorPath = pf.getBehaviorConfPath(name);

        var poseLoader = new DefaultPoseLoader(imgLoader, soundLoader);
        var conf = XmlConfiguration.loadUsing(poseLoader, actionsPath, behaviorPath);

        return new ShimejiImageSet(conf, imgLoader, soundLoader);
    }

    //----------mascot creation-----------//

    /**
     * Spawns a random mascot
     */
    private void createMascot() {
        var ims = imageSets.getRandomSelection();
        if (ims == null) {
            ImageSetChooserUtils.askUserForSelection(imageSets::setSelected, imageSets.getSelected(), programFolder);
        } else {
            createMascot(ims);
        }
    }

    /**
     * Creates a mascot from the specified imageSet.
     * <p>
     * Fails if the image set has not been loaded.
     */
    private void createMascot(String imageSet) {
        // Create one mascot
        final Mascot mascot = new Mascot(imageSet, this, imageSets, new MascotUiFactory() {
            @Override
            public DebugUi createDebugUiFor(Mascot mascot) {
                return new DebugWindow();
            }

            @Override
            public TopLevelMenuRep createContextMenuFor(Mascot mascot) {
                return createCtxMenuFor(mascot);
            }
        });

        // Create it outside the bounds of the screen
        mascot.setAnchor(new Point(-4000, -4000));

        // Randomize the initial orientation
        mascot.setLookRight(Math.random() < 0.5);

        try {
            mascot.setBehavior(mascot.getOwnImageSet().getConfiguration().buildBehavior(null, mascot));
            manager.add(mascot);
        } catch (Exception e) {
            log.log(Level.SEVERE, imageSet + " fatal error, can not be started.", e);
            AppController.showError(
                    Tr.tr("CouldNotCreateShimejiErrorMessage") + ": " + imageSet +
                    ".\n" + e.getMessage()
                    + "\n" + Tr.tr("SeeLogForDetails"));
            mascot.dispose();
        }
    }

///=======v This class ends here, everything below is meant to be easily deletable v========//

    //---------Setting storage/extraction------------//

    private void loadAllSettings(Path inputFilePath) {
        var props = Prefs.readProps(inputFilePath);

        for (String key : Constants.USER_SWITCH_KEYS) {
            var s = Prefs.getSetting(props, key);
            if (s != null) {
                userSwitches.put(key, Boolean.parseBoolean(s));
            }
        }

        for (String key: Constants.IMGSET_DEFAULTS_KEYS) {
            var s = Prefs.getSetting(props, key);
            if (s != null) {
                imageSetDefaults.put(key, s);
            }
        }

        var localeProp = Prefs.getSetting(props, "Language");
        if (localeProp != null) {
            locale = Locale.forLanguageTag(localeProp);
        }

        programFolder = Prefs.getProgramFolder(programFolder, props);
        imageSets.setSelected(Prefs.getActiveImageSets(programFolder, props));
    }

    private void writeAllSettings(Path outputFilePath) {
        Map<String, String> props = new HashMap<>();

        userSwitches.forEach((k,v) -> props.put(k, v + ""));
        props.putAll(imageSetDefaults);

        if (getScaling() != 1.0) {
            props.put("Scaling", getScaling() + "");
        }
        if (!locale.equals(Locale.getDefault())) {
            props.put("Language", locale.toLanguageTag());
        }

        var ims = Prefs.serializeImageSets(imageSets.getSelected());
        props.put("ActiveShimeji", ims);

        // program folder excluded on purpose since there's not going to be a gui for it

        Prefs.writeProps(props, outputFilePath);
    }

    private final Map<String, Runnable> mainMenuActions = Map.ofEntries(
            entry("CallShimeji", this::createMascot),
            entry("FollowCursor", () -> manager.trySetBehaviorAll(BEHAVIOR_GATHER)),
            entry("ReduceToOne", manager::reduceToOne),
            entry("RestoreWindows", () -> NativeFactory.getInstance().getEnvironment().restoreIE()),
            entry("ChooseShimeji", this::showImageSetChooser),
            entry("ReloadMascots", this::reloadImageSets),
            entry("DismissAll", manager::disposeAll),
            entry("Quit", () -> System.exit(0))
    );

    private final Map<String, Consumer<Mascot>> mascotActions = Map.ofEntries(
            entry("CallAnother", m -> createMascot(m.getImageSet())),
            entry("RevealStatistics", Mascot::startDebugUi),
            entry("FollowCursor", m -> {
                try {
                    var conf = m.getOwnImageSet().getConfiguration();
                    m.setBehavior(conf.buildBehavior(conf.getSchema().getString(BEHAVIOR_GATHER)));
                } catch (Exception ignored) {
                    // again, we're ignoring ChaseMouse not existing
                }
            }),
            entry("Dismiss", Mascot::dispose),
            entry("DismissOthers", m -> manager.disposeIf(mascot -> mascot.id != m.id && mascot.getImageSet().equals(m.getImageSet()))),
            entry("DismissAllOthers", m -> manager.disposeIf(mascot -> mascot.id != m.id))
    );

    //----------Menus------------//

    //---Console Menu

    private MenuItemRep repActionBtn(String title, String action, Mascot m) {
        if (mascotActions.containsKey(action)) {
            return new MenuItemRep(title, () -> mascotActions.get(action).accept(m));
        }
        if (mainMenuActions.containsKey(action)) {
            return new MenuItemRep(title, mainMenuActions.get(action));
        }
        return new MenuItemRep(title, null, false);
    }

    private List<MenuItemRep> createBehaviourMenuItemsFor(Mascot m) {
        var conf = m.getOwnImageSet().getConfiguration();

        List<MenuItemRep> bvItems = new ArrayList<>();

        for (String bvName : conf.getBehaviorNames()) {
            String title = shouldTranslateBehaviours() ? Tr.trBv(bvName) : bvName;
            try {
                var bv = conf.buildBehavior(bvName);
                if (bv.isHidden()) {
                    continue;
                }
                bvItems.add(new MenuItemRep(title, () -> {
                    try {
                        m.setBehavior(conf.buildBehavior(bvName));
                    } catch (Exception err) {
                        showError(Tr.tr("CouldNotSetBehaviourErrorMessage")
                                  + "\n" + err.getMessage());
                    }
                }));
            } catch (Exception e) {
                bvItems.add(new MenuItemRep(title, null, false));
                log.log(Level.WARNING, "Failed to create Behaviour menu button for: " + bvName, e);
            }
        }

        return bvItems;
    }

    private TopLevelMenuRep createCtxMenuFor(Mascot m) {
        var rep = new TopLevelMenuRep("mascot",
                repActionBtn(Tr.tr("CallAnother"), "CallAnother", m),
                repActionBtn(Tr.tr("FollowCursor"), "FollowCursor", m),
                repActionBtn(Tr.tr("RestoreWindows"), "RestoreWindows", m),
                repActionBtn(Tr.tr("RevealStatistics"), "RevealStatistics", m),
                MenuItemRep.SEPARATOR,
                new MenuRep(Tr.tr("SetBehaviour"), createBehaviourMenuItemsFor(m).toArray(new MenuItemRep[0])),
                MenuItemRep.SEPARATOR,
                repActionBtn(Tr.tr("Dismiss"), "Dismiss", m),
                repActionBtn(Tr.tr("DismissOthers"), "DismissOthers", m),
                repActionBtn(Tr.tr("DismissAllOthers"), "DismissAllOthers", m),
                repActionBtn(Tr.tr("DismissAll"), "DismissAll", m)
        );

        rep.setOnOpenAction(() -> m.setAnimating(false));
        rep.setOnCloseAction(() -> m.setAnimating(true));

        return rep;
    }

    //---Tray Menu

    private MenuItem awtActionBtn(String title, String action) {
        final MenuItem btn = new MenuItem(title);
        btn.addActionListener(e -> mainMenuActions.get(action).run());
        return btn;
    }

    private CheckboxMenuItem awtToggle(String text, BooleanSupplier getter, Consumer<Boolean> setter) {
        final var toggleBtn = new CheckboxMenuItem(text, getter.getAsBoolean());
        toggleBtn.addItemListener(e -> {
            setter.accept(!getter.getAsBoolean());
            toggleBtn.setState(getter.getAsBoolean());
        });
        return toggleBtn;
    }

    private CheckboxMenuItem awtImgToggle(String text, String key, boolean dflt) {
        return awtToggle(text,
                () -> Boolean.parseBoolean(imageSetDefaults.getOrDefault(key, dflt + "")),
                (b) -> imageSetDefaults.put(key, b + "")
        );
    }

    private void createTrayIcon() {
        if (!SystemTray.isSupported()) {
            return;
        }

        //--languages submenu
        final Menu languageMenu = new Menu(Tr.tr("Language"));
        for (String[] lang : Constants.LANGUAGE_TABLE) {
            final var langName = lang[0];
            final var locale = Locale.forLanguageTag(lang[1]);
            final var langBtn = new MenuItem(langName);
            langBtn.addActionListener(e -> setLocale(locale));
            languageMenu.add(langBtn);
        }

        //--scaling submenu
        final Menu scalingMenu = new Menu(Tr.tr("Scaling"));
        final double scalingStep = 0.25;
        final int scalesCount = 16;
        for (int i = 1; i <= scalesCount; i++) {
            double opt = i * scalingStep;
            final var scaleBtn = new MenuItem(String.valueOf(opt));

            if (getScaling() == opt) {
                scaleBtn.setEnabled(false);
            }

            int btnIdx = i - 1;
            scaleBtn.addActionListener(e -> {
                for (int j = 0; j < scalingMenu.getItemCount(); j++) {
                    scalingMenu.getItem(j).setEnabled(true);
                }
                setScaling(opt);
                scalingMenu.getItem(btnIdx).setEnabled(false);
            });
            scalingMenu.add(scaleBtn);
        }

        //--behaviour toggles submenu
        final Menu bvTogglesMenu = new Menu(Tr.tr("AllowedBehaviours"));

        bvTogglesMenu.add(awtToggle(Tr.tr("BreedingCloning"), this::isBreedingAllowed, this::setBreedingAllowed));
        bvTogglesMenu.add(awtToggle(Tr.tr("BreedingTransient"), this::isTransientBreedingAllowed, this::setTransientBreedingAllowed));
        bvTogglesMenu.add(awtToggle(Tr.tr("Transformation"), this::isTransformationAllowed, this::setTransformationAllowed));
        bvTogglesMenu.add(awtToggle(Tr.tr("ThrowingWindows"), this::isIEMovementAllowed, this::setIEMovementAllowed));
        bvTogglesMenu.add(awtToggle(Tr.tr("SoundEffects"), this::isSoundAllowed, this::setSoundAllowed));
        bvTogglesMenu.add(awtToggle(Tr.tr("TranslateBehaviorNames"), this::shouldTranslateBehaviours, this::setShouldTranslateBehaviors));
        bvTogglesMenu.add(awtToggle(Tr.tr("AlwaysShowShimejiChooser"), this::shouldShowChooserAtStart, this::setShouldShowChooserAtStart));
        bvTogglesMenu.add(awtToggle(Tr.tr("IgnoreImagesetProperties"), this::shouldIgnoreImagesetProperties, this::setShouldIgnoreImagesetProperties));

        //--image set toggles
        final Menu imgTogglesMenu = new Menu(Tr.tr("ImageSet"));

        var rl = new MenuItem(Tr.tr("NeedsReload"));
        rl.setEnabled(false);
        imgTogglesMenu.add(rl);

        imgTogglesMenu.add("-");

        imgTogglesMenu.add(awtImgToggle(Tr.tr("LogicalAnchors"),"LogicalAnchors", false));
        imgTogglesMenu.add(awtImgToggle(Tr.tr("AsymmetryNameScheme"),"AsymmetryNameScheme", false));
        imgTogglesMenu.add(awtImgToggle(Tr.tr("PixelArtScaling"),"PixelArtScaling", false));
        imgTogglesMenu.add(awtImgToggle(Tr.tr("FixRelativeGlobalSound"),"FixRelativeGlobalSound", false));

        //----------------------//

        //----Create pop-up menu-----//
        final PopupMenu trayPopup = new PopupMenu();

        trayPopup.add(awtActionBtn(Tr.tr("CallShimeji"), "CallShimeji"));
        trayPopup.add(awtActionBtn(Tr.tr("FollowCursor"), "FollowCursor"));
        trayPopup.add(awtActionBtn(Tr.tr("ReduceToOne"), "ReduceToOne"));
        trayPopup.add(awtActionBtn(Tr.tr("RestoreWindows"), "RestoreWindows"));

        trayPopup.add("-");

        trayPopup.add(languageMenu);
        trayPopup.add(scalingMenu);
        trayPopup.add(bvTogglesMenu);
        trayPopup.add(imgTogglesMenu);

        trayPopup.add("-");

        trayPopup.add(awtActionBtn(Tr.tr("ChooseShimeji"), "ChooseShimeji"));
        trayPopup.add(awtActionBtn(Tr.tr("ReloadMascots"), "ReloadMascots"));
        trayPopup.add(awtActionBtn(Tr.tr("DismissAll"), "DismissAll"));
        trayPopup.add(awtActionBtn(Tr.tr("Quit"), "Quit"));

        try {
            //adding the tray icon
            Path trayIconPath = programFolder.getIconPath();
            Image trayIconImg = null;
            try {
                if (trayIconPath != null) {
                    trayIconImg = ImageIO.read(trayIconPath.toFile());
                }
                if (trayIconImg == null) {
                    trayIconImg = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/icon.png")));
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "unable to load tray icon", e);
            }

            if (trayIconImg == null) {
                trayIconImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
            }

            final TrayIcon trayIcon = new TrayIcon(trayIconImg, "ShimejiEE", trayPopup);

            // show tray icon
            SystemTray.getSystemTray().add(trayIcon);

        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to create tray menu", e);
            System.exit(1);
        }
    }

}
