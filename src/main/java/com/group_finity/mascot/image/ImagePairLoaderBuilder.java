package com.group_finity.mascot.image;

import java.nio.file.Path;

public class ImagePairLoaderBuilder {

    double scaling = 1;
    boolean logicalAnchors = false;
    boolean asymmetryNameScheme = false;
    boolean pixelArtScaling = false;

    public double getScaling() {
        return scaling;
    }

    /**
     * Sets the scale factor applies to the images and anchors.
     * <p>
     * Scaling to non-whole number might make some image sets look 'off'. If an image looks blurry or pixelated,
     * you may need to use {@link #setPixelArtScaling(boolean)}.
     *
     * @param scaling The scale factor. Must be a number above 0.
     * @throws IllegalArgumentException if the scale factor is less than equal to 0.
     */
    public ImagePairLoaderBuilder setScaling(double scaling) {
        if (scaling <= 0) {
            throw new IllegalArgumentException("Invalid scaling: '" + scaling + "'\n" + "Scaling must be greater than 0.");
        }
        this.scaling = scaling;
        return this;
    }

    public boolean isLogicalAnchors() {
        return logicalAnchors;
    }

    /**
     * Whether different anchors for the same image pairs are loaded.
     * <p>
     * This behavior was the default in the japanese version of shimeji (and how most users expect anchors to work).
     * Try this option if you have issues with drag anchors on old shimeji.
     */
    public ImagePairLoaderBuilder setLogicalAnchors(boolean logicalAnchors) {
        this.logicalAnchors = logicalAnchors;
        return this;
    }

    public boolean isAsymmetryNameScheme() {
        return asymmetryNameScheme;
    }

    /**
     * Uses the '-r' suffix to find a separate ImageRight (if no ImageRight is provided).
     * <p>
     * Old versions of shimeji supported asymmetry by looking for a file with a -r suffix.
     * For example, if '{@code /shime1.png}' was being loaded and '{@code /shime1-r.png}' existed,
     * '{@code /shime1-r.png}' would be used as the right facing image.
     * <p>
     * Keep in mind that ImageRight is given preference even if this option is enabled.
     */
    public ImagePairLoaderBuilder setAsymmetryNameScheme(boolean asymmetryNameScheme) {
        this.asymmetryNameScheme = asymmetryNameScheme;
        return this;
    }

    public boolean isPixelArtScaling() {
        return pixelArtScaling;
    }

    /**
     * Enable this to use nearest neighbour interpolation for scaling.
     */
    public ImagePairLoaderBuilder setPixelArtScaling(boolean pixelArtScaling) {
        this.pixelArtScaling = pixelArtScaling;
        return this;
    }

    public ImagePairLoader buildForBasePath(Path basePath) {
        return new ImagePairLoader(this, basePath);
    }

}