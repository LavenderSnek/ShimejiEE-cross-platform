package com.group_finity.mascot;

import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.config.Entry;
import com.group_finity.mascot.image.ImagePairs;
import com.group_finity.mascot.imagesets.ImageSetUtils;
import com.group_finity.mascot.sound.Sounds;
import com.group_finity.mascotnative.win.WindowsInteractiveWindowForm;
import com.group_finity.shimejiutils.ShimejiProgramFolder;
import com.joconner.i18n.Utf8ResourceBundleControl;
import com.sun.jna.Platform;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
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

    private static final Path SETTINGS_PATH;

    static {

        // sets up the logging + settings

        Path trueConfDir = null;
        try {
            trueConfDir = Path.of(Main.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI()).getParent().resolve("conf");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (trueConfDir == null || !Files.isDirectory(trueConfDir)) {
            showError(
                    """
                    unable find conf dir!
                    The original conf directory containing settings + logging properties\s
                    needs to exist in the jar's parent folder even if all folders were specified manually
                    """);
            System.exit(1);
        }

        SETTINGS_PATH = trueConfDir.resolve("settings.properties");

        try (var ins = new FileInputStream(trueConfDir.resolve("logging.properties").toFile())){
            LogManager.getLogManager().readConfiguration(ins);
        } catch (final SecurityException | IOException e) {
            e.printStackTrace();
        }

        //----------UI setup-----------/

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

    }

    private ResourceBundle languageBundle;

    private ShimejiProgramFolder programFolder;

    private static final JFrame frame = new javax.swing.JFrame();

    private final Manager manager = new Manager();

    /**
     * Key: An imageSet name
     * <p>Value: Configuration associated with the imageSet
     */
    private final Hashtable<String, Configuration> configurations = new Hashtable<>();

    /**
     * A list of the active imageSets
     */
    private final ArrayList<String> imageSets = new ArrayList<>();

    /**
     * The global settings
     */
    private Properties properties = new Properties();

    private static final Main instance = new Main();


    //--------------Simple Getters--------------//

    private Manager getManager() {
        return this.manager;
    }

    public Properties getProperties() {
        return properties;
    }

    public ResourceBundle getLanguageBundle() {
        return languageBundle;
    }

    public ShimejiProgramFolder getProgramFolder() {
        return programFolder;
    }

    //-----------------------------------//

    public Configuration getConfiguration(String imageSet) {
        return configurations.get(imageSet);
    }

    public static Main getInstance() {
        return instance;
    }

    //-----------------Initialization--------------------//

    /**
     * Program entry point
     */
    public static void main(final String[] args) {
        try {
            // not ideal, but too few people will use the command line for a proper lib to be worth it
            Map<String, String> argsMap = new HashMap<>();
            for (String arg: args) {
                String[] parts = arg.split("=",2);
                argsMap.put(parts[0], parts[1]);
            }

            ShimejiProgramFolder base;
            if (argsMap.containsKey("--pf")) {
                base = ShimejiProgramFolder.fromFolder(Path.of(argsMap.get("--pf")));
            } else {
                base = ShimejiProgramFolder
                        .fromFolder(Path.of(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent());
            }

            Path altConfPath = argsMap.containsKey("--conf") ? Path.of(argsMap.get("--conf")): base.confPath();
            Path altImgPath= argsMap.containsKey("--img") ? Path.of(argsMap.get("--img")) : base.imgPath();
            Path altSoundPath = argsMap.containsKey("--sound") ? Path.of(argsMap.get("--sound")) : base.soundPath();
            boolean altMono = argsMap.containsKey("--mono") ? Boolean.parseBoolean(argsMap.get("--mono")) : base.isMonoImageSet();

            getInstance().programFolder = new ShimejiProgramFolder(altConfPath, altImgPath, altSoundPath, altMono);

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        try {
            getInstance().run();
        } catch (OutOfMemoryError error) {
            final String msg = "Out of Memory.";
            log.log(Level.SEVERE, msg, error);
            Main.showError(msg);
            System.exit(0);
        }
    }

    private void loadChosenLanguage() {
        try {
            ResourceBundle.Control utf8Control = new Utf8ResourceBundleControl();
            languageBundle = ResourceBundle.getBundle(
                    "language",
                    Locale.forLanguageTag(properties.getProperty("Language", "en-GB")),
                    utf8Control
            );
        } catch (Exception ex) {
            Main.showError("The language files could not be loaded. Make sure java is set up properly");
            exit();
        }
    }

    public void run() {

        // Dock icon on platforms that support it
        // https://stackoverflow.com/questions/6006173/
        final Path imageResource = getProgramFolder().getDockIconPath();
        if (imageResource != null) {
            try {
                final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
                final Image image = defaultToolkit.getImage(imageResource.toString());

                final Taskbar taskbar = Taskbar.getTaskbar();
                taskbar.setIconImage(image);
            } catch (final UnsupportedOperationException | SecurityException ignored) {
            }
        }

        // load settings.properties
        properties = new Properties();
        try (FileInputStream input = new FileInputStream(SETTINGS_PATH.toFile())) {
            properties.load(input);
        } catch (IOException ignored) {
        }

        loadChosenLanguage();

        //because the chooser is async
        boolean isExit = getManager().isExitOnLastRemoved();
        getManager().setExitOnLastRemoved(false);

        //image choosing at startup
        boolean chooseAtStart = Boolean.parseBoolean(properties.getProperty("ShowChooserAtStart", "false"));
        ArrayList<String> selection = ImageSetUtils.getImageSetsFromSettings();

        // the policy is basically that the user should see the chooser or a mascot atleast once
        if (chooseAtStart || selection.isEmpty()) {
            ImageSetUtils.askUserForSelection(newImageSets -> {
                setActiveImageSets(newImageSets);
                getManager().setExitOnLastRemoved(isExit);
            });
        }
        else {
            // keeps only existent setting selections to avoid unnecessary errors
            try {
                selection.retainAll(getProgramFolder().getImageSetNames());
            } catch (IOException e) {
                log.log(Level.SEVERE, "unable to load imageSets", e);
                showError("unable to load imageSets");
                exit();
            }
            setActiveImageSets(selection);

            if (imageSets.isEmpty()) {
                ImageSetUtils.askUserForSelection(newImageSets -> {
                    setActiveImageSets(newImageSets);
                    getManager().setExitOnLastRemoved(isExit);
                });
            } else {
                getManager().setExitOnLastRemoved(isExit);
            }
        }

        createTrayIcon();

        getManager().start();
    }


    /**
     * @return true if the imageSet was successfully loaded
     */
    private boolean loadConfiguration(final String imageSet) {

        try {
            Configuration configuration = new Configuration();

            //--actions.xml--/
            Path actionsFilePath = getProgramFolder().getActionConfPath(imageSet);

            log.log(Level.INFO, imageSet + " Read Action File ({0})", actionsFilePath);

            final Document actions = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(actionsFilePath.toFile());
            configuration.load(new Entry(actions.getDocumentElement()), imageSet);


            //--behaviors.xml--//
            Path behaviorsFilePath = getProgramFolder().getBehaviorConfPath(imageSet);

            log.log(Level.INFO, imageSet + " Read Behavior File ({0})", behaviorsFilePath);

            final Document behaviors = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(behaviorsFilePath.toFile());
            configuration.load(new Entry(behaviors.getDocumentElement()), imageSet);


            //---validate and set config---//
            configuration.validate();
            configurations.put(imageSet, configuration);

            // Initial action: BornMascot
            for (final Entry list : new Entry(actions.getDocumentElement()).selectChildren("ActionList")) {
                for (final Entry node : list.selectChildren("Action")) {
                    if (node.getAttributes().containsKey("BornMascot") && !configurations.containsKey(node.getAttribute("BornMascot"))) {
                        loadConfiguration(node.getAttribute("BornMascot"));
                    }
                    if (node.getAttributes().containsKey("TransformMascot") && !configurations.containsKey(node.getAttribute("TransformMascot"))) {
                        loadConfiguration(node.getAttribute("TransformMascot"));
                    }
                }
            }

            return true;

        } catch (final Exception e) {
            log.log(Level.SEVERE, "Failed to load configuration files", e);
            e.printStackTrace();

            // error cleanup
            configurations.remove(imageSet);
            ImagePairs.imagepairs.keySet().removeIf(k -> imageSet.equals(k.split("/")[0]));

            Main.showError(languageBundle.getString("FailedLoadConfigErrorMessage") + "\n"
                    + e.getMessage() + "\n" + languageBundle.getString("SeeLogForDetails"));
        }

        return false;
    }


    //--------imageSet management---------//

    /**
     * Replaces the current set of active imageSets without modifying
     * valid imageSets that are already active. does nothing if newImageSets are null
     * <p>Writes end result to settings. Invalid sets excluded.
     *
     * @param newImageSets All the imageSets that should now be active
     */
    private void setActiveImageSets(ArrayList<String> newImageSets) {
        if (newImageSets == null) {
            return;
        }

        //Not worth using a HashSet
        var toRemove = new ArrayList<>(imageSets);
        toRemove.removeAll(newImageSets);

        var toAdd = new ArrayList<String>();
        for (String set : newImageSets) {
            if (!configurations.containsKey(set)) {
                toAdd.add(set);
            }
        }

        boolean isExit = getManager().isExitOnLastRemoved();
        getManager().setExitOnLastRemoved(false);

        for (String r : toRemove) {
            removeLoadedImageSet(r);
        }

        for (String a : toAdd) {
            addNewImageSet(a);
        }

        getManager().setExitOnLastRemoved(isExit);

        serializeImageSetSettings();
    }

    private void removeLoadedImageSet(String imageSet) {
        imageSets.remove(imageSet);
        getManager().remainNone(imageSet);
        configurations.remove(imageSet);
        ImagePairs.imagepairs.keySet().removeIf(k -> imageSet.equals(k.split("/")[0]));
    }

    private void addNewImageSet(String imageSet) {
        if (loadConfiguration(imageSet)) {
            imageSets.add(imageSet);
            createMascot(imageSet);
        }
    }

    //----------mascot creation-----------//

    /**
     * Randomly picks from {@link #imageSets} creates a mascot
     */
    public void createMascot() {
        int length = imageSets.size();
        int random = (int) (length * Math.random());
        createMascot(imageSets.get(random));
    }

    /**
     * Creates a mascot from the specified imageSet name.
     * <p>The imageSet's configuration has to be loaded first or it shows an error
     */
    public void createMascot(String imageSet) {
        log.log(Level.INFO, "create a mascot");

        // Create one mascot
        final Mascot mascot = new Mascot(imageSet);

        // Create it outside the bounds of the screen
        mascot.setAnchor(new Point(-4000, -4000));

        // Randomize the initial orientation
        mascot.setLookRight(Math.random() < 0.5);

        try {

            mascot.setBehavior(getConfiguration(imageSet).buildBehavior(null, mascot));
            this.getManager().add(mascot);

        } catch (Exception e) {

            e.printStackTrace();
            log.log(Level.SEVERE, imageSet + " fatal error, can not be started.", e);
            Main.showError(languageBundle.getString("CouldNotCreateShimejiErrorMessage") + " " + imageSet +
                    ".\n" + e.getMessage() + "\n" + languageBundle.getString("SeeLogForDetails"));
            mascot.dispose();

        }
    }

    //--------------Utilities-------------//

    public void exit() {
        this.getManager().disposeAll();
        this.getManager().stop();

        System.exit(0);
    }

    private void reloadMascots() {
        boolean isExit = getManager().isExitOnLastRemoved();
        getManager().setExitOnLastRemoved(false);
        getManager().disposeAll();

        // Wipe all loaded data
        ImagePairs.imagepairs.clear();
        configurations.clear();

        // Load settings
        for (String imageSet : imageSets) {
            loadConfiguration(imageSet);
        }

        // Create the first mascot
        for (String imageSet : imageSets) {
            createMascot(imageSet);
        }

        getManager().setExitOnLastRemoved(isExit);
    }

    public static void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }


    /**
     * Serializes and writes current imageSets into
     * the `ActiveShimeji` property in settings.properties
     */
    private void serializeImageSetSettings() {
        StringBuilder builder = new StringBuilder();
        HashSet<String> uniqueImgSets = new HashSet<>(imageSets); // makes sure its all unique
        for (String imgSet : uniqueImgSets) {
            builder.append(imgSet).append('/');
        }

        properties.setProperty("ActiveShimeji", String.valueOf(builder));
        writeSettings();
    }

    /**
     * writes the current {@link #properties} to `settings.properties`
     * @see #serializeImageSetSettings() for imageSetSettings
     */
    private void writeSettings() {
        try (FileOutputStream output = new FileOutputStream(SETTINGS_PATH.toFile())) {
            properties.store(output, "Shimeji-ee Configuration Options");
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to write to settings.properties", e);
        }
    }

    //-----------other settings------------//

    private void setLanguage(String newLangCode) {
        if (!properties.getProperty("Language", "en-GB").equals(newLangCode)) {
            properties.setProperty("Language", newLangCode);
            refreshLanguage();
        }
        writeSettings();
    }

    private void refreshLanguage() {
        loadChosenLanguage();
        //reload tray icon if it exists
        if (SystemTray.isSupported()) {
            SystemTray.getSystemTray().remove(SystemTray.getSystemTray().getTrayIcons()[0]);
            createTrayIcon();
        }
    }


    private void setScaling(String scaling) {
        properties.setProperty("Scaling", scaling);
        writeSettings();
        // need to reload the shimeji as the images have rescaled
        reloadMascots();
    }

    //--Toggling--//
    private void toggleProperty(String propertyKey, boolean initiallyTrue) {
        if (initiallyTrue) {
            properties.setProperty(propertyKey, "false");
        } else {
            properties.setProperty(propertyKey, "true");
        }

        writeSettings();
    }

//--------------v-UI RELATED CODE IS BELOW-v-------------//

    private CheckboxMenuItem getGenericToggleItem(String langBundleKey, String propertyKey) {
        final var toggleBtn = new CheckboxMenuItem(
                languageBundle.getString(langBundleKey),
                Boolean.parseBoolean(properties.getProperty(propertyKey, "true"))
        );

        toggleBtn.addItemListener(e -> toggleProperty(propertyKey, !toggleBtn.getState()));

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
                        {"Français", "fr-FR"},//French
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

        final String[] scalingOptions = {"1", "2", "3", "6", "8"};

        //------------------------------------//

        // create shimeji
        final MenuItem callShimeji = new MenuItem(languageBundle.getString("CallShimeji"));
        callShimeji.addActionListener(event -> createMascot());

        // chase mouse
        final MenuItem followCursor = new MenuItem(languageBundle.getString("FollowCursor"));
        followCursor.addActionListener(event -> getManager().setBehaviorAll(BEHAVIOR_GATHER));

        // Reduce to One
        final MenuItem reduceToOne = new MenuItem(languageBundle.getString("ReduceToOne"));
        reduceToOne.addActionListener(event -> getManager().remainOne());

        // Undo window interaction
        final MenuItem restoreWindows = new MenuItem(languageBundle.getString("RestoreWindows"));
        restoreWindows.addActionListener(event -> NativeFactory.getInstance().getEnvironment().restoreIE());

        //--------------//

        //--languages submenu
        final Menu languageMenu = new Menu(languageBundle.getString("Language"));
        for (String[] lang : languageTable) {
            final var langName = lang[0];
            final var langCode = lang[1];
            final var langBtn = new MenuItem(langName);
            langBtn.addActionListener(e -> setLanguage(langCode));
            languageMenu.add(langBtn);
        }

        //--scaling submenu
        final Menu scalingMenu = new Menu(languageBundle.getString("Scaling"));
        for (String opt : scalingOptions) {
            final var scaleBtn = new MenuItem(opt);
            scaleBtn.addActionListener(e -> setScaling(opt));
            scalingMenu.add(scaleBtn);
        }

        //--behaviour toggles submenu
        final Menu togglesMenu = new Menu(languageBundle.getString("AllowedBehaviours"), true);

        final var breedingToggle = getGenericToggleItem
                ("BreedingCloning", "Breeding");

        final var transformToggle = getGenericToggleItem
                ("Transformation", "Transformation");

        final var windowThrowToggle = getGenericToggleItem
                ("ThrowingWindows", "Throwing");

        final var multiscreenToggle = getGenericToggleItem
                ("Multiscreen", "Multiscreen");

        //this is slightly different from the rest so i didn't use the function
        final var soundToggle = new CheckboxMenuItem(
                languageBundle.getString("SoundEffects"),
                Boolean.parseBoolean(properties.getProperty("Sounds", "true"))
        );
        soundToggle.addItemListener(e -> {
            final boolean initiallyTrue = !soundToggle.getState();
            toggleProperty("Sounds", initiallyTrue);
            Sounds.setMuted(initiallyTrue);
        });

        togglesMenu.add(breedingToggle);
        togglesMenu.add(transformToggle);
        togglesMenu.add(windowThrowToggle);
        togglesMenu.add(soundToggle);
        togglesMenu.add(multiscreenToggle);

        //----------------------//

        //image set chooser
        final MenuItem chooseShimeji = new MenuItem(languageBundle.getString("ChooseShimeji"));
        chooseShimeji.addActionListener(e -> ImageSetUtils.askUserForSelection(this::setActiveImageSets));

        //interactive window chooser
        MenuItem interactiveMenu = new MenuItem(languageBundle.getString("ChooseInteractiveWindows"));
        interactiveMenu.addActionListener(e -> {
            new WindowsInteractiveWindowForm(frame, true).display();
            NativeFactory.getInstance().getEnvironment().refreshCache();
            writeSettings();
        });

        //reload button
        final MenuItem reloadMascot = new MenuItem(languageBundle.getString("ReloadMascots"));
        reloadMascot.addActionListener(e -> reloadMascots());

        // Quit Button
        final MenuItem dismissAll = new MenuItem(languageBundle.getString("DismissAll"));
        dismissAll.addActionListener(e -> exit());

        //----Create pop-up menu-----//
        final PopupMenu trayPopup = new PopupMenu();

        trayPopup.add(callShimeji);
        trayPopup.add(followCursor);
        trayPopup.add(reduceToOne);
        trayPopup.add(restoreWindows);

        trayPopup.add(new MenuItem("-"));

        trayPopup.add(languageMenu);
        if (Platform.isWindows()) {
            trayPopup.add(scalingMenu);
        }
        trayPopup.add(togglesMenu);

        trayPopup.add(new MenuItem("-"));

        trayPopup.add(chooseShimeji);
        trayPopup.add(reloadMascot);
        // selective window interaction is only available on windows for now
        if (Platform.isWindows()) {
            trayPopup.add(interactiveMenu);
        }
        trayPopup.add(dismissAll);

        try {
            //adding the tray icon
            Path trayIconPath = getProgramFolder().getIconPath();
            Image trayIcon;
            if (trayIconPath != null) {
                trayIcon = ImageIO.read(trayIconPath.toFile());
            } else {
                trayIcon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
            }
            final TrayIcon icon = new TrayIcon(trayIcon, "ShimejiEE", trayPopup);

            // Flip the click required to create mascot on non-windows
            if (Platform.isWindows()) {
                icon.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(final MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            createMascot();
                        }
                    }
                });
            } else {
                icon.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(final MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            createMascot();
                        }
                    }
                });
            }

            // show tray icon
            SystemTray.getSystemTray().add(icon);

        } catch (final IOException | AWTException e) {
            log.log(Level.SEVERE, "Failed to create tray icon", e);
            exit();
        }
    }

}
