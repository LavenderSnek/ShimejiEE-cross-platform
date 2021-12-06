package com.group_finity.mascotnative.generic;

import com.group_finity.mascot.image.NativeImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;

class GenericNativeImage implements NativeImage {

    /**
     * Java Image object.
     */
    private final BufferedImage managedImage;

    private final Icon icon;

    public GenericNativeImage(final BufferedImage image) {
        this.managedImage = image;
        this.icon = new ImageIcon(image);
    }

    BufferedImage getManagedImage() {
        return this.managedImage;
    }

    Icon getIcon() {
        return this.icon;
    }

}
