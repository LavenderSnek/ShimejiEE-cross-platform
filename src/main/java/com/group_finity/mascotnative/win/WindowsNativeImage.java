package com.group_finity.mascotnative.win;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascotnative.win.jna.BITMAP;
import com.group_finity.mascotnative.win.jna.BITMAPINFOHEADER;
import com.group_finity.mascotnative.win.jna.Gdi32;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.awt.image.BufferedImage;

/**
 * An alpha-valued image that can be used with {@link WindowsTranslucentWindow}.
 * <p>
 * Only Windows bitmaps can be used for {@link WindowsTranslucentWindow},
 * so copy pixels from an existing {@link BufferedImage} to a Windows bitmap.
 */
class WindowsNativeImage implements NativeImage {

    private static final OsArchitecture ARCHITECTURE;

    static {
        ARCHITECTURE = System.getProperty("sun.arch.data.model").equals("64")
                ? OsArchitecture.x86_64
                : OsArchitecture.x86;
    }

    private final BufferedImage managedImage;
    private final Pointer nativeHandle;

    /**
     * Creates the windows bitmap
     *
     * @param width  width of the bitmap.
     * @param height the height of the bitmap.
     * @return tThe pointer to the newly created bitmap
     */
    private static Pointer createNative(final int width, final int height) {

        final BITMAPINFOHEADER bmi = new BITMAPINFOHEADER();
        bmi.biSize = 40;
        bmi.biWidth = width;
        bmi.biHeight = height;
        bmi.biPlanes = 1;
        bmi.biBitCount = 32;

        return Gdi32.INSTANCE.CreateDIBSection(Pointer.NULL, bmi, Gdi32.DIB_RGB_COLORS, Pointer.NULL, Pointer.NULL, 0);
    }

    /**
     * Fill a native windows bitmap with the specified pixel data
     *
     * @param nativeHandle bitmap handle.
     * @param rgb          unscaled ARGB pixels to transfer.
     * @param scaling      amount of scaling to apply when transferring the image.
     */
    private static void flushToNative(final Pointer nativeHandle, final int[] rgb, final int scaling) {

        final BITMAP bmp = new BITMAP();
        Gdi32.INSTANCE.GetObjectW(nativeHandle, ARCHITECTURE.getBitmapSize() + Native.POINTER_SIZE, bmp);

        // Copy at the pixel level. These dimensions are already scaled
        int width = bmp.bmWidth;
        int height = bmp.bmHeight;
        final int destPitch = ((bmp.bmWidth * bmp.bmBitsPixel) + 31) / 32 * 4;
        int destIndex = destPitch * (height - 1);
        int srcColIndex = 0;
        int srcRowIndex = 0;

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                //UpdateLayeredWindow and Photoshop seem to be incompatible
                //UpdateLayeredWindow has a bug that the alpha value is ignored when the RGB value is #FFFFFF.
                //Photoshop sets the RGB value to 0 where the alpha value is 0.

                bmp.bmBits.setInt(destIndex + x * 4L,
                        (rgb[srcColIndex / scaling] & 0xFF000000) == 0 ? 0 : rgb[srcColIndex / scaling]);

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

    }

    /**
     * Disposes native bitmap
     * @param nativeHandle native bitmap to dispose.
     */
    private static void freeNative(final Pointer nativeHandle) {
        Gdi32.INSTANCE.DeleteObject(nativeHandle);
    }

    public WindowsNativeImage(final BufferedImage image) {
        int scaling = Main.getInstance().getScaling();

        this.managedImage = image;
        int imageWidth = getManagedImage().getWidth();
        int imageHeight = getManagedImage().getHeight();

        this.nativeHandle = createNative(imageWidth * scaling, imageHeight * scaling);

        int[] rbgValues = this.getManagedImage()
                .getRGB(0, 0, imageWidth, imageHeight, null, 0, imageWidth);

        flushToNative(this.getNativeHandle(), rbgValues, scaling);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        freeNative(this.getNativeHandle());
    }

    Pointer getNativeHandle() {
        return this.nativeHandle;
    }

    private BufferedImage getManagedImage() {
        return this.managedImage;
    }

}
