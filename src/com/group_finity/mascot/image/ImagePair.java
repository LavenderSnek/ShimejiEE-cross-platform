package com.group_finity.mascot.image;

/** A right and left facing pair of images */
public class ImagePair {


	private MascotImage leftImage;
	private MascotImage rightImage;


	public ImagePair(final MascotImage leftImage, final MascotImage rightImage) {
		this.leftImage = leftImage;
		this.rightImage = rightImage;
	}


	public MascotImage getImage(final boolean lookRight) {
		return lookRight ? this.getRightImage() : this.getLeftImage();
	}

	private MascotImage getLeftImage() {
		return this.leftImage;
	}
	
	private MascotImage getRightImage() {
		return this.rightImage;
	}
}
