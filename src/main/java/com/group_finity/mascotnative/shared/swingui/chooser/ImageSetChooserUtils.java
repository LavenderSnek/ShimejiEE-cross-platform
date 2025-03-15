package com.group_finity.mascotnative.shared.swingui.chooser;

import com.group_finity.mascot.imageset.ShimejiProgramFolder;

import java.util.Collection;
import java.util.function.Consumer;

public final class ImageSetChooserUtils {

    /**
     * Displays UI for choosing imageSets.
     *
     * @param onSelection consumer for an array of all selected image sets after
     *                    the selection has been completed.
     */
    public static void askUserForSelection(Consumer<Collection<String>> onSelection, Collection<String> currentSelection, ShimejiProgramFolder pf) {
        CompactChooser chooser = new CompactChooser(onSelection, currentSelection, pf);
        chooser.createGui();
    }

}
