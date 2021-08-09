package com.group_finity.mascot.mac;

import com.group_finity.mascot.NativeFactory;
import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;

import javax.swing.JRootPane;
import javax.swing.JWindow;

/**
 * @author nonowarn
 */
class MacTranslucentWindow implements TranslucentWindow {

    private TranslucentWindow delegate;

    private boolean imageChanged = false;
    private NativeImage oldImage = null;

    MacTranslucentWindow(NativeFactory factory) {
        delegate = factory.newTransparentWindow();
        JRootPane rootPane = delegate.asJWindow().getRootPane();

        //so it won't interfere with any custom shadows the user wants to add
        rootPane.putClientProperty("Window.shadow", Boolean.FALSE);

        // it gets rid of the 'flickering' when dragging,
        // im guessing that the java dragging clashes with the native
        rootPane.putClientProperty("apple.awt.draggableWindowBackground", Boolean.FALSE);
    }

    @Override
    public JWindow asJWindow() {
        return delegate.asJWindow();
    }

    @Override
    public void setImage(NativeImage image) {
        this.imageChanged = (this.oldImage != null && image != oldImage);
        this.oldImage = image;
        delegate.setImage(image);
    }

    @Override
    public void updateImage() {
        if (this.imageChanged) {
            delegate.updateImage();
            this.imageChanged = false;
        }
    }

}
