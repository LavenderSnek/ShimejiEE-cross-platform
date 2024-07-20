package com.group_finity.mascotapp.runners;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.NativeFactory;
import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.config.DefaultPoseLoader;
import com.group_finity.mascot.config.Entry;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.image.ImagePairLoaderBuilder;
import com.group_finity.mascot.imageset.ImageSet;
import com.group_finity.mascot.imageset.ShimejiImageSet;
import com.group_finity.mascot.imageset.ShimejiProgramFolder;
import com.group_finity.mascot.sound.SoundLoader;
import com.group_finity.mascotapp.Constants;
import com.group_finity.mascotapp.Manager;
import com.group_finity.mascotapp.imageset.ImageSetLoadingDelegate;
import com.group_finity.mascotapp.imageset.ImageSetManager;
import com.group_finity.mascotapp.imageset.ImageSetSelectionDelegate;
import com.group_finity.mascotapp.options.ImageSetOptions;
import com.group_finity.mascotapp.options.LaunchAppOptions;
import com.group_finity.mascotapp.options.PersistentAppOptions;
import com.group_finity.mascotapp.options.ProgramFolderOptions;

import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.Point;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.logging.LogManager;

import static picocli.CommandLine.*;

@Command(
        name = "run",
        descriptionHeading = "%nDescription: %n",
        description = "Runs shimeji with CLI settings. Reading/Writing `settings.properties` is disabled in this mode.%n",
        mixinStandardHelpOptions = true,
        sortOptions = false,
        sortSynopsis = false
)
public class MinimalRunner implements Runnable, ImageSetLoadingDelegate, ImageSetSelectionDelegate {

    // saved
    @ArgGroup(validate = false, heading = "%nGeneral:%n") PersistentAppOptions genOpts = new PersistentAppOptions();
    @ArgGroup(validate = false, heading = "%nImage Set:%n") ImageSetOptions imgOpts = new ImageSetOptions();

    // cli only
    @ArgGroup(validate = false, heading = "%nProgram Folder:%n") ProgramFolderOptions pfOpts = new ProgramFolderOptions();
    @ArgGroup(validate = false, heading = "%nLaunch:%n") LaunchAppOptions launchOpts = new LaunchAppOptions();

    private Manager manager;
    private ImageSetManager imageSets;

    private ShimejiProgramFolder programFolder = null;

    @Override
    public ImageSet load(String name) {
        try {
            return loadImageSet(programFolder, name, imgOpts);
        } catch (Exception e) {
            System.err.println("Unable to load image set: " + name);
            e.printStackTrace(); // gonna throw these about and replace them w real error handling later
        }
        return null;
    }

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
                System.err.println("Unable to dispose of imageSet: "  + name);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void imageSetHasBeenAdded(String name, ImageSet imageSet) {
        spawnMascot(name);
    }

    private void spawnMascot(String imageSetName) {
        if (imageSets.get(imageSetName) == null) {
            System.err.println("Invalid spawn, image set not loaded: " + imageSetName);
            return;
        }

        var m = new Mascot(imageSetName, imgOpts, imageSets);

        m.setAnchor(new Point(-4000, -4000));
        try {
            m.setBehavior(m.getOwnImageSet().getConfiguration().buildBehavior(null, m));
            manager.add(m);
        } catch (CantBeAliveException | BehaviorInstantiationException e) {
            m.dispose();
            System.err.println("Unable to spawn mascot");
            e.printStackTrace();
        }
    }

    public void init() {
        // setup object w settings
        programFolder = pfOpts.toProgramFolder(Constants.JAR_DIR);
        imageSets = new ImageSetManager(this, this);
        manager = new Manager();

        // setup native
        NativeFactory.init(launchOpts.nativePkg, Constants.NATIVE_LIB_DIR);
        NativeFactory.getInstance().getEnvironment().init();
    }

    @Override
    public void run() {
        // temp until i fix the logging
        LogManager.getLogManager().reset();

        // setup
        init();

        // selection
        imageSets.setSelected(genOpts.imageSetSelections);

        // start manager
        try {
            manager.start().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static ImageSet loadImageSet(ShimejiProgramFolder pf, String name, ImageSetOptions options) throws Exception {
        var imgStore = new ImagePairLoaderBuilder()
                .setScaling(options.scaling)
                .setLogicalAnchors(options.logicalAnchors)
                .setAsymmetryNameScheme(options.asymmetryNameScheme)
                .setPixelArtScaling(options.pixelArtScaling)
                .buildForBasePath(pf.imgPath().resolve(name));

        var soundStore = new SoundLoader(pf, name);
        soundStore.setFixRelativeGlobalSound(options.fixRelativeGlobalSound);

        // xml
        Path actionsPath = pf.getActionConfPath(name);
        Path behaviorPath = pf.getBehaviorConfPath(name);

        var docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        var actionsEntry = new Entry(docBuilder.parse(actionsPath.toFile()).getDocumentElement());
        var behaviorEntry = new Entry(docBuilder.parse(behaviorPath.toFile()).getDocumentElement());

        // make this XmlConfiguration and extract Configuration to interface for fun
        var config = new Configuration();
        config.load(new DefaultPoseLoader(imgStore, soundStore), actionsEntry, behaviorEntry);
        config.validate();

        return new ShimejiImageSet(config, imgStore, soundStore);
    }

}
