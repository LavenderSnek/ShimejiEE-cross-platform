package com.group_finity.mascot.image;

import java.util.Hashtable;

public class ImagePairs {

    /**
     * Key: The filename as `shimeji-name/image-left.png` or `shimeji-name/image-left.png:shimeji-name/image-right.png`
     * <br>Value: The {@link ImagePair} associated with that filename
     */
    private static final Hashtable<String, ImagePair> imagepairs = new Hashtable<>(40);

    static void load(final String filename, final ImagePair imagepair) {
        if (!imagepairs.containsKey(filename)) {
            imagepairs.put(filename, imagepair);
        }
    }

    public static ImagePair getImagePair(String filename) {
        if (!imagepairs.containsKey(filename)) {
            return null;
        }
        return imagepairs.get(filename);
    }

    static boolean contains(String filename) {
        return imagepairs.containsKey(filename);
    }

}