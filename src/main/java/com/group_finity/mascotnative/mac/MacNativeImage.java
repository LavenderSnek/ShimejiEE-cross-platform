package com.group_finity.mascotnative.mac;

import com.group_finity.mascot.image.NativeImage;

import java.awt.image.BufferedImage;

/**
 * Not actually a native image, this is just to separate it from the generic code
 */
class MacNativeImage implements NativeImage {

    private final BufferedImage managedImage;

    public MacNativeImage(final BufferedImage image) {
        this.managedImage = image;
    }

    BufferedImage getManagedImage() {
        return this.managedImage;
    }

}
