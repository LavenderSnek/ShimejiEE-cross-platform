package com.group_finity.mascotnative.macjni;

import com.group_finity.mascot.image.NativeImage;

import java.awt.image.BufferedImage;

class MacJniNativeImage implements NativeImage {

    private static native long createNSImageFromArray(int[] pixels, int width, int height);
    private static native void disposeNsImage(long nsImagePtr);

    private final long ptr;

    MacJniNativeImage(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int[] rgb = image.getRGB(0,0, w, h, null, 0, w);
        this.ptr = createNSImageFromArray(rgb, w, h);
    }

    long getNsImagePtr() {
        return ptr;
    }

    void dispose() {
        disposeNsImage(ptr);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected final void finalize() throws Throwable {
        super.finalize();
        dispose();
    }

}
