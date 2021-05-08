package com.group_finity.mascot.win;

import com.group_finity.mascot.Main;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.win.jna.BITMAP;
import com.group_finity.mascot.win.jna.BITMAPINFOHEADER;
import com.group_finity.mascot.win.jna.Gdi32;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import hqx.*;

/**
 * {@link WindowsTranslucentWindow} a value that can be used with images.
 * 
 * {@link WindowsTranslucentWindow} is available because only Windows bitmap
 * {@link BufferedImage} existing copy pixels from a Windows bitmap.
 */
class WindowsNativeImage implements NativeImage {

	/**
	* Windows to create a bitmap.
	* @param width width of the bitmap.
	* @param height the height of the bitmap.
	* @return the handle of a bitmap that you create.
	 */
	private static Pointer createNative(final int width, final int height) {

		final BITMAPINFOHEADER bmi = new BITMAPINFOHEADER();
		bmi.biSize = 40;
		bmi.biWidth = width;
		bmi.biHeight = height;
		bmi.biPlanes = 1;
		bmi.biBitCount = 32;

		final Pointer hBitmap = Gdi32.INSTANCE.CreateDIBSection(
				Pointer.NULL, bmi, Gdi32.DIB_RGB_COLORS, Pointer.NULL, Pointer.NULL, 0 );

		return hBitmap;
	}

	/**
	 * {@link BufferedImage} to reflect the contents of the bitmap.
	 * @param nativeHandle bitmap handle.
	 * @param rgb ARGB of the picture.
	 */
	private static void flushNative(final Pointer nativeHandle, final int[] rgb, final int scaling) {

		final BITMAP bmp = new BITMAP();
		Gdi32.INSTANCE.GetObjectW(nativeHandle, Main.getInstance().getPlatform().getBitmapSize() + Native.POINTER_SIZE, bmp);

		// Copy at the pixel level. These dimensions are already scaled
		int width = bmp.bmWidth;
		int height = bmp.bmHeight;
		final int destPitch = ((bmp.bmWidth*bmp.bmBitsPixel)+31)/32*4;
		int destIndex = destPitch*(height-1);
		int srcColIndex = 0;
		int srcRowIndex = 0;
                
		for( int y = 0; y < height; ++y )
		{
			for( int x = 0; x<width; ++x )
			{
				// UpdateLayeredWindow and Photoshop are incompatible ?Irashii
				// UpdateLayeredWindow FFFFFF RGB value has the bug that it ignores the value of a,
				// Photoshop is where a is an RGB value of 0 have the property value to 0.

				bmp.bmBits.setInt(destIndex + x*4,
					(rgb[srcColIndex / scaling]&0xFF000000)==0 ? 0 : rgb[srcColIndex / scaling] );

				++srcColIndex;
			}

			destIndex -= destPitch;
                        
                        // resets the srcColIndex to re-use the same indexes and stretch horizontally
                        ++srcRowIndex;
                        if( srcRowIndex != scaling )
                        {
                            srcColIndex -= width;
                        }
                        else
                        {
                            srcRowIndex = 0;
                        }
		}

	}

	/**
	* Windows to open a bitmap.
	* @param nativeHandle bitmap handle.
	 */
	private static void freeNative(final Pointer nativeHandle) {
		Gdi32.INSTANCE.DeleteObject(nativeHandle);
	}

	/**
	 * Java Image object.
	 */
	private final BufferedImage managedImage;

	/**
	 * Windows Bittomappuhandoru.
	 */
	private final Pointer nativeHandle;

	public WindowsNativeImage(final BufferedImage image) {
		int scaling = Integer.parseInt( Main.getInstance( ).getProperties( ).getProperty( "Scaling", "1" ) );
		boolean filter = scaling > 1 && Boolean.parseBoolean(Main.getInstance().getProperties().getProperty("Filter", "false"));
		//scale>1 && filtering on
		int effectiveScaling = filter ? 1 : scaling;

	    this.managedImage = image;
	    this.nativeHandle = createNative(this.getManagedImage().getWidth() * scaling, this.getManagedImage().getHeight() * scaling);

		int[] rbgValues = new int[ this.getManagedImage().getWidth() * this.getManagedImage().getHeight() * effectiveScaling * effectiveScaling ];
		this.getManagedImage().getRGB(0, 0, this.getManagedImage().getWidth(), this.getManagedImage().getHeight(), rbgValues, 0, this.getManagedImage().getWidth());

		// apply filter here
		if (filter)
		{
			int width = this.getManagedImage().getWidth();
			int height = this.getManagedImage().getHeight();
			int[] buffer;

			if( scaling == 4 || scaling == 8 )
			{
				width *= 4;
				height *= 4;
				buffer = new int[ width * height ];
				Hqx_4x.hq4x_32_rb( rbgValues, buffer, width / 4, height / 4 );
				rbgValues = buffer;
			}
			if( scaling == 3 || scaling == 6 )
			{
				width *= 3;
				height *= 3;
				buffer = new int[ width * height ];
				Hqx_3x.hq3x_32_rb( rbgValues, buffer, width / 3, height / 3 );
				rbgValues = buffer;
			}
			if( scaling == 2 )
			{
				width *= 2;
				height *= 2;
				buffer = new int[ width * height ];
				Hqx_2x.hq2x_32_rb( rbgValues, buffer, width / 2, height / 2 );
				rbgValues = buffer;
			}

			if( scaling > 4 ) {
				effectiveScaling = 2;
			}
		}

		flushNative(this.getNativeHandle(), rbgValues, effectiveScaling);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		freeNative(this.getNativeHandle());
	}

	/**
	 * Changes to be reflected in the Windows bitmap image.
	 */
	public void update( ) {
		// this isn't used
	}

	public void flush() {
		this.getManagedImage().flush();
	}

	public Pointer getHandle() {
		return this.getNativeHandle();
	}

	public Graphics getGraphics() {
		return this.getManagedImage().createGraphics();
	}

	public int getHeight() {
		return this.getManagedImage().getHeight();
	}

	public int getWidth() {
		return this.getManagedImage().getWidth();
	}

	public int getHeight(final ImageObserver observer) {
		return this.getManagedImage().getHeight(observer);
	}

	public Object getProperty(final String name, final ImageObserver observer) {
		return this.getManagedImage().getProperty(name, observer);
	}

	public ImageProducer getSource() {
		return this.getManagedImage().getSource();
	}

	public int getWidth(final ImageObserver observer) {
		return this.getManagedImage().getWidth(observer);
	}

	private BufferedImage getManagedImage() {
		return this.managedImage;
	}

	private Pointer getNativeHandle() {
		return this.nativeHandle;
	}
}
