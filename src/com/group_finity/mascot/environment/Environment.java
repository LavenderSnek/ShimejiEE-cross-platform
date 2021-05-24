package com.group_finity.mascot.environment;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Provides information regarding the desktop environment such as screen size,
 * cursor info, and window interaction related things.
 *
 * Subclasses provide platform specific info that it difficult to obtain using pure java
 * */
public abstract class Environment {

	protected abstract Area getWorkArea();

	public abstract Area getActiveIE();

	public abstract void moveActiveIE(final Point point);

	public abstract void restoreIE();
    
    public abstract void refreshCache( );

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
			} catch (final InterruptedException ignored) {}
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

	private static Rectangle getScreenRect() {
		return screenRect;
	}

    private static Point getCursorPos() {
        java.awt.PointerInfo info = MouseInfo.getPointerInfo( );
        return info != null ? info.getLocation() : new Point( 0, 0 );
    }

	public ComplexArea complexScreen = new ComplexArea();

	public Area screen = new Area();

	public Location cursor = new Location();

	protected Environment() {
		tick();
	}

	public void tick() {
		this.screen.set(Environment.getScreenRect());
		this.complexScreen.set(screenRects);
		this.cursor.set(Environment.getCursorPos());
	}

	public Area getScreen() {
		return screen;
	}

	public Collection<Area> getScreens() {
		return complexScreen.getAreas();
	}

	public ComplexArea getComplexScreen() {
		return complexScreen;
	}

	public Location getCursor() {
		return cursor;
	}

	public boolean isScreenTopBottom(final Point location) {

		int count = 0;

		for( Area area: getScreens() ) {
			if ( area.getTopBorder().isOn(location)) {
				++count;
			}
			if ( area.getBottomBorder().isOn(location)) {
				++count;
			}
		}


		if ( count==0 ) {
			if ( getWorkArea().getTopBorder().isOn(location) ) {
				return true;
			}
			if ( getWorkArea().getBottomBorder().isOn(location) ) {
				return true;
			}
		}

		return count==1;
	}

	public boolean isScreenLeftRight(final Point location) {

		int count = 0;

		for( Area area: getScreens() ) {
			if ( area.getLeftBorder().isOn(location)) {
				++count;
			}
			if ( area.getRightBorder().isOn(location)) {
				++count;
			}
		}

		if ( count==0 ) {
			if ( getWorkArea().getLeftBorder().isOn(location) ) {
				return true;
			}
			if ( getWorkArea().getRightBorder().isOn(location) ) {
				return true;
			}
		}

		return count==1;
	}

}
