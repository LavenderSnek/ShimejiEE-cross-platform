package com.group_finity.mascotapp.imageset;

import com.group_finity.mascot.imageset.ImageSet;

public interface ImageSetSelectionDelegate {

    /**
     * Called when any image set will be removed
     */
    default void imageSetWillBeRemoved(String name, ImageSet imageSet) {
    }


    /**
     * Called when any image set has been removed
     */
    default void imageSetHasBeenRemoved(String name, ImageSet imageSet) {
    }

    /**
     * Called when a new image set will be added to the current selection.
     * Not called for dependencies nor dependencies turning into selections.
     */
    default void imageSetWillBeAdded(String name, ImageSet imageSet) {
    }

    /**
     * Called when a new image set has been added to the current selection and loaded.
     * Not called for dependencies nor dependencies turned into selections.
     */
    default void imageSetHasBeenAdded(String name, ImageSet imageSet) {
    }

    /**
     * Called when an image set that was previously loaded as a dependency will become selected
     */
    default void dependencyWillBecomeSelection(String name, ImageSet imageSet) {
    }

    /**
     * Called when an image set that was previously loaded as a dependency has become selected
     */
    default void dependencyHasBecomeSelection(String name, ImageSet imageSet) {
    }

}
