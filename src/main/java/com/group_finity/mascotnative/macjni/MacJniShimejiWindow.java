package com.group_finity.mascotnative.macjni;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.ui.contextmenu.TopLevelMenuRep;
import com.group_finity.mascot.window.TranslucentWindow;
import com.group_finity.mascot.window.TranslucentWindowEvent;
import com.group_finity.mascot.window.TranslucentWindowEventHandler;
import com.group_finity.mascotnative.macjni.menu.MacJniPopupUtil;

import java.awt.Point;
import java.awt.Rectangle;

// might need to make a base jni translucent window eventually for x11

class MacJniShimejiWindow implements TranslucentWindow {

    private static native void setImageForShimejiWindow(long shimejiWindowPtr, long nsImagePtr);
    private static native void setJavaBoundsForNSWindow(long nsWindowPtr, int x, int y, int width, int height);
    private static native void setVisibilityForNSWindow(long nsWindowPtr, boolean visible);
    private static native void disposeShimejiWindow(long shimejiWindowPtr);

    //--- class ---//

    /**
     * Creates a native ShimejiWindow with this object as its callback object.
     *
     * @return ShimejiWindow pointer
     */
    private native long createNativeShimejiWindow();

    private final long ptr;

    private MacJniNativeImage currentImage = null;
    private Rectangle lastSetBounds = new Rectangle();
    private boolean lastSetVisibility = false;

    private TranslucentWindowEventHandler eventHandler = TranslucentWindowEventHandler.DEFAULT;

    MacJniShimejiWindow() {
        this.ptr = createNativeShimejiWindow();
    }

    @Override
    public void setEventHandler(TranslucentWindowEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @Override
    public void setImage(NativeImage image) {
        if (image != null) {
            currentImage = (MacJniNativeImage) image;
        }
    }

    @Override
    public void updateImage() {
        setImageForShimejiWindow(ptr, currentImage.getNsImagePtr());
    }

    @Override
    public Rectangle getBounds() {
        return lastSetBounds;
    }

    @Override
    public void setBounds(Rectangle r) {
        setJavaBoundsForNSWindow(ptr, r.x, r.y, r.width, r.height);
        lastSetBounds = r;
    }

    @Override
    public boolean isVisible() {
        return lastSetVisibility;
    }

    @Override
    public void setVisible(boolean b) {
        setVisibilityForNSWindow(ptr, b);
        lastSetVisibility = b;
    }

    @Override
    public void dispose() {
        setVisible(false);
        disposeShimejiWindow(ptr);
    }

    //--- native callbacks---//

    @SuppressWarnings("unused")
    private void _onLeftMouseDown(int relX, int relY) {
        eventHandler.onDragBegin(new TranslucentWindowEvent(new Point(relX, relY)));
    }

    @SuppressWarnings("unused")
    private void _onLeftMouseUp(int relX, int relY) {
        eventHandler.onDragEnd(new TranslucentWindowEvent(new Point(relX, relY)));
    }

    @SuppressWarnings("unused")
    private long _getNSMenuPtrForPopup() {
        TopLevelMenuRep topLevelMenuRep = eventHandler.getContextMenuRep();
        if (topLevelMenuRep != null) {
            return MacJniPopupUtil.createNSMenuFor(topLevelMenuRep);
        }
        return 0;
    }

}
