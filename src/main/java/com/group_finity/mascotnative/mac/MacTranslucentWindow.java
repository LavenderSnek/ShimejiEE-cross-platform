package com.group_finity.mascotnative.mac;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;

import javax.swing.JPanel;
import javax.swing.JWindow;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * Can't pass mouse clicks through the transparent areas like other platforms
 *
 * @see <a href="https://bugs.openjdk.java.net/browse/JDK-8013450">Bug [JDK-8013450]</a>
 */
class MacTranslucentWindow extends JWindow implements TranslucentWindow {

    private static final MacNativeImage START_IMAGE = new MacNativeImage(new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB));

    private boolean imageChanged = false;

    private MacNativeImage currentImage;
    private MacNativeImage nextImage;

    MacTranslucentWindow() {
        super();

        setBackground(new Color(0, 0, 0, 0));

        currentImage = START_IMAGE;

        setContentPane(new JPanel() {
            @Override
            protected void paintComponent(final Graphics g) {
                g.drawImage(currentImage.getManagedImage(), 0, 0, null);
            }
        });

        //so it won't interfere with any custom shadows the user wants to add
        getRootPane().putClientProperty("Window.shadow", Boolean.FALSE);

        // it gets rid of the 'flickering' when dragging,
        getRootPane().putClientProperty("apple.awt.draggableWindowBackground", Boolean.FALSE);
    }

    @Override
    public JWindow asJWindow() {
        return this;
    }

    @Override
    public void setImage(NativeImage image) {
        this.imageChanged = (this.currentImage != null && image != currentImage);
        nextImage = (MacNativeImage) image;
    }

    @Override
    public void updateImage() {
        if (this.imageChanged) {
            currentImage = nextImage;
            this.repaint();
            this.imageChanged = false;
        }
    }

}
