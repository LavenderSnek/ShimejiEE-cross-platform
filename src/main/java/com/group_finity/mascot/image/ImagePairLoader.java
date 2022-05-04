package com.group_finity.mascot.image;

import com.group_finity.mascot.NativeFactory;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImagePairLoader implements ImagePairStore {

    private final Map<String, ImagePair> imagePairs = new ConcurrentHashMap<>(64, 0.75f, 2);

    private final double scaling;
    private final boolean logicalAnchors;
    private final boolean asymmetryNameScheme;
    private final boolean pixelArtScaling;

    private final Path basePath;

    ImagePairLoader(ImagePairLoaderBuilder builder, Path basePath) {
        this.scaling = builder.scaling;
        this.logicalAnchors = builder.logicalAnchors;
        this.asymmetryNameScheme = builder.asymmetryNameScheme;
        this.pixelArtScaling = builder.pixelArtScaling;
        this.basePath = basePath;
    }

    @Override
    public String load(String imageText, String imageRightText, Point anchor) throws IOException {
        String errorText = "image=" + imageText + (imageRightText == null ? "" : ", imageRight=" + imageRightText) + ", anchor=" + anchor;
        if (anchor == null) {
            throw  new IOException("Invalid/Missing image anchor: " + errorText);
        }
        if (!imageText.startsWith("/") || (imageRightText != null && !imageRightText.startsWith("/"))) {
            throw new IOException("Image text must start with '/' (slash) for compatibility: " + errorText);
        }

        imageText = imageText.replaceAll("^/+", "");

        if (imageRightText != null) {
            imageRightText = imageRightText.replaceAll("^/+", "");
        }
        else if (asymmetryNameScheme) {
            String possibleImgRight = imageText.replaceAll("\\.[a-zA-Z]+$", "-r$0");
            if (Files.isRegularFile(basePath.resolve(possibleImgRight))) {
                imageRightText = possibleImgRight;
            }
        }

        String key = imageText + (imageRightText == null ? "" : ":" + imageRightText);
        if (logicalAnchors) {
            key = anchor.x + "," + anchor.y + ":" + key;
        }

        if (imagePairs.containsKey(key)) {
            return key;
        }

        Path leftPath = basePath.resolve(imageText);
        Path rightPath = imageRightText == null ? null : basePath.resolve(imageRightText);

        ImagePair ip = createImagePair(leftPath, rightPath, anchor, getScaling());
        imagePairs.put(key, ip);

        return key;
    }

    @Override
    public ImagePair get(String key) {
        return key == null ? null : imagePairs.get(key);
    }

    @Override
    public double getScaling() {
        return scaling;
    }

    protected ImagePair createImagePair(Path leftImgPath, Path rightImgPath, Point rawAnchor, double scaling) throws IOException {
        BufferedImage leftImg = transform(ImageIO.read(leftImgPath.toFile()), scaling, false);
        BufferedImage rightImg = rightImgPath == null
                ? transform(leftImg, 1, true)
                : transform(ImageIO.read(rightImgPath.toFile()), scaling, false);

        final Point scaledAnchor = new Point(
                (int) Math.round(rawAnchor.x * scaling),
                (int) Math.round(rawAnchor.y * scaling)
        );

        MascotImage lMascot = new MascotImage(
                NativeFactory.getInstance().newNativeImage(leftImg),
                scaledAnchor,
                new Dimension(leftImg.getWidth(), leftImg.getHeight())
        );

        MascotImage rMascot = new MascotImage(
                NativeFactory.getInstance().newNativeImage(rightImg),
                new Point(rightImg.getWidth() - scaledAnchor.x, scaledAnchor.y),
                new Dimension(rightImg.getWidth(), rightImg.getHeight())
        );

        return new ImagePair(lMascot, rMascot);
    }

    protected BufferedImage transform(BufferedImage src, double scaleFactor, boolean flip) {
        final int fWidth = (int) Math.round(src.getWidth() * scaleFactor);
        final int fHeight = (int) Math.round(src.getHeight() * scaleFactor);

        final BufferedImage copy = new BufferedImage(fWidth, fHeight, BufferedImage.TYPE_INT_ARGB_PRE);

        Graphics2D g2d = copy.createGraphics();
        var renderHint = pixelArtScaling
                ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
                : RenderingHints.VALUE_INTERPOLATION_BICUBIC;

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, renderHint);
        g2d.drawImage(src, (flip ? fHeight : 0), 0, (flip ? -fWidth : fWidth), fHeight, null);
        g2d.dispose();

        return copy;
    }

}

