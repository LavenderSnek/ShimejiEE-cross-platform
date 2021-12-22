package com.group_finity.mascotnative.macjni;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;
import com.group_finity.mascot.ui.contextmenu.TopLevelMenuRep;

import java.awt.Rectangle;
import java.util.Objects;
import java.util.function.Supplier;

// might need to make a base jni translucent window eventually for x11

class MacJniShimejiWindow implements TranslucentWindow {

    private static native void setImageForShimejiWindow(long shimejiWindowPtr, long nsImagePtr);
    private static native void repaintShimejiWindow(long shimejiWindowPtr);

    private static native void setJavaBoundsForNSWindow(long nsWindowPtr, int x, int y, int width, int height);
    //private static native Rectangle getJavaBoundsForNSWindow(long nsWindowPtr);

    private static native void setVisibilityForNSWindow(long nsWindowPtr, boolean visible);
    //private static native boolean getVisibilityForNSWindow(long nsWindowPtr);

    private static native void disposeShimejiWindow(long shimejiWindowPtr);

    //--- class ---//

    /**
     * Creates a native ShimejiWindow with this object as its callback object.
     * @return ShimejiWindow pointer
     */
    private native long createNativeShimejiWindow();

    private final long ptr;

    private MacJniNativeImage currentImage = null;
    private Rectangle lastSetBounds = new Rectangle();
    private boolean lastSetVisibility = false;

    private Runnable leftMousePressedAction = () -> {};
    private Runnable leftMouseReleasedAction = () -> {};
    private Supplier<TopLevelMenuRep> popupMenuSupplier = null;

    MacJniShimejiWindow() {
        this.ptr = createNativeShimejiWindow();
    }

    @Override
    public void setLeftMousePressedAction(Runnable leftMousePressedAction) {
        this.leftMousePressedAction = Objects.requireNonNullElse(leftMousePressedAction, () -> {});
    }

    @Override
    public void setLeftMouseReleasedAction(Runnable leftMouseReleasedAction) {
        this.leftMouseReleasedAction = Objects.requireNonNullElse(leftMouseReleasedAction, () -> {});
    }

    @Override
    public void setPopupMenuSupplier(Supplier<TopLevelMenuRep> popupMenuSupplier) {
        this.popupMenuSupplier = popupMenuSupplier;
    }

    @Override
    public void setImage(NativeImage image) {
        currentImage = (MacJniNativeImage) image;
        setImageForShimejiWindow(ptr, currentImage.getNsImagePtr());
    }

    @Override
    public void updateImage() {
        repaintShimejiWindow(ptr);
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
        disposeShimejiWindow(ptr);
    }

    //--- native callbacks---//

    private void _onLeftMouseDown() {
        leftMousePressedAction.run();
    }

    private void _onLeftMouseUp() {
        leftMouseReleasedAction.run();
    }

    private long _getNSMenuPtrForPopup() {
        // TODO: write native popups
        return 0L;
    }

}
