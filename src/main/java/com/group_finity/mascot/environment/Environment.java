package com.group_finity.mascot.environment;

import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides information regarding the desktop environment such as screen size,
 * cursor info, and window interaction related things.
 * <p>
 * Subclasses provide platform specific info that is difficult to obtain using pure java
 */
public abstract class Environment {

    private static Rectangle screenRect = new Rectangle(new Point(0, 0), Toolkit.getDefaultToolkit().getScreenSize());
    private static Map<String, Rectangle> screenRects = new HashMap<String, Rectangle>();

    static {
        // updates the windows + screen rect every n milliseconds
        final Thread thread = new Thread(() -> {
            try {
                while (true) {
                    updateScreenRect();
                    Thread.sleep(5000);
                }
            } catch (final InterruptedException ignored) {
            }
        });
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

	private static void updateScreenRect() {

		Rectangle virtualBounds = new Rectangle();

		Map<String, Rectangle> screenRects = new HashMap<String, Rectangle>();

		final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final GraphicsDevice[] screens = ge.getScreenDevices();

		for (final GraphicsDevice gd : screens) {
			screenRects.put(gd.getIDstring(), gd.getDefaultConfiguration().getBounds());
			virtualBounds = virtualBounds.union(gd.getDefaultConfiguration().getBounds());
		}

		Environment.screenRects = screenRects;

		screenRect = virtualBounds;
	}

	protected static Rectangle getScreenRect() {
		return screenRect;
	}

	private static Point getCursorPos() {
		java.awt.PointerInfo info = MouseInfo.getPointerInfo();
		return info != null ? info.getLocation() : new Point(0, 0);
	}

    //---

	private final Area screen = new Area();
	private final ComplexArea complexScreen = new ComplexArea();
	private final Location cursor = new Location();

    protected Environment() {
        tick();
    }

    public void tick() {
        this.screen.set(Environment.getScreenRect());
        this.complexScreen.set(screenRects);
        this.cursor.set(Environment.getCursorPos());
    }

	protected abstract Area getWorkArea();

	public abstract Area getActiveIE();

    public abstract String getActiveIETitle();

	public abstract void moveActiveIE(final Point point);

	public abstract void restoreIE();

	public abstract void refreshCache();

    public boolean isScreenTopBottom(final Point location) {
        int count = 0;

        for (Area area : getScreens()) {
            if (area.getTopBorder().isOn(location)) {
                ++count;
            }
            if (area.getBottomBorder().isOn(location)) {
                ++count;
            }
        }

        if (count == 0) {
            if (getWorkArea().getTopBorder().isOn(location)) {
                return true;
            }
            if (getWorkArea().getBottomBorder().isOn(location)) {
                return true;
            }
        }

        return count == 1;
    }

    public boolean isScreenLeftRight(final Point location) {
        int count = 0;

        for (Area area : getScreens()) {
            if (area.getLeftBorder().isOn(location)) {
                ++count;
            }
            if (area.getRightBorder().isOn(location)) {
                ++count;
            }
        }

        if (count == 0) {
            if (getWorkArea().getLeftBorder().isOn(location)) {
                return true;
            }
            if (getWorkArea().getRightBorder().isOn(location)) {
                return true;
            }
        }

        return count == 1;
    }

	public Area getScreen() {
		return screen;
	}

	public ComplexArea getComplexScreen() {
		return complexScreen;
	}

	public Collection<Area> getScreens() {
		return complexScreen.getAreas();
	}

	public Location getCursor() {
		return cursor;
	}

}
