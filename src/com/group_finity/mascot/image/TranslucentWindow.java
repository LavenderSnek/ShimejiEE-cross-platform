package com.group_finity.mascot.image;

import javax.swing.JWindow;

public interface TranslucentWindow {

    /**
     * The window the shimeji is drawn on
     */
    JWindow asJWindow();

    void setImage(NativeImage image);

    void updateImage();

}
