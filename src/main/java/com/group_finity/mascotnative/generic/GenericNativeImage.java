package com.group_finity.mascotnative.generic;

import com.group_finity.mascot.image.NativeImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;

class GenericNativeImage implements NativeImage {

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

    @Override
    public int getWidth() {
        return managedImage.getWidth();
    }

    @Override
    public int getHeight() {
        return managedImage.getHeight();
    }

}
