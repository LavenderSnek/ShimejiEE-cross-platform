package com.group_finity.mascotnative.generic;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;
import com.sun.jna.platform.WindowUtils;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JWindow;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

class GenericTranslucentWindow extends JWindow implements TranslucentWindow {

    private static final Color CLEAR = new Color(0, 0, 0, 0);

    private GenericNativeImage image;

    private JPanel panel;

    public GenericTranslucentWindow() {
        super();
        WindowUtils.setWindowTransparent(this, true);

        this.panel = new JPanel() {
            @Override
            protected void paintComponent(final Graphics g) {
                g.drawImage(image.getManagedImage(), 0, 0, null);
            }
        };

        panel.setBackground(CLEAR);
        setBackground(CLEAR);

        this.setContentPane(this.panel);

        setAlwaysOnTop(true);
    }

    @Override
    public void setVisible(final boolean b) {
        super.setVisible(b);
    }

    @Override
    protected void addImpl(final Component comp, final Object constraints, final int index) {
        super.addImpl(comp, constraints, index);
        if (comp instanceof final JComponent jcomp) {
            jcomp.setOpaque(false);
        }
    }

    @Override
    public JWindow asJWindow() {
        return this;
    }

    public void setImage(final NativeImage image) {
        this.image = (GenericNativeImage) image;
    }

    public void updateImage() {
        WindowUtils.setWindowMask(this, image.getIcon());
        validate();
        this.repaint();
    }

}
