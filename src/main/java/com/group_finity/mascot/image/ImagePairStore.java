package com.group_finity.mascot.image;

import java.awt.Point;
import java.io.IOException;

public interface ImagePairStore {

    /**
     * Loads the image pair and return a key.
     *
     * @param imageText      The raw image text for the left image.
     * @param imageRightText The raw image text for the right image.
     * @param anchor         The unscaled image anchor.
     */
    String load(String imageText, String imageRightText, Point anchor) throws IOException;


    /**
     * Gets the image pair corresponding to the key.
     *
     * @param key The key to an image, obtained from {@link #load(String, String, Point)}.
     * @return The image pair if it has been loaded, null otherwise.
     */
    ImagePair get(String key);

    /**
     * The amount of scaling applied to the images.
     */
    default double getScaling() {
        return 1;
    }

    /**
     * Deletes all images associated with the given store (and disposes native images)
     */
    void disposeAll();
}
