package com.group_finity.mascot.imagesets;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.imagesets.compact.CompactChooser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Provides utility functions to deal with the contents of the img directory (imageSets).
 * <p>
 * Does not directly modify the global selection settings,
 * this class should only provide information.
 */
public final class ImageSetUtils {

    /**
     * Displays UI for choosing imageSets.
     *
     * @param onSelection consumer for an array of all selected image sets after
     *                    the selection has been completed.
     */
    public static void askUserForSelection(Consumer<ArrayList<String>> onSelection) {
        CompactChooser chooser = new CompactChooser(onSelection);
        chooser.createGui();
    }

    /**
     * Returns list of imageSets that are present in the
     * settings.properties ActiveShimeji property
     * <p>
     * Beware that these sets might not exist due to name changes and deletions
     * <p>
     * does not actually fetch data from the file, instead it reads
     * the properties of the Main instance and parses that.
     */
    public static ArrayList<String> getImageSetsFromSettings() {
        if (Main.getInstance().getProgramFolder().isMonoImageSet()) {
            return new ArrayList<>(List.of(""));
        }

        ArrayList<String> selectedInSettings =
                new ArrayList<>(Arrays.asList(Main.getInstance().getProperties()
                        .getProperty("ActiveShimeji", "").split("/")));

        var retArr = new ArrayList<String>();

        for (var s : selectedInSettings) {
            if (!s.isBlank()) {
                retArr.add(s);
            }
        }

        return retArr;
    }

}
