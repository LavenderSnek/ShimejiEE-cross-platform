package com.group_finity.mascotapp;

import com.group_finity.mascotapp.options.*;

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


    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
