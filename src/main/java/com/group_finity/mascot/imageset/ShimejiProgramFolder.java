package com.group_finity.mascot.imageset;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

/**
 * Represents the standard directory layout for shimeji and shimejiEE.
 * Note that these paths can be named anything and not just the default names.
 *
 * @param confPath The folder that contains the default config files. Does not need to
 *                 exist if all the image sets have their own conf folders.
 *
 * @param imgPath The folder that contains the imageSet folders (or just the imageSet if isMonoImageSet is true).
 *                It also contains icon.png for the tray icon and optionally dock-icon.png for the dock icon.
 *                Needs to exist and contain specified files.
 *
 * @param soundPath Global sounds folder. Since this is just a place for the program can search for sound files,
 *                  It does not have to actually exist; but it can't be null either.
 *
 * @param isMonoImageSet Whether the imgPath folder directly contains an image set.
 *                       This was common in very old versions of shimeji.
 * */
public record ShimejiProgramFolder(
        Path confPath,
        Path imgPath,
        Path soundPath,
        boolean isMonoImageSet
) {
    private static final int MONO_CHECK_THRESHOLD = 20;

    private static final String DEFAULT_CONF_DIR = "conf";
    private static final String DEFAULT_IMG_DIR = "img";
    private static final String DEFAULT_SOUND_DIR = "sound";

    private static final String UNUSED_DIR = "unused";
    private static final String ICON_NAME = "icon.png";
    private static final String SHIME_1 = "shime1.png";

    private static final String[] BEHAVIOR_FILENAMES = {
            "behavior.xml", "behaviors.xml",
            "行動.xml", "#U884c#U52d5.xml",
            "ÞíîÕïò.xml", "µ¦-.xml", "ìsô«.xml",
            "two.xml", "2.xml",
    };

    private static final String[] ACTIONS_FILENAMES = {
            "actions.xml", "action.xml",
            "動作.xml", "#U52d5#U4f5c.xml",
            "Õïòõ¢£.xml", "¦-º@.xml", "ô«ìý",
            "one.xml", "1.xml",
    };

    /**
     * Creates a {@link ShimejiProgramFolder} object from an existing shimeji installation.
     * It does not guarantee that any of the paths will actually exist.
     */
    public static ShimejiProgramFolder fromFolder(Path programFolder) throws IOException {
        final Path imgPath = programFolder.resolve(DEFAULT_IMG_DIR);
        final Path confPath = programFolder.resolve(DEFAULT_CONF_DIR);
        final Path soundPath = programFolder.resolve(DEFAULT_SOUND_DIR);

        // this isn't a surefire way to check if it's a mono imageSet, but it'll work most of the time
        boolean isMono = listMatchingFilesIn(imgPath, (path, basicFileAttributes) -> {
            boolean isFile = basicFileAttributes.isRegularFile();
            boolean isImage = path.toString().toLowerCase().endsWith(".png");
            return isFile && isImage;
        }
        ).size() > MONO_CHECK_THRESHOLD;

        return new ShimejiProgramFolder(confPath, imgPath, soundPath, isMono);
    }

    /**
     * Names of the image sets in the program folder
     * @return if {@link #isMonoImageSet()} is true then it returns a list with one empty string,
     *         otherwise it returns all directories in {@link #imgPath()} except dot-files and 'unused'
     */
    public List<String> getImageSetNames() throws IOException {
        if (isMonoImageSet) {
            return List.of("");
        }
        var matches =  listMatchingFilesIn(imgPath, ((path, basicFileAttributes) -> {
            String name = path.getFileName().toString().toLowerCase();
            boolean ignored = name.charAt(0) == '.' || name.equalsIgnoreCase(UNUSED_DIR);
            boolean isDir = basicFileAttributes.isDirectory();
            return !ignored && isDir;
        }));
        return matches.stream()
                .map(path -> path.getFileName().toString())
                .toList();
    }

    /**
     * @param imageSetName name of an imageSet in this program folder,
     *                     value ignored if {@link #isMonoImageSet()} is true.
     */
    public Path getBehaviorConfPath(String imageSetName) throws FileNotFoundException {
        return getConfFilePath(imageSetName, BEHAVIOR_FILENAMES);
    }

    /**
     * @param imageSetName name of an imageSet in this program folder,
     *                     value ignored if {@link #isMonoImageSet()} is true.
     */
    public Path getActionConfPath(String imageSetName) throws FileNotFoundException {
        return getConfFilePath(imageSetName, ACTIONS_FILENAMES);
    }

    /**
     * Path of the highest priority sound file found with given name and imageSet
     * @param imageSetName name of an imageSet in this program folder,
     *                     value ignored if {@link #isMonoImageSet()} is true.
     * @param soundFileName name of the sound file to find
     */
    public Path getSoundFilePath(String imageSetName, String soundFileName) throws FileNotFoundException {
        imageSetName = isMonoImageSet ? "" : imageSetName;
        // this is a reversed order from the official code; but it fits better w other file finding in shimeji
        Path[] SOUND_DIRS = {
                imgPath.resolve(imageSetName).resolve(DEFAULT_SOUND_DIR),
                soundPath.resolve(imageSetName),
                soundPath
        };

        for (Path dir : SOUND_DIRS) {
            Path fp = Path.of(dir.toString(), soundFileName);
            if (Files.isRegularFile(fp)) {
                return fp;
            }
        }

        throw new FileNotFoundException();
    }

    /**
     * Path to tray icon
     */
    public Path getIconPath() {
        Path iconPath = imgPath.resolve(ICON_NAME);
        if (Files.isRegularFile(iconPath)) {
            return iconPath;
        } else {
            return null;
        }
    }

    /**
     * path of an image representing the image set. (ie, the one used in the chooser)
     * @param imageSetName name of an imageSet in this program folder,
     *                     value ignored if {@link ShimejiProgramFolder#isMonoImageSet()} is true.
     */
    public Path getIconPathForImageSet(String imageSetName) {
        imageSetName = isMonoImageSet ? "" : imageSetName;
        Path imageSetPath = imgPath.resolve(imageSetName);

        Path iconPath = imageSetPath.resolve(ICON_NAME);
        if (Files.isRegularFile(iconPath)) {
            return iconPath;
        }
        iconPath = imageSetPath.resolve(SHIME_1);
        if (Files.isRegularFile(iconPath)) {
            return iconPath;
        } else {
            return null;
        }
    }

    private static List<Path> listMatchingFilesIn(Path folder, BiPredicate<Path, BasicFileAttributes> filter) throws IOException {
        try (Stream<Path> s = Files.find(folder,1, filter)) {
            return s.filter(path -> !path.equals(folder)).toList();
        }
    }

    private Path getConfFilePath(String imageSetName, String[] allowedNames) throws FileNotFoundException {
        imageSetName = isMonoImageSet ? "" : imageSetName;

        Path[] CONF_DIRS = {
                imgPath.resolve(imageSetName).resolve(DEFAULT_CONF_DIR),
                confPath.resolve(imageSetName),
                confPath
        };

        for (Path dir : CONF_DIRS) {
            for (String name : allowedNames) {
                Path fp = dir.resolve(name);
                if (Files.isRegularFile(fp)) {
                    return fp;
                }
            }
        }

        throw new FileNotFoundException("Unable to locate config files for: " + imageSetName);
    }

}