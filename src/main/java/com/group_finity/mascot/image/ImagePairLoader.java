package com.group_finity.mascot.image;

import com.group_finity.mascot.Main;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

public class ImagePairLoader {

    /**
     * Loads a pair of images into {@link ImagePairs#imagepairs} if it isn't already loaded
     *
     * @param leftName      Path to the image
     * @param rightName Path to the right facing version of the image, flipped left image will be used if this is null
     * @param anchor    Anchor point of the image
     * @param scaling   amount of scaling applied to the image, also affects anchor paint
     *
     * @return The key with which the loaded image pair can be accessed with {@link ImagePairs#getImagePair(String)}
     */
    public static String load(String imageSetName, final String leftName, final String rightName, final Point anchor, final int scaling) throws IOException {
        String identifier =
                Path.of(imageSetName, leftName) +
                (rightName == null ? "" : (":" + Path.of(imageSetName, rightName)));

        if (ImagePairs.contains(identifier)) {
            return identifier;
        }

        Path imgSetFolder = Main.getInstance().getProgramFolder().imgPath().resolve(imageSetName).toAbsolutePath();

        Path leftImagePath = Path.of(imgSetFolder.toString(), leftName); // ignores the leading slashes
        final BufferedImage leftImage = premultiply(ImageIO.read(leftImagePath.toFile()));
        final BufferedImage rightImage;
        if (rightName == null) {
            rightImage = flip(leftImage);
        } else {
            Path rightImagePath = Path.of(imgSetFolder.toString(), rightName);
            rightImage = premultiply(ImageIO.read(rightImagePath.toFile()));
        }

        ImagePair ip = new ImagePair(
                new MascotImage(leftImage, new Point(anchor.x * scaling, anchor.y * scaling)),
                new MascotImage(rightImage, new Point((rightImage.getWidth() - anchor.x) * scaling, anchor.y * scaling))
        );

        ImagePairs.load(identifier, ip);

        return identifier;
    }


    private static BufferedImage flip(final BufferedImage src) {

        final BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(),
                src.getType() == BufferedImage.TYPE_CUSTOM ? BufferedImage.TYPE_INT_ARGB : src.getType());

        for (int y = 0; y < src.getHeight(); ++y) {
            for (int x = 0; x < src.getWidth(); ++x) {
                copy.setRGB(copy.getWidth() - x - 1, y, src.getRGB(x, y));
            }
        }
        return copy;
    }

    private static BufferedImage premultiply(final BufferedImage source) {
        final BufferedImage returnImage = new BufferedImage(source.getWidth(), source.getHeight(),
                source.getType() == BufferedImage.TYPE_CUSTOM ? BufferedImage.TYPE_INT_ARGB_PRE : source.getType());
        Color colour;
        float[] components;

        for (int y = 0; y < returnImage.getHeight(); ++y) {
            for (int x = 0; x < returnImage.getWidth(); ++x) {
                colour = new Color(source.getRGB(x, y), true);
                components = colour.getComponents(null);
                components[0] = components[3] * components[0];
                components[1] = components[3] * components[1];
                components[2] = components[3] * components[2];
                colour = new Color(components[0], components[1], components[2], components[3]);
                returnImage.setRGB(x, y, colour.getRGB());
            }
        }

        return returnImage;
    }

}

