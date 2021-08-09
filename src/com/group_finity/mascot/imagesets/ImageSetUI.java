package com.group_finity.mascot.imagesets;

import java.util.ArrayList;

/**
 * A UI for choosing imageSets
 */
public interface ImageSetUI {

    /**
     * All the imageSets selected by the user
     *
     * @return ImageSets if any are selected, an empty list if 0
     * imageSets are selected, and null if the user canceled the selection.
     */
    ArrayList<String> getSelections();

}
