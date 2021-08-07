package com.group_finity.mascot.imagesets;

import java.util.ArrayList;

/**
 * A UI for choosing imageSets
 */
public interface ImageSetUI {

    /**
     * All the imageSets selected by the user
     *
     * @return ImageSets if any are selected, returns an empty list if no
     * imageSets are selected, and null if the user canceled the selection.
     */
    ArrayList<String> getSelections();

}
