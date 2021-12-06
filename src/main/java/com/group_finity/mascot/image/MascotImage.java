package com.group_finity.mascot.image;

import java.awt.Dimension;
import java.awt.Point;

@SuppressWarnings("ClassCanBeRecord")
// this class is exposed to scripting and making it a record might break it (I don't really know)
public class MascotImage {

    private final NativeImage image;
    private final Point center;
    private final Dimension size;

    public MascotImage(final NativeImage image, final Point center, final Dimension size) {
        this.image = image;
        this.center = center;
        this.size = size;
    }

    /**
     * Opaque type containing the data for the native code to render the image.
     */
    public NativeImage getImage() {
        return this.image;
    }

    /**
     * The scaled image anchor, this is where the image touches the environment.
     */
    public Point getCenter() {
        return this.center;
    }

    /**
     * The size of the image after the scaling has been applied.
     * <p>
     * This is value used when setBounds is called on {@link TranslucentWindow}
     */
    public Dimension getSize() {
        return this.size;
    }

}
