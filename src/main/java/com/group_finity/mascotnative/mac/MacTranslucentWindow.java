package com.group_finity.mascotnative.mac;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;

import javax.swing.JPanel;
import javax.swing.JWindow;
import java.awt.Color;
import java.awt.Graphics;

/**
 * Can't pass mouse clicks through the transparent areas like other platforms
 *
 * @see <a href="https://bugs.openjdk.java.net/browse/JDK-8013450">Bug [JDK-8013450]</a>
 */
class MacTranslucentWindow extends JWindow implements TranslucentWindow {

    private static final Color CLEAR = new Color(0, 0, 0, 0);

    private boolean imageChanged = false;

    private MacNativeImage currentImage;

    MacTranslucentWindow() {
        super();

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(final Graphics g) {
                g.drawImage(currentImage.getManagedImage(), 0, 0, null);
            }
        };

        setBackground(CLEAR);
        panel.setBackground(CLEAR);

        setContentPane(panel);

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
        imageChanged = (currentImage != null && image != currentImage);
        currentImage = (MacNativeImage) image;
    }

    @Override
    public void updateImage() {
        if (this.imageChanged) {
            getContentPane().repaint();
            imageChanged = false;
        }
    }

}
