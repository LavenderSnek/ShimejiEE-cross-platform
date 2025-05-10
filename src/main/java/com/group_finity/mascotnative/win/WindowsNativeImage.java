package com.group_finity.mascotnative.win;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascotnative.win.jna.GDI32;

import java.awt.image.BufferedImage;

import static com.sun.jna.platform.win32.WinDef.HBITMAP;
import static com.sun.jna.platform.win32.WinGDI.BITMAP;
import static com.sun.jna.platform.win32.WinGDI.BITMAPINFO;
import static com.sun.jna.platform.win32.WinGDI.BITMAPINFOHEADER;
import static com.sun.jna.platform.win32.WinGDI.DIB_RGB_COLORS;

/**
 * An alpha-valued image that can be used with {@link WindowsTranslucentWindow}.
 * <p>
 * Only Windows bitmaps can be used for {@link WindowsTranslucentWindow},
 * so copy pixels from an existing {@link BufferedImage} to a Windows bitmap.
 */
class WindowsNativeImage implements NativeImage {

    private final HBITMAP bmpHandle;
    private final int w;
    private final int h;

    public WindowsNativeImage(BufferedImage src, int scaling) {
        w = src.getWidth() * scaling;
        h = src.getHeight() * scaling;
        bmpHandle = createBitmap(src, scaling);
    }

    public HBITMAP getBmpHandle() {
        return bmpHandle;
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
        GDI32.INSTANCE.DeleteObject(getBmpHandle());
    }

    //=== Util

    private static HBITMAP createBitmap(BufferedImage image, int scaling) {

        int[] unscaledRgb = image.getRGB(0,0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());

        int width = image.getWidth() * scaling;
        int height = image.getHeight() * scaling;

        HBITMAP handle = createNative(width, height);

        BITMAP bmp = new BITMAP();
        GDI32.INSTANCE.GetObjectW(handle, bmp.size(), bmp.getPointer());
        bmp.read();

        final int destPitch = ((width * bmp.bmBitsPixel) + 31) / 32 * 4;
        int destIndex = destPitch * (height - 1);
        int srcColIndex = 0;
        int srcRowIndex = 0;

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {

                long offset = destIndex + x * 4L;
                int value = unscaledRgb[srcColIndex/scaling];

                if ((value & 0xFF000000) == 0) {
                    value = 0;
                }

                bmp.bmBits.setInt(offset,value);

                ++srcColIndex;
            }

            destIndex -= destPitch;

            // resets the srcColIndex to re-use the same indexes and stretch horizontally
            ++srcRowIndex;
            if (srcRowIndex != scaling) {
                srcColIndex -= width;
            } else {
                srcRowIndex = 0;
            }
        }

        return handle;
    }

    /**
     * Creates the empty windows bitmap
     *
     * @param width  width of the bitmap.
     * @param height the height of the bitmap.
     * @return tThe pointer to the newly created bitmap
     */
    private static HBITMAP createNative(final int width, final int height) {
        final BITMAPINFOHEADER header = new BITMAPINFOHEADER();
        header.biSize = 40;
        header.biWidth = width;
        header.biHeight = height;
        header.biPlanes = 1;
        header.biBitCount = 32;

        BITMAPINFO bmi = new BITMAPINFO();
        bmi.bmiHeader = header;

        return GDI32.INSTANCE.CreateDIBSection(null, bmi, DIB_RGB_COLORS, null, null, 0);
    }

}
