package com.group_finity.mascotapp;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.MascotPrefProvider;
import com.group_finity.mascot.NativeFactory;
import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.config.DefaultPoseLoader;
import com.group_finity.mascot.config.Entry;
import com.group_finity.mascot.image.ImagePairLoaderBuilder;
import com.group_finity.mascot.imageset.ImageSet;
import com.group_finity.mascot.imageset.ImageSetStore;
import com.group_finity.mascot.imageset.ShimejiImageSet;
import com.group_finity.mascot.imageset.ShimejiProgramFolder;
import com.group_finity.mascot.sound.SoundLoader;
import com.group_finity.mascotapp.options.*;

import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.LogManager;

import static java.util.Map.entry;
import static picocli.CommandLine.*;

@Command(
        name = "run",
        descriptionHeading = "%nDescription: %n",
        description = "Runs shimeji with CLI settings. Reading/Writing `settings.properties` is disabled in this mode.%n",
        mixinStandardHelpOptions = true,
        sortOptions = false,
        sortSynopsis = false
)
public class ShimejiRun implements Callable<Integer> {

    // saved
    @ArgGroup(validate = false, heading = "%nGeneral:%n") PersistentAppOptions genOpts = new PersistentAppOptions();
    @ArgGroup(validate = false, heading = "%nImage Set:%n") ImageSetOptions imgOpts = new ImageSetOptions();

    // cli only
    @ArgGroup(validate = false, heading = "%nProgram Folder:%n") ProgramFolderOptions pfOpts = new ProgramFolderOptions();
    @ArgGroup(validate = false, heading = "%nLaunch:%n") LaunchAppOptions launchOpts = new LaunchAppOptions();

    private Manager manager  = new Manager();

    private Map<String, Runnable> userActions = Map.ofEntries(
            entry("ui.SpawnMascot", this::spawnMascot)
    );

    private void spawnMascot() {

    }

    @Override
    public Integer call() throws Exception {
        // temp until i fix the logging
        LogManager.getLogManager().reset();

        NativeFactory.getInstance().getEnvironment().init();

        var pf = ShimejiProgramFolder.fromFolder(Path.of("/Users/snek/code/shimeji-projects/ShimejiEE-cross-platform/ext-resources"));

        var ims = loadImageSet(pf, "greeen-shime", imgOpts);

        var m = new Mascot("", MascotPrefProvider.DEFAULT, name -> name == null ? null : ims);

        m.setAnchor(new Point(-4000, -4000));
        m.setBehavior(m.getOwnImageSet().getConfiguration().buildBehavior(null, m));

        manager.add(m);

        manager.start().get();
        return 0;
    }

    public static ImageSet loadImageSet(ShimejiProgramFolder pf, String name, ImageSetOptions options) throws Exception {
        Path actionsPath = pf.getActionConfPath(name);
        Path behaviorPath = pf.getBehaviorConfPath(name);

        var docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        var actionsEntry = new Entry(docBuilder.parse(actionsPath.toFile()).getDocumentElement());
        var behaviorEntry = new Entry(docBuilder.parse(behaviorPath.toFile()).getDocumentElement());

        var imgStore = new ImagePairLoaderBuilder()
                .setScaling(options.scaling)
                .setLogicalAnchors(options.logicalAnchors)
                .setAsymmetryNameScheme(options.asymmetryNameScheme)
                .setPixelArtScaling(options.pixelArtScaling)
                .buildForBasePath(pf.imgPath().resolve(name));

        var soundStore = new SoundLoader(pf, name);
        soundStore.setFixRelativeGlobalSound(options.fixRelativeGlobalSound);

        var config = new Configuration();
        config.load(new DefaultPoseLoader(imgStore, soundStore), actionsEntry, behaviorEntry);
        config.validate();

        return new ShimejiImageSet(config, imgStore, soundStore);
    }
}
