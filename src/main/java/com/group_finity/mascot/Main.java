package com.group_finity.mascot;

import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.exception.ConfigurationException;
import com.group_finity.mascot.imageset.ImageSet;
import com.group_finity.mascot.imageset.ShimejiImageSet;
import com.group_finity.mascot.imageset.ShimejiProgramFolder;
import com.group_finity.mascot.ui.imagesets.ImageSetChooserUtils;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The main app instance,
 * manages/responds to user actions such as changing settings
 *
 * @author see readme
 */
public final class Main {
    private static final Logger log = Logger.getLogger(Main.class.getName());

    // Action that matches the followCursor action
    static final String BEHAVIOR_GATHER = "ChaseMouse";

    private static final String SP_PREFIX = "com.group_finity.mascot.prefs.";

    public static final Path JAR_PARENT_DIR;

    static {

        System.setProperty("java.util.PropertyResourceBundle.encoding", "UTF-8");

        Path folder = null;
        try {
            folder = Path.of(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        } catch (Exception e) {
            showError("Unable to find path to jar");
            System.exit(1);
        }
        JAR_PARENT_DIR = folder;

        try (var ins = new FileInputStream(JAR_PARENT_DIR.resolve(Path.of("conf","logging.properties")).toFile())) {
            LogManager.getLogManager().readConfiguration(ins);
        } catch (final SecurityException | IOException e) {
            e.printStackTrace();
        }

    }

    //--------//

    private ShimejiProgramFolder programFolder = ShimejiProgramFolder.fromFolder(JAR_PARENT_DIR);

    private final ConcurrentMap<String, ImageSet> loadedImageSets = new ConcurrentHashMap<>();
    private final List<String> activeImageSets = new ArrayList<>();

    private Locale locale = Locale.ENGLISH;
    private final Map<String, Boolean> userSwitches = new ConcurrentHashMap<>(16, 0.75f, 2);
    private final Map<String, String> imageSetDefaults = new ConcurrentHashMap<>(8, 0.75f, 2);

    private final Manager manager = new Manager();
    private static final Main instance = new Main();

    private Main() {
    }

    //------------Getters/Setters-------------//

    public static Main getInstance() {
        return instance;
    }

    private Manager getManager() {
        return this.manager;
    }

    public ShimejiProgramFolder getProgramFolder() {
        return programFolder;
    }

    public ImageSet getImageSet(String name) {
        if (loadedImageSets.containsKey(name)) {
            return loadedImageSets.get(name);
        }
        if (!Files.isDirectory(getProgramFolder().imgPath().resolve(name))) {
            return null;
        }
        // sort of deals with dependencies + allows dynamic loading with scripts
        try {
            loadImageSet(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return loadedImageSets.getOrDefault(name, null);
    }

    public Configuration getConfiguration(String imageSet) {
        if (imageSet == null) {
            return null;
        }
        var imgSet = getImageSet(imageSet);
        return imgSet == null ? null : imgSet.getConfiguration();
    }

    public Locale getLocale() {return locale;}
    private void setLocale(Locale locale) {
        if (!getLocale().equals(locale)) {
            this.locale = locale;
            Tr.loadLanguage();
            //reload tray icon if it exists
            if (SystemTray.isSupported()) {
                SystemTray.getSystemTray().remove(SystemTray.getSystemTray().getTrayIcons()[0]);
                createTrayIcon();
            }
        }
    }


    //--- Bool settings
    private final String[] USER_SWITCH_KEYS = {
            "Breeding",
            "Transients",
            "Transformation",
            "Throwing",
            "Sounds",
            "AlwaysShowShimejiChooser",
            "TranslateBehaviorNames"
    };

    public boolean isBreedingAllowed() {return userSwitches.getOrDefault("Breeding", true);}
    private void setBreedingAllowed(boolean allowed) {userSwitches.put("Breeding", allowed);}

    public boolean isTransientBreedingAllowed() {return userSwitches.getOrDefault("Transients", true);}
    private void setTransientBreedingAllowed(boolean allowed) {userSwitches.put("Transients", allowed);}

    public boolean isTransformationAllowed() {return userSwitches.getOrDefault("Transformation", true);}
    private void setTransformationAllowed(boolean allowed) {userSwitches.put("Transformation", allowed);}

    public boolean isIEMovementAllowed() {return userSwitches.getOrDefault("Throwing", true);}
    private void setIEMovementAllowed(boolean allowed) {userSwitches.put("Throwing", allowed);}

    public boolean isSoundAllowed() {return userSwitches.getOrDefault("Sounds", true);}
    private void setSoundAllowed(boolean allowed) {userSwitches.put("Sounds", allowed);}

    private boolean shouldShowChooserAtStart() {return userSwitches.getOrDefault("AlwaysShowShimejiChooser", false);}
    private void setShouldShowChooserAtStart(boolean b) {userSwitches.put("AlwaysShowShimejiChooser", b);}

    public boolean shouldTranslateBehaviorNames() {return userSwitches.getOrDefault("TranslateBehaviorNames", false);}
    private void setShouldTranslateBehaviorNames(boolean b) {userSwitches.put("TranslateBehaviorNames", b);}

    private boolean shouldIgnoreImagesetProperties() {return userSwitches.getOrDefault("IgnoreImagesetProperties", false);}
    private void setShouldIgnoreImagesetProperties(boolean b) {userSwitches.put("IgnoreImagesetProperties", b);}

    //--- image set settings

    private final String[] IMGSET_DEFAULTS_KEYS = {
            "Scaling",
            "LogicalAnchors",
            "AsymmetryNameScheme",
            "PixelArtScaling",
            "FixRelativeGlobalSound"
    };

    private double getScaling() {return Double.parseDouble(imageSetDefaults.getOrDefault("Scaling", "1"));}
    private void setScaling(double scaling) {
        imageSetDefaults.put("Scaling", scaling + "");}

    //-------------------------------------//

    /**
     * Program entry point
     */
    public static void main(final String[] args) {
        try {
            getInstance().run();
        } catch (OutOfMemoryError error) {
            log.log(Level.SEVERE, "Out of Memory.", error);
            Main.showError("Out of Memory.");
            System.exit(0);
        }
    }

    public void run() {
        String settingsPathProp = System.getProperty(SP_PREFIX + "SettingsPath");
        final Path SETTINGS_PATH = settingsPathProp != null
                ? Path.of(settingsPathProp)
                : JAR_PARENT_DIR.resolve(Path.of("conf","settings.properties"));

        loadAllSettings(SETTINGS_PATH);
        Tr.loadLanguage();

        // optional
        createTrayIcon();

        //because the chooser is async
        boolean isExit = getManager().isExitOnLastRemoved();
        getManager().setExitOnLastRemoved(false);

        Set<String> selections = getActiveImageSets();

        if (selections.isEmpty() || shouldShowChooserAtStart()) {
            ImageSetChooserUtils.askUserForSelection(c -> {
                setActiveImageSets(c);
                getManager().setExitOnLastRemoved(isExit);
            }, getActiveImageSets());
        } else {
            selections.forEach(this::addActiveImageSet);
            getManager().setExitOnLastRemoved(isExit);
        }

        getManager().start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> writeAllSettings(SETTINGS_PATH)));
    }

    private static final JFrame frame = new JFrame();
    public static void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    //--------imageSet management---------//

    /**
     * Loads resources for the specified image set.
     */
    private void loadImageSet(final String imageSet) throws ConfigurationException, ParserConfigurationException, IOException, SAXException {
        HashMap<String, String> settings = new HashMap<>(imageSetDefaults);

        if (!shouldIgnoreImagesetProperties()) {
            try {
                var imgSetPropsPath = getProgramFolder().imgPath()
                        .resolve(imageSet).resolve("conf").resolve("imageset.properties");
                settings.putAll(loadProperties(imgSetPropsPath));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        var imgSet = ShimejiImageSet.loadFrom(getProgramFolder(), imageSet, settings);
        loadedImageSets.put(imageSet, imgSet);
    }

    /**
     * The image sets that have been selected by the user
     * <p>
     * This collection does not contain image sets that have been loaded purely as
     * dependencies for other sets.
     *
     * @return A copy of the selected image sets collection
     */
    public Set<String> getActiveImageSets() {
        return new LinkedHashSet<>(activeImageSets);
    }

    /**
     * Replaces the current set of active imageSets without modifying
     * valid imageSets that are already active. does nothing if newImageSets is null
     *
     * @param newImageSets All the imageSets that should now be active
     */
    private void setActiveImageSets(Collection<String> newImageSets) {
        if (newImageSets == null) {
            return;
        }

        var toRemove = getActiveImageSets();
        toRemove.removeAll(newImageSets);

        var toAdd = new ArrayList<String>();
        for (String set : newImageSets) {
            if (!loadedImageSets.containsKey(set) || !activeImageSets.contains(set)) {
                toAdd.add(set);
            }
        }

        boolean isExit = getManager().isExitOnLastRemoved();
        getManager().setExitOnLastRemoved(false);

        toAdd.forEach(this::addActiveImageSet);
        toRemove.forEach(this::removeActiveImageSet);

        getManager().setExitOnLastRemoved(isExit);
    }

    /**
     * Adds a new image set to the list of selected image sets.
     * <p>
     * Loads the image set if it has not been loaded yet. If the image set is successfully
     * loaded (or was already loaded), it adds it to the selected image sets and creates a
     * mascot using the image set.
     * <p>
     * Image sets that have already been added to selections aren't re-added,
     * but a mascot is still created.
     */
    private void addActiveImageSet(String imageSet) {
        if (!loadedImageSets.containsKey(imageSet)) {
            try {
                loadImageSet(imageSet);
            } catch (ConfigurationException | ParserConfigurationException | SAXException | IOException e) {
                e.printStackTrace();
                loadedImageSets.remove(imageSet);
                Main.showError(Tr.tr("FailedLoadConfigErrorMessage")
                               + "\n" + e.getMessage()
                               + "\n" + Tr.tr("SeeLogForDetails"));
            }
        }

        if (loadedImageSets.containsKey(imageSet)) {
            if (!activeImageSets.contains(imageSet)) {
                activeImageSets.add(imageSet);
            }
            createMascot(imageSet);
        }
    }

    /**
     * Clears all loaded resources for the specified image set.
     * <p>
     * If the image sets is a selected image set, it is removed from selection.
     * All mascots of that image set are disposed.
     * <p>
     * Image sets that depend on this image set are not changed.
     */
    private void removeActiveImageSet(String imageSet) {
        activeImageSets.remove(imageSet);
        getManager().remainNone(imageSet);
        loadedImageSets.remove(imageSet);
    }

    /**
     * Clears all image set data and reloads all selected image sets
     */
    private void reloadImageSets() {
        boolean isExit = getManager().isExitOnLastRemoved();
        getManager().setExitOnLastRemoved(false);

        getManager().disposeAll();

        loadedImageSets.clear();
        getActiveImageSets().forEach(this::addActiveImageSet);

        getManager().setExitOnLastRemoved(isExit);
    }

    //----------mascot creation-----------//

    /**
     * Randomly picks an image set from {@link #activeImageSets} and creates a mascot
     */
    public void createMascot() {
        if (getProgramFolder().isMonoImageSet()) {
            createMascot("");
        } else {
            int length = activeImageSets.size();
            int random = (int) (length * Math.random());
            createMascot(activeImageSets.get(random));
        }
    }

    /**
     * Creates a mascot from the specified imageSet.
     * <p>
     * Fails if the image set has not been loaded.
     */
    public void createMascot(String imageSet) {

        // Create one mascot
        final Mascot mascot = new Mascot(imageSet);

        // Create it outside the bounds of the screen
        mascot.setAnchor(new Point(-4000, -4000));

        // Randomize the initial orientation
        mascot.setLookRight(Math.random() < 0.5);

        try {
            mascot.setBehavior(Objects.requireNonNull(getConfiguration(imageSet)).buildBehavior(null, mascot));
            getManager().add(mascot);
        } catch (Exception e) {
            log.log(Level.SEVERE, imageSet + " fatal error, can not be started.", e);
            Main.showError(
                    Tr.tr("CouldNotCreateShimejiErrorMessage") + ": " + imageSet +
                    ".\n" + e.getMessage()
                    + "\n" + Tr.tr("SeeLogForDetails"));
            mascot.dispose();
        }
    }

///=======v This class ends here, everything below is meant to be easily deletable v========//

    //---------Setting storage/extraction------------//

    private static String getSetting(Map<String, String> defaultValues, String key) {
        var sp = System.getProperty(SP_PREFIX + key);
        if (sp != null) {
            return sp;
        } else if (defaultValues != null) {
            return defaultValues.get(key);
        }
        return null;
    }

    private void loadAllSettings(Path inputFilePath) {
        var props = loadProperties(inputFilePath);

        for (String key : USER_SWITCH_KEYS) {
            var s = getSetting(props, key);
            if (s != null) {
                userSwitches.put(key, Boolean.parseBoolean(s));
            }
        }

        for (String key: IMGSET_DEFAULTS_KEYS) {
            var s = getSetting(props, key);
            if (s != null) {
                imageSetDefaults.put(key, s);
            }
        }

        var localeProp = getSetting(props, "Language");
        if (localeProp != null) {
            locale = Locale.forLanguageTag(localeProp);
        }

        var pfProp = getSetting(props, "ProgramFolder");
        ShimejiProgramFolder basePf = programFolder;
        if (pfProp != null) {
            basePf = ShimejiProgramFolder.fromFolder(Path.of(pfProp));
        }

        var altConfSp = getSetting(props, "ProgramFolder.conf");
        var altImgSp = getSetting(props, "ProgramFolder.img");
        var altSoundSp = getSetting(props, "ProgramFolder.sound");
        var altMonoSp = getSetting(props, "ProgramFolder.mono");

        programFolder = new ShimejiProgramFolder(
                altConfSp != null ? Path.of(altConfSp) : basePf.confPath(),
                altImgSp != null ? Path.of(altImgSp) : basePf.imgPath(),
                altSoundSp != null ? Path.of(altSoundSp) : basePf.soundPath(),
                altMonoSp != null ? Boolean.parseBoolean(altMonoSp) : basePf.isMonoImageSet());

        var selectionsProp = getSetting(props, "ActiveShimeji");
        if (selectionsProp != null) {
            var ims = Stream
                    .of(selectionsProp.split("/"))
                    .map(s -> URLDecoder.decode(s, StandardCharsets.UTF_8))
                    .filter(s -> {
                        Path p = programFolder.imgPath().resolve(s);
                        return Files.isDirectory(p);
                    })
                    .collect(Collectors.toSet());

            if (!programFolder.isMonoImageSet()) {
                ims.remove("");
            }
            activeImageSets.addAll(ims);
        }

    }

    private void writeAllSettings(Path outputFilePath) {
        Map<String, String> props = new HashMap<>();

        userSwitches.forEach((k,v) -> props.put(k, v + ""));
        if (getScaling() != 1) {
            props.put("Scaling", getScaling() + "");
        }

        if (!getLocale().equals(Locale.ENGLISH)) {
            props.put("Language", getLocale().toLanguageTag());
        }

        var sb = new StringBuilder();
        for (String set : getActiveImageSets()) {
            sb.append(URLEncoder.encode(set, StandardCharsets.UTF_8)).append('/');
        }
        props.put("ActiveShimeji", sb.toString());

        // program folder excluded on purpose since there's not going to be a gui for it

        writeProperties(props, outputFilePath);
    }

    private static Map<String, String> loadProperties(Path propsPath) {
        var props = new Properties();

        if (propsPath != null && Files.isRegularFile(propsPath)) {
            try (var input = new InputStreamReader(new FileInputStream(propsPath.toFile()), StandardCharsets.UTF_8)) {
                props.load(input);
            } catch (Exception ignored) {
            }
        }

        Map<String, String> ret = new HashMap<>();
        props.forEach((k,v) -> ret.put((String) k, (String) v));

        return ret;
    }

    private static void writeProperties(Map<String, String> propsMap, Path outPath) {
        var props = new Properties(propsMap.size());
        props.putAll(propsMap);
        try (var out = new OutputStreamWriter(new FileOutputStream(outPath.toFile()), StandardCharsets.UTF_8)) {
            props.store(out, "ShimejiEE preferences");
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to write settings:" + outPath, e);
        }
    }

    //----------Tray Icon------------//

    private CheckboxMenuItem getToggleItem(String langBundleKey, BooleanSupplier getter, Consumer<Boolean> setter) {
        final var toggleBtn = new CheckboxMenuItem(Tr.tr(langBundleKey), getter.getAsBoolean());
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

        //-----------safely loopable stuff----------//

        //{{langName, langCode}}
        final String[][] languageTable =
                {{"English", "en-GB"}, //English
                        {"Català", "ca-ES"},//Catalan
                        {"Deutsch", "de-DE"},//German
                        {"Español", "es-ES"},//Spanish
                        {"Français Canadien", "fr-CA"}, // Canadian french
                        {"Hrvatski", "hr-HR"},//Croatian
                        {"Italiano", "it-IT"},//Italian
                        {"Nederlands", "nl-NL"},//Dutch
                        {"Polski", "pl-PL"},//Polish
                        {"Português Brasileiro", "pt-BR"},//Brazilian Portuguese
                        {"Português", "pt-PT"},//Portuguese
                        {"ру́сский язы́к", "ru-RU"},//Russian
                        {"Română", "ro-RO"},//Romanian
                        {"Srpski", "sr-RS"},//Serbian
                        {"Suomi", "fi-FI"},//Finnish
                        {"tiếng Việt", "vi-VN"},//Vietnamese
                        {"简体中文", "zh-CN"},//Chinese(simplified)
                        {"繁體中文", "zh-TW"},//Chinese(traditional)
                        {"한국어", "ko-KR"}};//Korean

        //------------------------------------//

        // create shimeji
        final MenuItem callShimeji = new MenuItem(Tr.tr("CallShimeji"));
        callShimeji.addActionListener(event -> createMascot());

        // chase mouse
        final MenuItem followCursor = new MenuItem(Tr.tr("FollowCursor"));
        followCursor.addActionListener(event -> getManager().setBehaviorAll(Main.BEHAVIOR_GATHER));

        // Reduce to One
        final MenuItem reduceToOne = new MenuItem(Tr.tr("ReduceToOne"));
        reduceToOne.addActionListener(event -> getManager().remainOne());

        // Undo window interaction
        final MenuItem restoreWindows = new MenuItem(Tr.tr("RestoreWindows"));
        restoreWindows.addActionListener(event -> NativeFactory.getInstance().getEnvironment().restoreIE());

        //--------------//

        //--languages submenu
        final Menu languageMenu = new Menu(Tr.tr("Language"));
        for (String[] lang : languageTable) {
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

        final var breedingToggle = getToggleItem("BreedingCloning", this::isBreedingAllowed, this::setBreedingAllowed);
        final var transientToggle = getToggleItem("BreedingTransient", this::isTransientBreedingAllowed, this::setTransientBreedingAllowed);
        final var transformToggle = getToggleItem("Transformation", this::isTransformationAllowed, this::setTransformationAllowed);
        final var windowThrowToggle = getToggleItem("ThrowingWindows", this::isIEMovementAllowed, this::setIEMovementAllowed);
        final var soundToggle = getToggleItem("SoundEffects", this::isSoundAllowed, this::setSoundAllowed);
        final var chooserAtStartToggle = getToggleItem("AlwaysShowShimejiChooser", this::shouldShowChooserAtStart, this::setShouldShowChooserAtStart);
        final var behaviorTranslationToggle = getToggleItem("TranslateBehaviorNames", this::shouldTranslateBehaviorNames, this::setShouldTranslateBehaviorNames);
        final var ignoreImagesetPropsToggle = getToggleItem("IgnoreImagesetProperties", this::shouldIgnoreImagesetProperties, this::setShouldIgnoreImagesetProperties);

        togglesMenu.add(breedingToggle);
        togglesMenu.add(transientToggle);
        togglesMenu.add(transformToggle);
        togglesMenu.add(windowThrowToggle);
        togglesMenu.add(soundToggle);
        togglesMenu.add(chooserAtStartToggle);
        togglesMenu.add(behaviorTranslationToggle);
        togglesMenu.add(ignoreImagesetPropsToggle);

        //----------------------//

        //image set chooser
        final MenuItem chooseShimeji = new MenuItem(Tr.tr("ChooseShimeji"));
        chooseShimeji.addActionListener(e -> {
            ImageSetChooserUtils.askUserForSelection(this::setActiveImageSets, getActiveImageSets());
        });

        //reload button
        final MenuItem reloadMascot = new MenuItem(Tr.tr("ReloadMascots"));
        reloadMascot.addActionListener(e -> reloadImageSets());

        // Quit Button
        final MenuItem dismissAll = new MenuItem(Tr.tr("DismissAll"));
        dismissAll.addActionListener(e -> System.exit(0));

        //----Create pop-up menu-----//
        final PopupMenu trayPopup = new PopupMenu();

        trayPopup.add(callShimeji);
        trayPopup.add(followCursor);
        trayPopup.add(reduceToOne);
        trayPopup.add(restoreWindows);

        trayPopup.add(new MenuItem("-"));

        trayPopup.add(languageMenu);
        trayPopup.add(scalingMenu);
        trayPopup.add(togglesMenu);

        trayPopup.add(new MenuItem("-"));

        trayPopup.add(chooseShimeji);
        trayPopup.add(reloadMascot);
        trayPopup.add(dismissAll);

        try {
            //adding the tray icon
            Path trayIconPath = getProgramFolder().getIconPath();
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
            log.log(Level.SEVERE, "Failed to create tray icon", e);
            System.exit(1);
        }
    }

}
