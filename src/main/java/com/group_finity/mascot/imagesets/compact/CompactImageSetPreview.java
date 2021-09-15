package com.group_finity.mascot.imagesets.compact;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Holds the data needed to for the menu to display an imageSet panel
 */
public class CompactImageSetPreview {

    private JPanel panel;
    private JLabel label = new JLabel();
    private final String name;

    private static final int nameTrim = 40;
    private static final int THUMB_SIZE = 60;
    private static final int PANEL_H = 70;
    private static final int PANEL_W = 400;


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


    private JPanel createJpanel(String imageSet) {
        var component = new JPanel();
        component.setPreferredSize(new Dimension(CompactImageSetPreview.PANEL_W, CompactImageSetPreview.PANEL_H));
        component.setLayout(new BoxLayout(component, BoxLayout.LINE_AXIS));


        // get icon
        BufferedImage icon = null;
        try {
            icon = ImageIO.read(new File("./img/" + imageSet + "/icon.png"));
        } catch (IOException e) {
            try {
                icon = ImageIO.read(new File("./img/" + imageSet + "/shime1.png"));
            } catch (IOException ioException) {
                // just ignore it
            }
        }

        component.add(Box.createRigidArea(new Dimension(4, 1)));

        if (icon != null) {
            icon = makeThumbnail(icon, CompactImageSetPreview.THUMB_SIZE);
            component.add(new JLabel(new ImageIcon(icon)));
            component.add(Box.createRigidArea(new Dimension(8, 1)));
        }

        //trims name
        String trimmedImageSet =
                imageSet.length() > CompactImageSetPreview.nameTrim ?
                        imageSet.substring(0, CompactImageSetPreview.nameTrim - 3) + "..." : imageSet;

        label.setText(trimmedImageSet);
        component.add(label);

        return component;
    }

    // https://stackoverflow.com/questions/15558202/
    private static BufferedImage makeThumbnail(BufferedImage imageToScale, int size) {
        BufferedImage scaledImage = null;
        if (imageToScale != null) {
            scaledImage = new BufferedImage(size, size, imageToScale.getType());
            Graphics2D graphics2D = scaledImage.createGraphics();
            graphics2D.drawImage(imageToScale, 0, 0, size, size, null);
            graphics2D.dispose();
        }
        return scaledImage;
    }

}
