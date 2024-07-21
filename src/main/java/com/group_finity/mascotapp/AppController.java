package com.group_finity.mascotapp;

import com.group_finity.mascot.*;
import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.config.DefaultPoseLoader;
import com.group_finity.mascot.config.Entry;
import com.group_finity.mascot.exception.ConfigurationException;
import com.group_finity.mascot.image.ImagePairLoaderBuilder;
import com.group_finity.mascot.imageset.ImageSet;
import com.group_finity.mascot.imageset.ShimejiProgramFolder;
import com.group_finity.mascot.imageset.ShimejiImageSet;
import com.group_finity.mascot.manager.DefaultManager;
import com.group_finity.mascot.sound.SoundLoader;
import com.group_finity.mascotapp.gui.chooser.ImageSetChooserUtils;
import com.group_finity.mascotapp.imageset.ImageSetManager;
import com.group_finity.mascotapp.imageset.ImageSetSelectionDelegate;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.AWTException;
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
 *
 * @author see readme
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

        Path actionsPath = pf.getActionConfPath(name);
        Path behaviorPath = pf.getBehaviorConfPath(name);

        SoundLoader soundLoader = new SoundLoader(pf, name);
        soundLoader.setFixRelativeGlobalSound(Boolean.parseBoolean(prefs.getOrDefault("FixRelativeGlobalSound", false + "")));

        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Entry actionsEntry = new Entry(docBuilder.parse(actionsPath.toFile()).getDocumentElement());
        Entry behaviorEntry = new Entry(docBuilder.parse(behaviorPath.toFile()).getDocumentElement());

        Configuration config = new Configuration();
        config.load(new DefaultPoseLoader(imgLoader, soundLoader), actionsEntry, behaviorEntry);
        config.validate();

        return new ShimejiImageSet(config, imgLoader, soundLoader);
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
        final Mascot mascot = new Mascot(imageSet, this, imageSets);

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
            entry("ReduceToOne", () -> manager.disposeIf(m -> manager.getCount() >= 2)),
            entry("RestoreWindows", () -> NativeFactory.getInstance().getEnvironment().restoreIE()),
            entry("ChooseShimeji", this::showImageSetChooser),
            entry("ReloadMascots", this::reloadImageSets),
            entry("DismissAll", manager::disposeAll),
            entry("Quit", () -> System.exit(0))
    );

    private final Map<String, Consumer<Mascot>> mascotActions = Map.ofEntries(
            entry("CallAnother", m -> createMascot(m.getImageSet())),
            entry("RevealStatistics", Mascot::startDebugUi),
            entry("Dismiss", Mascot::dispose),
            entry("DismissOthers", m -> manager.disposeIf(mascot -> mascot.id != m.id && mascot.getImageSet().equals(m.getImageSet()))),
            entry("DismissAllOthers", m -> manager.disposeIf(mascot -> mascot.id != m.id))
    );

    //----------Tray Icon------------//

    private MenuItem mkActionBtn(String title, String action) {
        final MenuItem btn = new MenuItem(title);
        btn.addActionListener(e -> mainMenuActions.get(action).run());
        return btn;
    }

    private CheckboxMenuItem mkToggle(String text, BooleanSupplier getter, Consumer<Boolean> setter) {
        final var toggleBtn = new CheckboxMenuItem(text, getter.getAsBoolean());
        toggleBtn.addItemListener(e -> {
            setter.accept(!getter.getAsBoolean());
            toggleBtn.setState(getter.getAsBoolean());
        });
        return toggleBtn;
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
        final Menu togglesMenu = new Menu(Tr.tr("AllowedBehaviours"));

        togglesMenu.add(mkToggle(Tr.tr("BreedingCloning"), this::isBreedingAllowed, this::setBreedingAllowed));
        togglesMenu.add(mkToggle(Tr.tr("BreedingTransient"), this::isTransientBreedingAllowed, this::setTransientBreedingAllowed));
        togglesMenu.add(mkToggle(Tr.tr("Transformation"), this::isTransformationAllowed, this::setTransformationAllowed));
        togglesMenu.add(mkToggle(Tr.tr("ThrowingWindows"), this::isIEMovementAllowed, this::setIEMovementAllowed));
        togglesMenu.add(mkToggle(Tr.tr("SoundEffects"), this::isSoundAllowed, this::setSoundAllowed));
        togglesMenu.add(mkToggle(Tr.tr("TranslateBehaviorNames"), this::shouldTranslateBehaviours, this::setShouldTranslateBehaviors));
        togglesMenu.add(mkToggle(Tr.tr("AlwaysShowShimejiChooser"), this::shouldShowChooserAtStart, this::setShouldShowChooserAtStart));
        togglesMenu.add(mkToggle(Tr.tr("IgnoreImagesetProperties"), this::shouldIgnoreImagesetProperties, this::setShouldIgnoreImagesetProperties));

        //----------------------//

        //----Create pop-up menu-----//
        final PopupMenu trayPopup = new PopupMenu();

        trayPopup.add(mkActionBtn(Tr.tr("CallShimeji"), "CallShimeji"));
        trayPopup.add(mkActionBtn(Tr.tr("FollowCursor"), "FollowCursor"));
        trayPopup.add(mkActionBtn(Tr.tr("ReduceToOne"), "ReduceToOne"));
        trayPopup.add(mkActionBtn(Tr.tr("RestoreWindows"), "RestoreWindows"));

        trayPopup.add(new MenuItem("-"));

        trayPopup.add(languageMenu);
        trayPopup.add(scalingMenu);
        trayPopup.add(togglesMenu);

        trayPopup.add(new MenuItem("-"));

        trayPopup.add(mkActionBtn(Tr.tr("ChooseShimeji"), "ChooseShimeji"));
        trayPopup.add(mkActionBtn(Tr.tr("ReloadMascots"), "ReloadMascots"));
        trayPopup.add(mkActionBtn(Tr.tr("DismissAll"), "DismissAll"));
        trayPopup.add(mkActionBtn(Tr.tr("Quit"), "Quit"));

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

        } catch (final AWTException e) {
            log.log(Level.SEVERE, "Failed to create tray menu", e);
            System.exit(1);
        }
    }

}
