package com.group_finity.mascot;

import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.config.Entry;
import com.group_finity.mascot.image.ImagePairs;
import com.group_finity.mascot.sound.Sounds;
import com.group_finity.mascot.ui.imagesets.ImageSetUtils;
import com.group_finity.mascot.imageset.ShimejiProgramFolder;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

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

    private ShimejiProgramFolder programFolder;
    {
        try {
            programFolder = ShimejiProgramFolder.fromFolder(JAR_PARENT_DIR);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final ConcurrentMap<String, Configuration> configurations = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Set<String>> dependencies = new ConcurrentHashMap<>();
    private final List<String> activeImageSets = new ArrayList<>();

    private Locale locale = Locale.ENGLISH;
    private int scaling = 1;
    private final ConcurrentMap<String, Boolean> userSwitches = new ConcurrentHashMap<>();

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

    public Configuration getConfiguration(String imageSet) {
        return configurations.get(imageSet);
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

    public int getScaling() {return scaling;}
    private void setScaling(int scaling) {
        this.scaling = scaling > 0 ? scaling : 1;
        reloadImageSets();
    }

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
            ImageSetUtils.askUserForSelection(c -> {
                setActiveImageSets(c);
                getManager().setExitOnLastRemoved(isExit);
            });
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
     * <p>
     * If the image sets depends on other image sets, those image sets are also loaded.
     * This function only loads the image set. It does not add it to the list of selected image sets,
     * nor does it create a mascot.
     *
     * @return true if the imageSet was successfully loaded
     */
    private boolean loadImageSet(final String imageSet) {

        try {
            Configuration configuration = new Configuration();
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            //--actions.xml--/
            Path actionsFilePath = getProgramFolder().getActionConfPath(imageSet);
            Entry actionDocEntry = new Entry(docBuilder.parse(actionsFilePath.toFile()).getDocumentElement());
            configuration.load(actionDocEntry, imageSet);

            //--behaviors.xml--//
            Path behaviorsFilePath = getProgramFolder().getBehaviorConfPath(imageSet);
            Entry behaviorDocEntry = new Entry(docBuilder.parse(behaviorsFilePath.toFile()).getDocumentElement());
            configuration.load(behaviorDocEntry, imageSet);

            //---validate and set config---//
            configuration.validate();
            configurations.put(imageSet, configuration);

            // loading dependencies
            Set<String> deps = new HashSet<>();

            for (final Entry actionList : actionDocEntry.selectChildren("ActionList")) {
                for (final Entry actionNode : actionList.selectChildren("Action")) {
                    if (actionNode.getAttributes().containsKey("BornMascot")) {
                        deps.add(actionNode.getAttribute("BornMascot"));
                    }
                    if (actionNode.getAttributes().containsKey("TransformMascot")) {
                        deps.add(actionNode.getAttribute("TransformMascot"));
                    }
                }
            }

            for (String dep : deps) {
                if (!configurations.containsKey(dep)) {
                    boolean loaded = loadImageSet(dep);
                    if (!loaded) {
                        deps.remove(dep);
                    }
                }
            }

            dependencies.put(imageSet, deps);

            return true;

        } catch (final Exception e) {
            log.log(Level.SEVERE, "Failed to load configuration files", e);
            e.printStackTrace();

            // error cleanup
            unloadImageSet(imageSet);

            Main.showError(Tr.tr("FailedLoadConfigErrorMessage")
                    + "\n" + e.getMessage()
                    + "\n" + Tr.tr("SeeLogForDetails"));
        }

        return false;
    }

    /**
     * Clears all loaded resources for the specified image set.
     * <p>
     * If the image sets is a selected image set, it is removed from selections
     * and all mascots of that image set are disposed.
     * <p>
     * Image sets that depend on this image set are not changed.
     */
    private void unloadImageSet(String imageSet) {
        activeImageSets.remove(imageSet);
        getManager().remainNone(imageSet);
        configurations.remove(imageSet);
        dependencies.remove(imageSet);
        ImagePairs.removeAllFromImageSet(imageSet);
        // the sounds just leak since they can be shared between imgSets
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
        if (getProgramFolder().isMonoImageSet()) {
            return new HashSet<>(Set.of(""));
        }
        Set<String> ret = new HashSet<>(activeImageSets);
        ret.remove("");
        return ret;
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
            if (!configurations.containsKey(set) || !activeImageSets.contains(set)) {
                toAdd.add(set);
            }
        }

        boolean isExit = getManager().isExitOnLastRemoved();
        getManager().setExitOnLastRemoved(false);

        toAdd.forEach(this::addActiveImageSet);

        // done after loading so that new deps are reflected
        for (String set : newImageSets) {
            if (dependencies.containsKey(set)) {
                toRemove.removeAll(dependencies.get(set));
            }
        }

        toRemove.forEach(this::unloadImageSet);

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
        boolean loaded = configurations.containsKey(imageSet);
        if (!loaded){
            loaded = loadImageSet(imageSet);
        }

        if (loaded) {
            if (!activeImageSets.contains(imageSet)) {
                activeImageSets.add(imageSet);
            }
            createMascot(imageSet);
        }
    }

    /**
     * Clears all image set data and reloads all selected image sets
     */
    private void reloadImageSets() {
        boolean isExit = getManager().isExitOnLastRemoved();
        getManager().setExitOnLastRemoved(false);

        getManager().disposeAll();

        // Wipe all loaded data
        configurations.clear();
        dependencies.clear();
        ImagePairs.clear();
        Sounds.clear();

        // re-add
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
            mascot.setBehavior(getConfiguration(imageSet).buildBehavior(null, mascot));
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

    private final String[] USER_SWITCH_KEYS = {
            "Breeding",
            "Transformation",
            "Throwing",
            "Sounds",
            "Multiscreen",
            "AlwaysShowShimejiChooser",
            "TranslateBehaviorNames"
    };

    private final String[] OTHER_PREF_KEYS = {
            "Language",
            "Scaling",
            "ActiveShimeji",
            "InteractiveWindows",
            // ↓ cli only options
            "ProgramFolder",
            "ProgramFolder.conf",
            "ProgramFolder.img",
            "ProgramFolder.sound",
            "ProgramFolder.mono",
    };

    private static String getSetting(Properties defaultValues, String key) {
        var sp = System.getProperty(SP_PREFIX + key);
        if (sp != null) {
            return sp;
        } else if (defaultValues != null) {
            return defaultValues.getProperty(key);
        }
        return null;
    }

    private void loadAllSettings(Path inputFilePath) {
        var props = new Properties();
        try (var input = new InputStreamReader(new FileInputStream(inputFilePath.toFile()), StandardCharsets.UTF_8)) {
            props.load(input);
        } catch (Exception ignored) {
        }

        for (String key : USER_SWITCH_KEYS) {
            var s = getSetting(props, key);
            if (s != null) {
                userSwitches.put(key, Boolean.parseBoolean(s));
            }
        }

        var localeProp = getSetting(props, "Language");
        if (localeProp != null) {
            locale = Locale.forLanguageTag(localeProp);
        }

        var scaleProp = getSetting(props, "Scaling");
        if (scaleProp != null) {
            scaling = Integer.parseInt(scaleProp);
        }

        var pfProp = getSetting(props, "ProgramFolder");
        ShimejiProgramFolder basePf = programFolder;
        if (pfProp != null) {
            try {
                basePf = ShimejiProgramFolder.fromFolder(Path.of(pfProp));
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
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
        if (selectionsProp != null && !programFolder.isMonoImageSet()) {
            var ims = new HashSet<>(List.of(selectionsProp.split("/")));
            ims.remove("");
            try {
                ims.retainAll(programFolder.getImageSetNames());
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            activeImageSets.addAll(ims);
        }

    }

    private void writeAllSettings(Path outputFilePath) {
        var props = new Properties();

        userSwitches.forEach((k,v) -> props.setProperty(k, String.valueOf(v)));
        if (getScaling() != 1) {
            props.setProperty("Scaling", String.valueOf(getScaling()));
        }
        if (!getLocale().equals(Locale.ENGLISH)) {
            props.setProperty("Language", getLocale().toLanguageTag());
        }

        var sb = new StringBuilder();
        for (String set : getActiveImageSets()) {
            sb.append(set).append('/');
        }
        props.setProperty("ActiveShimeji", sb.toString());

        // program folder excluded on purpose since there's not going to be a gui for it

        try (var out = new OutputStreamWriter(new FileOutputStream(outputFilePath.toFile()), StandardCharsets.UTF_8)) {
            props.store(out, "ShimejiEE preferences");
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to write settings:" + outputFilePath, e);
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

        final int[] scalingOptions = {1, 2, 3, 4, 5, 6, 7, 8};

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
        for (int opt : scalingOptions) {
            final var scaleBtn = new MenuItem(String.valueOf(opt));
            scaleBtn.addActionListener(e -> setScaling(opt));
            scalingMenu.add(scaleBtn);
        }

        //--behaviour toggles submenu
        final Menu togglesMenu = new Menu(Tr.tr("AllowedBehaviours"));

        final var breedingToggle = getToggleItem("BreedingCloning", this::isBreedingAllowed, this::setBreedingAllowed);
        final var transientToggle = getToggleItem("BreedingTransient", this::isTransientBreedingAllowed, this::setTransientBreedingAllowed);
        final var transformToggle = getToggleItem("Transformation", this::isTransformationAllowed, this::setTransformationAllowed);
        final var windowThrowToggle = getToggleItem("ThrowingWindows", this::isIEMovementAllowed, this::setIEMovementAllowed);
        final var chooserAtStartToggle = getToggleItem("AlwaysShowShimejiChooser", this::shouldShowChooserAtStart, this::setShouldShowChooserAtStart);
        final var behaviorTranslationToggle = getToggleItem("TranslateBehaviorNames", this::shouldTranslateBehaviorNames, this::setShouldTranslateBehaviorNames);
        final var soundToggle = getToggleItem("SoundEffects", this::isSoundAllowed, this::setSoundAllowed);

        togglesMenu.add(breedingToggle);
        togglesMenu.add(transientToggle);
        togglesMenu.add(transformToggle);
        togglesMenu.add(windowThrowToggle);
        togglesMenu.add(soundToggle);
        togglesMenu.add(chooserAtStartToggle);
        togglesMenu.add(behaviorTranslationToggle);

        //----------------------//

        //image set chooser
        final MenuItem chooseShimeji = new MenuItem(Tr.tr("ChooseShimeji"));
        chooseShimeji.addActionListener(e -> ImageSetUtils.askUserForSelection(this::setActiveImageSets));

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
