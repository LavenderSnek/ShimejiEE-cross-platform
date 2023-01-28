package com.group_finity.mascot.ui.imagesets;

import com.group_finity.mascot.Main;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Holds the data needed to for the menu to display an imageSet panel
 */
public class CompactImageSetPreview {

    private JPanel panel;
    private final String name;

    private static final int nameTrim = 40;
    private static final int THUMB_SIZE = 60;
    private static final int PANEL_H = 70;
    private static final int PANEL_W = 400;
    private static final BufferedImage DEFAULT_IMG = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);

    CompactImageSetPreview(String imageSet) {
        this.name = imageSet;
    }

    @Override
    public String toString() {
        return name;
    }

    JPanel getPanel() {
        if (panel == null) {
            this.panel = createJpanel(name);
        }
        return panel;
    }

    private static JPanel createJpanel(String imageSet) {
        var component = new JPanel();
        component.setPreferredSize(new Dimension(CompactImageSetPreview.PANEL_W, CompactImageSetPreview.PANEL_H));
        component.setLayout(new BoxLayout(component, BoxLayout.LINE_AXIS));

        // get icon
        Path iconPath = Main.getInstance().getProgramFolder().getIconPathForImageSet(imageSet);
        BufferedImage icon = null;
        if (iconPath != null) {
            try {
                icon = ImageIO.read(iconPath.toFile());
            } catch (IOException ignored) {
            }
        }

        if (icon == null) {
            icon = DEFAULT_IMG;
        }

        component.add(Box.createRigidArea(new Dimension(4, 1)));

        icon = makeThumbnail(icon, CompactImageSetPreview.THUMB_SIZE);
        component.add(new JLabel(new ImageIcon(icon)));
        component.add(Box.createRigidArea(new Dimension(8, 1)));

        //trims name
        String trimmedImageSet =
                imageSet.length() > CompactImageSetPreview.nameTrim ?
                        imageSet.substring(0, CompactImageSetPreview.nameTrim - 3) + "..." : imageSet;

        JLabel label = new JLabel(trimmedImageSet);
        component.add(label);

        return component;
    }

    // https://stackoverflow.com/questions/15558202/
    private static BufferedImage makeThumbnail(BufferedImage imageToScale, int size) {
        BufferedImage scaledImage = null;
        if (imageToScale != null) {
            scaledImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB_PRE); // https://github.com/usnistgov/pyramidio/issues/7
            Graphics2D graphics2D = scaledImage.createGraphics();
            graphics2D.drawImage(imageToScale, 0, 0, size, size, null);
            graphics2D.dispose();
        }
        return scaledImage;
    }

}
