package com.group_finity.mascotnative.virtualdesktop.display;

import com.group_finity.mascotnative.virtualdesktop.VirtualWindowPanel;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.util.List;

public class VirtualEnvironmentDisplay {

    private static final int INIT_WIDTH = 900;
    private static final int INIT_HEIGHT = 600;
    private static final Color BG_COLOR = Color.WHITE;

    private final JFrame frame;

    public VirtualEnvironmentDisplay() {
        frame = new JFrame("Shimeji");
        frame.getContentPane().setPreferredSize(new Dimension(INIT_WIDTH, INIT_HEIGHT));
        frame.getContentPane().setLayout(null);
        frame.getContentPane().setBackground(BG_COLOR);

        SwingUtilities.invokeLater(() -> {
            frame.pack();
            frame.setVisible(true);
        });
    }

    public List<Rectangle> getDisplayBoundsList() {
        return List.of(frame.getContentPane().getBounds());
    }

    public Point getCursorLocation(Point screenCoordinates) {
        var p = screenCoordinates.getLocation();
        SwingUtilities.convertPointFromScreen(p, frame.getContentPane());
        return p;
    }

    public void addShimejiWindow(VirtualWindowPanel panel) {
        SwingUtilities.invokeLater(() -> {
            frame.getContentPane().add(panel);
        });
    }

}
