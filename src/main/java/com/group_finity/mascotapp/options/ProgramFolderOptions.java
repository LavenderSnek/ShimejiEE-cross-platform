package com.group_finity.mascotapp.options;

import com.group_finity.mascot.imageset.ShimejiProgramFolder;

import java.nio.file.Path;
import java.util.Optional;

import static picocli.CommandLine.Option;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ProgramFolderOptions {

    @Option(names = "--pf",
            description = "Override for the base directory of the shimeji installation. " +
                          "lib and settings not affected.")
    public Optional<Path> base = Optional.empty();

    @Option(names = "--img", description = "Override for the `img` dir")
    public Optional<Path> img = Optional.empty();

    @Option(names = "--conf", description = "Override for the global `conf` dir")
    public Optional<Path> conf = Optional.empty();

    @Option(names = "--sound", description = "Override for the global `sound` dir")
    public Optional<Path> sound = Optional.empty();

    @Option(names = "--mono", description = "Search for sprites directly in the `img` dir")
    public boolean mono = false;

    /**
     * Creates a program folder object based on the options
     * @param defaultBase default base path used when no base is provided in options
     * @return newly created program folder
     */
    public ShimejiProgramFolder toProgramFolder(Path defaultBase) {
        var pf = ShimejiProgramFolder.fromFolder(base.orElse(defaultBase));
        return new ShimejiProgramFolder(
                conf.orElse(pf.confPath()),
                img.orElse(pf.imgPath()),
                sound.orElse(pf.soundPath()),
                mono
        );
    }
}
