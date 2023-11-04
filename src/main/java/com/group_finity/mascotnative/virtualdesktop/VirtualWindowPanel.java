package com.group_finity.mascotnative.virtualdesktop;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.ui.contextmenu.TopLevelMenuRep;
import com.group_finity.mascot.window.TranslucentWindow;
import com.group_finity.mascot.window.TranslucentWindowEvent;
import com.group_finity.mascot.window.TranslucentWindowEventHandler;
import com.group_finity.mascotnative.shared.SwingPopupUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class VirtualWindowPanel extends JPanel implements TranslucentWindow {

    protected static final Color CLEAR = new Color(0, 0, 0, 0);
    private TranslucentWindowEventHandler eventHandler = TranslucentWindowEventHandler.DEFAULT;
    private VirtualImage image;

    private final MouseAdapter parentListener = new MouseAdapter() {
        @Override
        public void mouseReleased(MouseEvent e) {
            if (dragging) {
                super.mouseReleased(e);
                var p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), VirtualWindowPanel.this);
                endDrag(new TranslucentWindowEvent(p));
            }
        }
    };

    private boolean dragging = false;

    public VirtualWindowPanel() {
        super();
        setBounds(0,0,0,0);
        setBackground(CLEAR);
        setOpaque(false);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    showPopupMenu(e);
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    beginDrag(new TranslucentWindowEvent(e.getPoint()));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    endDrag(new TranslucentWindowEvent(e.getPoint()));
                }
            }
        });
    }

    private void beginDrag(TranslucentWindowEvent e) {
        if (dragging) {return;}
        getEventHandler().onDragBegin(e);
        dragging = true;
    }

    private void endDrag(TranslucentWindowEvent e) {
        if (!dragging) {return;}
        getEventHandler().onDragEnd(e);
        dragging = false;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        getParent().addMouseListener(parentListener);
    }

    protected void showPopupMenu(MouseEvent e) {
        TopLevelMenuRep menuRep = getEventHandler().getContextMenuRep();
        if (menuRep == null) {
            return;
        }
        JPopupMenu popupMenu = SwingPopupUtil.createJPopupmenuFrom(menuRep);
        SwingUtilities.invokeLater(() -> popupMenu.show(this, e.getX(), e.getY()));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image.bufferedImage(), 0, 0, null);
    }

    public TranslucentWindowEventHandler getEventHandler() {
        return eventHandler;
    }

    @Override
    public void setEventHandler(TranslucentWindowEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @Override
    public void setImage(NativeImage image) {
        if (image != null) {
            this.image = (VirtualImage) image;
        }
    }

    @Override
    public void updateImage() {
        repaint();
    }

    @Override
    public void dispose() {
        SwingUtilities.invokeLater(() -> {
            var parent = getParent();
            if (parent != null) {
                parent.remove(this);
                parent.repaint();
                parent.removeMouseListener(parentListener);
            }
        });
    }
}
