package com.group_finity.mascotnative.macclassic;

import com.group_finity.mascot.environment.Area;
import com.group_finity.mascot.environment.BaseNativeEnvironment;
import com.group_finity.mascotnative.macclassic.jna.AXUIElementRef;
import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation.CFArrayRef;

import java.awt.Point;
import java.awt.Rectangle;
import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.Set;

/**
 * Uses the apple Accessibility API  thought JNA to get environment information
 *
 * @author nonowarn
 */
class MacEnvironment extends BaseNativeEnvironment {

    // this might still break on the new notched macs
    private static final int MENUBAR_HEIGHT = 24;
    private static final int MAX_DOCK_SIZE = 100;

    private static final long myPID = Long.parseLong(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
    private long frontMostAppPid = myPID;
    private final Set<Long> touchedProcesses = new HashSet<>();

    private Rectangle getFrontmostAppRect() {

        // prevent crash from not running appkit on main thread
        if (frontMostAppPid == myPID) {
            return null;
        }

        return AXUtils.getFocusedWindowRectOfPid(frontMostAppPid);
    }

    private void setFrontMostAppPid(long pid) {
        if (pid != myPID) {
            frontMostAppPid = pid;
            touchedProcesses.add(pid);
        }
    }

    private static double betweenOrLimit(double n, double min, double max) {
        return Math.min(Math.max(n, min), max);
    }

    /**
     * On a Mac, trying to move a window completely off-screen pushes it back into the screen.
     * Going beyond the dock is also considered offscreen. This returns an area where that won't happen
     *
     * @return A rectangle where the window will still be visible on screen
     */
    private Rectangle getWindowVisibleArea() {
        var total = getScreen().toRectangle();
        total.y += MENUBAR_HEIGHT;
        total.x += MAX_DOCK_SIZE;
        total.width -= MAX_DOCK_SIZE;
        return total;
    }

    @Override
    protected void updateIe(Area ieToUpdate) {
        setFrontMostAppPid(AXUtils.getFrontmostAppsPID());

        final Rectangle frontmostWindowRect = getFrontmostAppRect();
        final Rectangle windowVisibleArea = getWindowVisibleArea();

        ieToUpdate.setVisible(
                (frontmostWindowRect != null)
                        && frontmostWindowRect.intersects(windowVisibleArea)
                        && !frontmostWindowRect.contains(windowVisibleArea) // Exclude desktop
        );
        ieToUpdate.set(
                frontmostWindowRect == null
                        ? new Rectangle(-1, -1, 0, 0)
                        : frontmostWindowRect
        );
    }

    /**
     * As mentioned above, if you try to move completely off the screen, you will be pushed back,
     * so switch to moving as much as possible for such a position specification.
     */
    @Override
    public void moveActiveIE(final Point point) {
        if (frontMostAppPid == myPID) {
            return;
        }

        Rectangle windowRect = getFrontmostAppRect();
        if (windowRect == null) {
            return;
        }

        Rectangle visibleRect = getWindowVisibleArea();

        final double minX = visibleRect.getMinX() - windowRect.getWidth();
        final double maxX = visibleRect.getMaxX();
        final double minY = visibleRect.getMinY();
        final double maxY = visibleRect.getMaxY();

        double pX = betweenOrLimit(point.x, minX, maxX);
        double pY = betweenOrLimit(point.y, minY, maxY);

        AXUIElementRef focusedWin = AXUtils.copyFocusedWindowOfPid(frontMostAppPid);

        if (focusedWin != null) {
            AXUtils.setAXPositionOf(focusedWin, pX, pY);
            focusedWin.release();
        }
    }

    @Override
    public void restoreIE() {
        for (long pid : touchedProcesses) {
            CFArrayRef childWindows = AXUtils.copyChildWindowsOfPid(pid);
            restoreWindowsInArr(childWindows);
            childWindows.release();
        }
        touchedProcesses.clear();
    }

    private void restoreWindowsInArr(CFArrayRef axUiWindows) {
        Rectangle visibleArea = getWindowVisibleArea();

        if (axUiWindows == null) {
            return;
        }

        int ct = axUiWindows.getCount();
        for (int i = 0; i < ct; i++) {
            Pointer winPtr = axUiWindows.getValueAtIndex(i);
            if (winPtr == Pointer.NULL) {
                continue;
            }

            AXUIElementRef window = new AXUIElementRef();
            window.setPointer(winPtr);
            Rectangle windowRect = AXUtils.getRectOf(window);

            if (!visibleArea.intersects(windowRect)) {
                AXUtils.setAXPositionOf(window, 0,0);
            }
        }

    }

}
