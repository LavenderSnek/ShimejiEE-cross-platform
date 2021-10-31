package com.group_finity.mascot.image;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class ImagePairs {

    /**
     * Key: The filename as `shimeji-name/image-left.png` or `shimeji-name/image-left.png:shimeji-name/image-right.png`
     * <br>Value: The {@link ImagePair} associated with that filename
     */
    private static final Hashtable<String, ImagePair> imagepairs = new Hashtable<>(40);

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

    public static void clear() {
        imagepairs.clear();
    }

    public static void removeAllFromImageSet(String imageSetName) {
        imagepairs.keySet().removeIf(s -> {
            String keySetName = s.split("/", 2)[0];
            return imageSetName.equals(keySetName);
        });
    }

    public static MascotImage getImage(String filename, boolean isLookRight) {
        if (!imagepairs.containsKey(filename)) {
            return null;
        }
        return imagepairs.get(filename).getImage(isLookRight);
    }

}