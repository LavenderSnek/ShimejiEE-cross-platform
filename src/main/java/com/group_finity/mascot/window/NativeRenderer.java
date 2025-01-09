package com.group_finity.mascot.window;

import com.group_finity.mascot.image.NativeImage;

import java.awt.*;

public interface NativeRenderer {

    void createWindow(int id, TranslucentWindowEventHandler callbacks);

    void updateWindow(int id, boolean visible, NativeImage image, Rectangle bounds);

    void disposeWindow(int id);

}
