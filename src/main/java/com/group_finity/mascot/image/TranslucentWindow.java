package com.group_finity.mascot.image;

import javax.swing.JWindow;
import java.awt.Rectangle;

public interface TranslucentWindow {

    /**
     * The window the shimeji is drawn on
     */
    JWindow asJWindow();

    void setImage(NativeImage image);

    void updateImage();

    Rectangle getBounds();

    void setBounds(Rectangle r);

    boolean isVisible();

    void setVisible(boolean b);

    void dispose();

}
