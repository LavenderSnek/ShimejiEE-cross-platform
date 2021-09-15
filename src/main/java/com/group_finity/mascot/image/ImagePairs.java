package com.group_finity.mascot.image;

import java.util.Hashtable;

public class ImagePairs {

    /**
     * Key: The filename as '/shimeji-name/image-name.png'
     * <br>Value: The {@link ImagePair} associated with that filename
     */
    public static Hashtable<String, ImagePair> imagepairs = new Hashtable<>(40);

    public static void load(final String filename, final ImagePair imagepair) {
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

    public static boolean contains(String filename) {
        return imagepairs.containsKey(filename);
    }

    public static MascotImage getImage(String filename, boolean isLookRight) {
        if (!imagepairs.containsKey(filename)) {
            return null;
        }
        return imagepairs.get(filename).getImage(isLookRight);
    }

}