package com.group_finity.mascot.image;

import com.group_finity.mascot.NativeFactory;

import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImagePairLoader implements ImagePairStore {

    private final Map<String, ImagePair> imagePairs = new ConcurrentHashMap<>(64, 0.75f, 2);
    private final List<NativeImage> loadedImageRefs = new ArrayList<>();

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
        if (anchor == null) {
            throw  new IOException("Invalid/Missing image anchor.");
        }
        if (!imageText.startsWith("/") || (imageRightText != null && !imageRightText.startsWith("/"))) {
            throw new IOException("Image text must start with '/' (slash) for compatibility.");
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
    public void disposeAll() {
        imagePairs.clear();

        loadedImageRefs.forEach(NativeImage::dispose);
        loadedImageRefs.clear();
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
        var leftImg = NativeFactory.getInstance().newNativeImage(leftImgPath, scaling, false, !pixelArtScaling);
        var rightImg = rightImgPath == null
                ? NativeFactory.getInstance().newNativeImage(leftImgPath, scaling, true, !pixelArtScaling)
                : NativeFactory.getInstance().newNativeImage(rightImgPath, scaling, false, !pixelArtScaling);

        final Point scaledAnchor = new Point(
                (int) Math.round(rawAnchor.x * scaling),
                (int) Math.round(rawAnchor.y * scaling)
        );

        MascotImage lMascot = new MascotImage(
                leftImg,
                scaledAnchor,
                new Dimension(leftImg.getWidth(), leftImg.getHeight())
        );

        MascotImage rMascot = new MascotImage(
                rightImg,
                new Point(rightImg.getWidth() - scaledAnchor.x, scaledAnchor.y),
                new Dimension(rightImg.getWidth(), rightImg.getHeight())
        );

        return new ImagePair(lMascot, rMascot);
    }

}

