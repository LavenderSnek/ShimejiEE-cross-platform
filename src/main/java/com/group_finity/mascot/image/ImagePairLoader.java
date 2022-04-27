package com.group_finity.mascot.image;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.NativeFactory;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

public class ImagePairLoader {

    /**
     * Loads a pair of images into {@link ImagePairs} if it isn't already loaded
     *
     * @param leftName      Path to the image
     * @param rightName Path to the right facing version of the image, flipped left image will be used if this is null
     * @param anchor    Anchor point of the image
     * @param scaling   amount of scaling applied to the image, also affects anchor paint
     *
     * @return The key with which the loaded image pair can be accessed with {@link ImagePairs#getImagePair(String)}
     */
    public static String load(String imageSetName, final String leftName, final String rightName, final Point anchor, final double scaling) throws IOException {
        String identifier =
                Path.of(imageSetName, leftName) +
                (rightName == null ? "" : (":" + Path.of(imageSetName, rightName)));

        if (ImagePairs.contains(identifier)) {
            return identifier;
        }

        Path imgSetFolder = Main.getInstance().getProgramFolder().imgPath().resolve(imageSetName).toAbsolutePath();

        Path leftPath = Path.of(imgSetFolder.toString(), leftName); // ignores the leading slashes
        Path rightPath = rightName == null ? null : Path.of(imgSetFolder.toString(), rightName);

        ImagePair ip = createImagePair(leftPath, rightPath, anchor, scaling);
        ImagePairs.load(identifier, ip);

        return identifier;
    }

    private static ImagePair createImagePair(Path leftImgPath, Path rightImgPath, Point rawAnchor, double scaling) throws IOException {

        BufferedImage leftImg = transform(ImageIO.read(leftImgPath.toFile()), scaling, false);
        BufferedImage rightImg = rightImgPath == null
                ? transform(leftImg, 1, true)
                : transform(ImageIO.read(rightImgPath.toFile()), scaling, false);

        final Point scaledAnchor = new Point(
                (int) Math.round(rawAnchor.x * scaling),
                (int) Math.round(rawAnchor.y * scaling)
        );

        // todo: native scaling
        MascotImage lMascot = new MascotImage(
                NativeFactory.getInstance().newNativeImage(leftImg, 1),
                scaledAnchor,
                new Dimension(leftImg.getWidth(), leftImg.getHeight())
        );

        MascotImage rMascot = new MascotImage(
                NativeFactory.getInstance().newNativeImage(rightImg, 1),
                new Point(rightImg.getWidth() - scaledAnchor.x, scaledAnchor.y),
                new Dimension(rightImg.getWidth(), rightImg.getHeight())
        );

        return new ImagePair(lMascot, rMascot);
    }

    private static BufferedImage transform(BufferedImage src, double scaleFactor, boolean flip) {
        final int fWidth = (int) Math.round(src.getWidth() * scaleFactor);
        final int fHeight = (int) Math.round(src.getHeight() * scaleFactor);

        final BufferedImage copy = new BufferedImage(fWidth, fHeight, BufferedImage.TYPE_INT_ARGB_PRE);

        Graphics2D g2d = copy.createGraphics();
        var renderHint = scaleFactor <= 1
                ? RenderingHints.VALUE_INTERPOLATION_BICUBIC
                : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, renderHint);
        g2d.drawImage(src, (flip ? fHeight : 0), 0, (flip ? -fWidth : fWidth), fHeight, null);
        g2d.dispose();

        return copy;
    }

}

