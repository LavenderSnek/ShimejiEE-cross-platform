package com.group_finity.mascot.imagesets;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.imagesets.compact.CompactChooser;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Provides utility functions to deal with the contents of the img directory (imageSets).
 * <p>
 * Does not directly modify the global selection settings,
 * this class should only provide information.
 *
 * @apiNote this might just be temporary refactoring leftovers, not sure yet
 * */
public final class ImageSetUtils {

    /**looks for only the image set specific configurations if true, added for debugging purposes*/
    private static final boolean USE_DEFAULT_CONF = true;

    /**Allowed filenames for the action config file, sorted from highest to lowest priority*/
    public static final String[] ACTION_FILENAMES = {
            "actions.xml",
            "action.xml",
            "動作.xml",
            "one.xml",
            "1.xml",
            "Õïòõ¢£.xml",
            "¦-º@.xml",
            "ô«ìý",
    };

    /**Allowed filenames for the behaviour config file, sorted from highest to lowest priority*/
    public static final String[] BEHAVIOUR_FILENAMES = {
            "behaviors.xml",
            "behavior.xml",
            "行動.xml",
            "behaviours.xml",
            "behaviour.xml",
            "two.xml",
            "2.xml",
            "ÞíîÕïò.xml",
            "µ¦-.xml",
            "ìsô«.xml",
    };


    /**looks through the img directory and returns all directories that aren't `unused` or start with a dot*/
    public static String[] getAllImageSets() {
        return new File("./img").list(((dir, name) -> {
           if (name.equals("unused") || name.charAt(0) == '.') {
               return false;
           }
           return Files.isDirectory(Path.of(dir + "/" + name));
       }));
    }

    /**
     * Displays UI for choosing imageSets.
     * @see ImageSetUI#getSelections()
     * */
    public static ArrayList<String> askUserForSelection(){
        ImageSetUI chooser = new CompactChooser(new JFrame());
        return chooser.getSelections();
    }


    /**
     * Returns list of imageSets that are present in the
     * settings.properties ActiveShimeji property
     * <p>Returns null if none are found
     * <p>Beware that these sets might not exist due to name changes and deletions
     *
     * @implNote does not actually fetch data from the file, instead it reads
     * the properties of the Main instance and parses that.
     * */
    public static ArrayList<String> getImageSetsFromSettings() {
        ArrayList<String> selectedInSettings =
                new ArrayList<>(Arrays.asList(Main.getInstance().getProperties()
                        .getProperty("ActiveShimeji", "").split("/")));

        try {
            if (selectedInSettings.get(0).trim().isBlank()) {
                return null;
            }
        } catch (IndexOutOfBoundsException e) {
            return null;
        }

        return selectedInSettings;
    }


    /**
     * Returns the path of the action config for the specified imageSet.
     * @implNote uses {@link #findConfig(String[], String)} and passes in {@link #ACTION_FILENAMES}
     * */
    public static String findActionConfig(String imageSet) throws FileNotFoundException {
        return findConfig(ACTION_FILENAMES, imageSet);
    }


    /**
     * Returns the path of the behaviour config for the specified imageSet.
     * @implNote uses {@link #findConfig(String[], String)} and passes in {@link #BEHAVIOUR_FILENAMES}
     * */
    public static String findBehaviorConfig(String imageSet) throws FileNotFoundException {
        return findConfig(BEHAVIOUR_FILENAMES, imageSet);
    }


    /**
     * Finds a file in the config folders for the given imageSet name
     * throws file not found if it can't find anything.
     *
     * <p>Looks in the following directories (high to low priority):
     *
     * <ul>
     *     <li>./img/{@code imageSet}/conf/</li>
     *     <li>./conf/{@code imageSet}/</li>
     *     <li>./conf/ (if USE_DEFAULT_CONF = true, which it generally is)</li>
     * </ul>
     *
     * @param fileNames Allowed file names for the config file. Assumed to be sorted from highest to lowest priority
     * @param imageSet Folder name of an imageSet
     * */
    private static String findConfig(String[] fileNames, String imageSet) throws FileNotFoundException {
        String[] CONF_DIRS = {
                "./img/" + imageSet + "/conf/",
                "./conf/" + imageSet + "/",
                "./conf/",
        };

        if (!USE_DEFAULT_CONF) {
            CONF_DIRS = new String[]{
                    "./img/" + imageSet + "/conf/",
                    "./conf/" + imageSet + "/",
            };
        }

        for (String dir : CONF_DIRS) {
            for (String name : fileNames) {
                String fp = dir + name;
                if (Files.isRegularFile(Path.of(fp))) {
                    return fp;
                }
            }
        }

        throw new FileNotFoundException("Unable to locate any config files for" + imageSet
                + "\n USE_DEFAULT_CONF = " + USE_DEFAULT_CONF);
    }
}
