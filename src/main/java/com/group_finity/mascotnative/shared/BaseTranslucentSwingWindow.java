package com.group_finity.mascotnative.shared;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;
import com.group_finity.mascot.ui.contextmenu.TopLevelMenuRep;

import javax.swing.JPopupMenu;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Base class for Swing based shimeji rendering
 * @param <T> The type of native image used by the window
 */
public abstract class BaseTranslucentSwingWindow<T extends NativeImage> extends JWindow implements TranslucentWindow {

    protected static final Color CLEAR = new Color(0, 0, 0, 0);

    private Supplier<TopLevelMenuRep> popupMenuSupplier = null;
    protected Runnable leftMousePressedAction = () -> {};
    protected Runnable leftMouseReleasedAction = () -> {};

    private T image;
    private boolean imageChanged = false;

    protected BaseTranslucentSwingWindow() {
        super();
        setUp();
        addMouseListeners();
    }

    protected abstract void setUp();

    @Override
    public void setLeftMousePressedAction(Runnable leftMousePressedAction) {
        this.leftMousePressedAction = Objects.requireNonNullElse(leftMousePressedAction, ()->{});
    }

    @Override
    public void setLeftMouseReleasedAction(Runnable leftMouseReleasedAction) {
        this.leftMouseReleasedAction = Objects.requireNonNullElse(leftMouseReleasedAction, ()->{});
    }

    @Override
    public void setPopupMenuSupplier(Supplier<TopLevelMenuRep> popupMenuSupplier) {
        this.popupMenuSupplier = popupMenuSupplier;
    }

    protected void addMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    showPopupMenu(e);
                }
                else if (SwingUtilities.isLeftMouseButton(e)) {
                    leftMousePressedAction.run();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    leftMouseReleasedAction.run();
                }
            }
        });
    }

    protected void showPopupMenu(MouseEvent e) {
        if (popupMenuSupplier == null) {
            return;
        }
        TopLevelMenuRep menuRep = popupMenuSupplier.get();
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
