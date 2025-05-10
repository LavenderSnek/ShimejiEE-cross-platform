package com.group_finity.mascotnative.virtualdesktop;

import com.group_finity.mascot.image.NativeImage;

import java.awt.image.BufferedImage;

public record VirtualImage(BufferedImage bufferedImage) implements NativeImage {
    @Override
    public int getWidth() {
        return bufferedImage.getWidth();
    }

    @Override
    public int getHeight() {
        return bufferedImage.getHeight();
    }
}
