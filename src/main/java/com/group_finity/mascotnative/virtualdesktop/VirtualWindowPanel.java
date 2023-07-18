package com.group_finity.mascotnative.virtualdesktop;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.ui.contextmenu.TopLevelMenuRep;
import com.group_finity.mascot.window.TranslucentWindow;
import com.group_finity.mascot.window.TranslucentWindowEvent;
import com.group_finity.mascot.window.TranslucentWindowEventHandler;
import com.group_finity.mascotnative.shared.SwingPopupUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class VirtualWindowPanel extends JPanel implements TranslucentWindow {

    protected static final Color CLEAR = new Color(0, 0, 0, 0);

    private TranslucentWindowEventHandler eventHandler = TranslucentWindowEventHandler.DEFAULT;

    private VirtualImage image;

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

    @Override
    protected void paintComponent(Graphics g) {
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
            }
        });
    }
}
