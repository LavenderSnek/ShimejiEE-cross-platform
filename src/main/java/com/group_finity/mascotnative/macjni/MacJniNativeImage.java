package com.group_finity.mascotnative.macjni;

import com.group_finity.mascot.image.NativeImage;

import java.awt.image.BufferedImage;

class MacJniNativeImage implements NativeImage {

    private static native long createNSImageFromArray(int[] pixels, int width, int height);
    private static native void disposeNsImage(long nsImagePtr);

    private final long ptr;
    private final int w;
    private final int h;

    MacJniNativeImage(BufferedImage image) {
        w = image.getWidth();
        h = image.getHeight();
        int[] rgb = image.getRGB(0,0, w, h, null, 0, w);
        this.ptr = createNSImageFromArray(rgb, w, h);
    }

    long getNsImagePtr() {
        return ptr;
    }

    @Override
    public int getWidth() {
        return w;
    }

    @Override
    public int getHeight() {
        return h;
    }

    @Override
    public void dispose() {
        disposeNsImage(ptr);
    }

}
