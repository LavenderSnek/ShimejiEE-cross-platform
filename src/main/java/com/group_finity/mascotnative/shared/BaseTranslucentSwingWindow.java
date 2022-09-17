package com.group_finity.mascotnative.shared;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.ui.contextmenu.TopLevelMenuRep;
import com.group_finity.mascot.window.TranslucentWindow;
import com.group_finity.mascot.window.TranslucentWindowEvent;
import com.group_finity.mascot.window.TranslucentWindowEventHandler;

import javax.swing.JPopupMenu;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Base class for Swing based shimeji rendering
 * @param <T> The type of native image used by the window
 */
public abstract class BaseTranslucentSwingWindow<T extends NativeImage> extends JWindow implements TranslucentWindow {

    protected static final Color CLEAR = new Color(0, 0, 0, 0);

    private TranslucentWindowEventHandler eventHandler = TranslucentWindowEventHandler.DEFAULT;

    private T image;
    private boolean imageChanged = false;

    protected BaseTranslucentSwingWindow() {
        super();
        setUp();
        addMouseListeners();
    }

    protected abstract void setUp();

    @Override
    public void setEventHandler(TranslucentWindowEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    protected TranslucentWindowEventHandler getEventHandler() {
        return eventHandler;
    }

    protected void addMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    showPopupMenu(e);
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    getEventHandler().onDragBegin(new TranslucentWindowEvent(e.getPoint()));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    getEventHandler().onDragEnd(new TranslucentWindowEvent(e.getPoint()));
                }
            }
        });
    }

    protected void showPopupMenu(MouseEvent e) {
        TopLevelMenuRep menuRep = getEventHandler().getContextMenuRep();
        if (menuRep == null) {
            return;
        }
        JPopupMenu popupMenu = SwingPopupUtil.createJPopupmenuFrom(menuRep);
        SwingUtilities.invokeLater(() -> popupMenu.show(this, e.getX(), e.getY()));
    }

    protected boolean isImageChanged() {
        return imageChanged;
    }

    protected void setImageChanged(boolean imageChanged) {
        this.imageChanged = imageChanged;
    }

    protected T getImage() {
        return image;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setImage(NativeImage image) {
        setImageChanged(image != null && getImage() != image);
        this.image = (T) image;
    }

    @Override
    public abstract void updateImage();

}
