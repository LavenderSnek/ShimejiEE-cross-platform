package com.group_finity.mascotnative.shared;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

public class ImageUtil {

    public static BufferedImage newBufferedImage(Path path, double scaling, boolean flipped, boolean antialiasing) throws IOException {
        var img = ImageIO.read(path.toFile());
        return transform(img, scaling, flipped, antialiasing);
    }

    private static BufferedImage transform(BufferedImage src, double scaleFactor, boolean flip, boolean antialiasing) {
        final int fWidth = (int) Math.round(src.getWidth() * scaleFactor);
        final int fHeight = (int) Math.round(src.getHeight() * scaleFactor);

        final BufferedImage copy = new BufferedImage(fWidth, fHeight, BufferedImage.TYPE_INT_ARGB_PRE);

        Graphics2D g2d = copy.createGraphics();
        var renderHint = antialiasing
                ? RenderingHints.VALUE_INTERPOLATION_BICUBIC
                : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, renderHint);
        g2d.drawImage(src, (flip ? fWidth : 0), 0, (flip ? -fWidth : fWidth), fHeight, null);
        g2d.dispose();

        return copy;
    }

}
