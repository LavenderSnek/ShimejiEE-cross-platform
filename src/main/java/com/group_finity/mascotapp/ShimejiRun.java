package com.group_finity.mascotapp;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.MascotPrefProvider;
import com.group_finity.mascot.NativeFactory;
import com.group_finity.mascot.imageset.ShimejiImageSet;
import com.group_finity.mascot.imageset.ShimejiProgramFolder;
import com.group_finity.mascotapp.options.*;

import java.awt.*;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;

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

    @Override
    public Integer call() throws Exception {
        NativeFactory.getInstance().getEnvironment().init();

        var pf = ShimejiProgramFolder.fromFolder(Path.of("/Users/snek/code/shimeji-projects/ShimejiEE-cross-platform/ext-resources"));

        var ims = ShimejiImageSet.loadFrom(pf, "greeen-shime", Map.of());

        var m = new Mascot("", MascotPrefProvider.DEFAULT, s->ims);

        m.setAnchor(new Point(-4000, -4000));
        m.setBehavior(m.getOwnImageSet().getConfiguration().buildBehavior(null, m));

        manager.add(m);
        manager.start();
        return 0;
    }
}
