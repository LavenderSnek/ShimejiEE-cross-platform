package com.group_finity.mascot.window;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.ui.contextmenu.TopLevelMenuRep;

import java.awt.Rectangle;
import java.util.function.Supplier;

public interface TranslucentWindow {

    // event

    void setLeftMousePressedAction(Runnable action);

    void setLeftMouseReleasedAction(Runnable action);

    void setPopupMenuSupplier(Supplier<TopLevelMenuRep> popupMenuSupplier);

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
