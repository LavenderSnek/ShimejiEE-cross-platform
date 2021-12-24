package com.group_finity.mascotnative.macjni;

import com.group_finity.mascot.image.NativeImage;

import java.awt.image.BufferedImage;

public class MacJniNativeImage implements NativeImage {

    public static native long createNSImageFromArray(int[] pixels, int width, int height);

    private final long ptr;

    MacJniNativeImage(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int[] rgb = image.getRGB(0,0, w, h, null, 0, w);
        this.ptr = createNSImageFromArray(rgb, w, h);
    }

    public long getNsImagePtr() {
        return ptr;
    }

}
