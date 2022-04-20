package com.group_finity.mascot.environment;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Base class providing common functionality for {@link NativeEnvironment}.
 */
public abstract class BaseNativeEnvironment implements NativeEnvironment {

    protected final Area invisibleScreen = new Area() {
        @Override
        public boolean isVisible() {
            return false;
        }
    };

    private final List<Area> screenAreas = new ArrayList<>(1);
    private final Area unionScreenArea = new Area();

    private final Area ieArea = new Area();

    private final Location cursorLocation = new Location();

    private final ComplexArea complexArea = new ComplexArea() {
        @Override public void set(Map<String, Rectangle> rectangles) {}
        @Override public void set(String name, Rectangle value) {}
        @Override public void retain(Collection<String> deviceNames) {}

        @Override
        public Collection<Area> getAreas() {
            return screenAreas;
        }
    };

    protected BaseNativeEnvironment() {
        updateDisplayBounds();
        tick();
    }

    //====== Display =======//

    @Override
    public Area getWorkAreaAt(Point point) {
        if (screenAreas.size() == 1) {
            return screenAreas.get(0);
        }

        for (Area screenArea : screenAreas) {
            if (screenArea.contains(point.x, point.y)) {
                return screenArea;
            }
        }

        return invisibleScreen;
    }

    @Override
    public Area getScreen() {
        return unionScreenArea;
    }

    @Override
    public ComplexArea getComplexScreen() {
        return complexArea;
    }

    @Override
    public boolean isScreenLeftRight(Point location) {
        return getComplexScreen().getRightBorder(location) != null
                ^ getComplexScreen().getLeftBorder(location) != null;
    }

    @Override
    public boolean isScreenTopBottom(Point location) {
        return getComplexScreen().getTopBorder(location) != null
                ^ getComplexScreen().getBottomBorder(location) != null;
    }

    /**
     * Refreshes all cached info relating to display bounds.
     * <p>
     * This method is called in the constructor and should be called by
     * subclasses when the display configuration has changed.
     *
     * @see #getNewDisplayBoundsList()
     */
    public void updateDisplayBounds() {
        Rectangle totalBounds = new Rectangle();
        screenAreas.clear();

        for (var sb : getNewDisplayBoundsList()) {
            totalBounds = totalBounds.union(sb);

            Area screenArea = new Area();
            screenArea.set(sb);
            screenArea.set(sb); // remove delta
            screenAreas.add(screenArea);
        }

        Rectangle primary = screenAreas.size() > 0
                ? screenAreas.get(0).toRectangle()
                : new Rectangle();

        invisibleScreen.set(primary);
        unionScreenArea.set(totalBounds);
    }

    // could have been areas, but this approach is a bit less painful with native

    /**
     * The current bounds of each of the displays as reported by the system.
     * <p>
     * The first display is assumed to be the main display. Subclasses can override
     * this method to exclude areas such as taskbars/menus.
     */
    protected List<Rectangle> getNewDisplayBoundsList() {
        final GraphicsDevice[] screenDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();

        List<Rectangle> ret = new ArrayList<>(1);

        for (var sd : screenDevices) {
            Rectangle sdBounds = sd.getDefaultConfiguration().getBounds();
            ret.add(sdBounds);
        }

        return ret;
    }

    //========== updated per frame ============//

    /**
     * Updates the cursor and activeIE info
     * <p>
     * The default implementation of this function calls {@link #updateIe(Area)}
     * and {@link #updateIe(Area)}
     */
    @Override
    public void tick() {
        updateIe(ieArea);
        updateCursorLocation();
    }

    //--- interactive environment

    @Override
    public final Area getActiveIE() {
        return ieArea;
    }

    /**
     * Subclasses should set the status of the provided IE to match the system.
     *
     * @param ieToUpdate IE that needs to be updated.
     */
    protected abstract void updateIe(Area ieToUpdate);

    //--- cursor

    @Override
    public Location getCursor() {
        return cursorLocation;
    }

    /**
     * Updates the cursor location
     * <p>
     * Uses {@link #getNewCursorLocation()} to find the new value
     */
    protected void updateCursorLocation() {
        Point currentCursorLoc = getNewCursorLocation();
        if (currentCursorLoc != null) {
            cursorLocation.set(currentCursorLoc);
        }
    }

    /**
     * The current cursor location as reported by the system.
     * <p>
     * This value is used to update the cursor
     * location each tick.
     */
    protected Point getNewCursorLocation() {
        try {
            PointerInfo info = MouseInfo.getPointerInfo();
            return info.getLocation();
        } catch (NullPointerException e) {
            // info may be null on multi-monitor setups
            // https://bugs.openjdk.java.net/browse/JDK-8017567
        }
        return null;
    }

}
