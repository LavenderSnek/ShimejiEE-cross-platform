package com.group_finity.mascot.window;

import com.group_finity.mascot.image.NativeImage;

import java.awt.Rectangle;

public interface TranslucentWindow {

    // event

    void setEventHandler(TranslucentWindowEventHandler eventHandler);

    // image

    void setImage(NativeImage image);

    void updateImage();

    // window

    Rectangle getBounds();

    void setBounds(Rectangle r);

    boolean isVisible();

    void setVisible(boolean b);

    void dispose();

}
