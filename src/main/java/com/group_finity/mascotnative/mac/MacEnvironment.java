package com.group_finity.mascotnative.mac;

import com.group_finity.mascot.environment.Area;
import com.group_finity.mascot.environment.Environment;
import com.group_finity.mascotnative.mac.jna.*;
import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation.CFArrayRef;
import com.sun.jna.platform.mac.CoreFoundation.CFStringRef;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Uses the apple Accessibility API  thought JNA to get environment information
 *
 * @author nonowarn
 */
class MacEnvironment extends Environment {
    //a lot of the comments here have been though google translate, it did a surprisingly good job tbh

    private static final Carbon carbon = Carbon.INSTANCE;

    private static final int MENUBAR_HEIGHT = 22;
    private static final int MAX_DOCK_SIZE = 100;

    /**
     * On Mac, you can take the active window,
     * Make the Mascot react to it.
     * <p>
     * So, in this class, give activeIE the alias frontmostWindow.
     */
    private static final Area activeIE = new Area();

    private static final Area frontmostWindow = activeIE;

    private static final int screenWidth = (int) Math.round(Toolkit.getDefaultToolkit().getScreenSize().getWidth());
    private static final int screenHeight = (int) Math.round(Toolkit.getDefaultToolkit().getScreenSize().getHeight());

    /**
     * the Shimeji program's PID
     */
    private static final long myPID =
            Long.parseLong(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);

    /**
     * PID of the frontmost window's application
     */
    private static long frontMostAppPid = myPID;

    private static final HashSet<Long> touchedProcesses = new HashSet<>();

    private static final CFStringRef kAXPosition = toCFString("AXPosition");
    private static final CFStringRef kAXSize = toCFString("AXSize");
    private static final CFStringRef kAXFocusedWindow = toCFString("AXFocusedWindow");
    private static final CFStringRef kAXChildren = toCFString("AXChildren");

    private static CFStringRef toCFString(String s) {
        return CFStringRef.createCFString(s);
    }

    private static long getFrontmostAppsPID() {
        ProcessSerialNumber frontProcessPsn = new ProcessSerialNumber();
        LongByReference frontProcessPidp = new LongByReference();

        carbon.GetFrontProcess(frontProcessPsn);
        carbon.GetProcessPID(frontProcessPsn, frontProcessPidp);

        return frontProcessPidp.getValue();
    }


    private static Rectangle getFrontmostAppRect() {
        Rectangle rect = null;
        long pid = getFrontMostAppPid();

        // prevent crash from not running appkit on main thread
        if (pid == myPID) {
            return null;
        }

        AXUIElementRef application = carbon.AXUIElementCreateApplication(pid);

        PointerByReference windowp = new PointerByReference();

        // XXX: Is error checking necessary other than here?
        if (carbon.AXUIElementCopyAttributeValue(application, kAXFocusedWindow, windowp) == carbon.kAXErrorSuccess) {
            AXUIElementRef window = new AXUIElementRef();
            window.setPointer(windowp.getValue());
            rect = getRectOfWindow(window);
        }

        application.release();
        return rect;
    }


    private static CGPoint getPositionOfWindow(AXUIElementRef window) {
        CGPoint position = new CGPoint();
        AXValueRef axvalue = new AXValueRef();
        PointerByReference valuep = new PointerByReference();

        carbon.AXUIElementCopyAttributeValue(window, kAXPosition, valuep);
        axvalue.setPointer(valuep.getValue());
        carbon.AXValueGetValue(axvalue, carbon.kAXValueCGPointType, position.getPointer());
        position.read();

        return position;
    }


    private static CGSize getSizeOfWindow(AXUIElementRef window) {
        CGSize size = new CGSize();
        AXValueRef axvalue = new AXValueRef();
        PointerByReference valuep = new PointerByReference();

        carbon.AXUIElementCopyAttributeValue(window, kAXSize, valuep);
        axvalue.setPointer(valuep.getValue());
        carbon.AXValueGetValue(axvalue, carbon.kAXValueCGSizeType, size.getPointer());
        size.read();

        return size;
    }


    private static void moveFrontmostWindow(final Point point) {
        AXUIElementRef application =
                carbon.AXUIElementCreateApplication(frontMostAppPid);

        PointerByReference windowp = new PointerByReference();

        if (carbon.AXUIElementCopyAttributeValue(application, kAXFocusedWindow, windowp) == carbon.kAXErrorSuccess) {
            AXUIElementRef window = new AXUIElementRef();
            window.setPointer(windowp.getValue());
            moveWindow(window, point.x, point.y);
        }

        application.release();
    }


    private static void restoreWindows() {
        Rectangle visibleArea = getWindowVisibleArea();
        for (long pid : getTouchedProcesses()) {
            AXUIElementRef application =
                    carbon.AXUIElementCreateApplication(pid);

            for (AXUIElementRef window : getWindowsOf(application)) {
                window.retain();
                Rectangle windowRect = getRectOfWindow(window);
                if (!visibleArea.intersects(windowRect)) {
                    moveWindow(window, 0, 0);
                }
                window.release();
            }

            application.release();
        }
    }


    private static ArrayList<AXUIElementRef> getWindowsOf(AXUIElementRef application) {
        PointerByReference axWindowsp = new PointerByReference();
        ArrayList<AXUIElementRef> ret = new ArrayList<>();

        carbon.AXUIElementCopyAttributeValue(application, kAXChildren, axWindowsp);

        if (axWindowsp.getValue() == Pointer.NULL) {
            return ret;
        }

        var cfWindows = new CFArrayRef(axWindowsp.getValue());

        for (int i = 0, l = cfWindows.getCount(); i < l; ++i) {
            Pointer p = cfWindows.getValueAtIndex(i);
            AXUIElementRef el = new AXUIElementRef();
            el.setPointer(p);
            ret.add(el);
        }

        return ret;
    }

    private static Rectangle getRectOfWindow(AXUIElementRef window) {
        CGPoint pos = getPositionOfWindow(window);
        CGSize size = getSizeOfWindow(window);
        return new Rectangle(pos.getX(), pos.getY(), size.getWidth(), size.getHeight());
    }

    private static void moveWindow(AXUIElementRef window, int x, int y) {
        CGPoint position = new CGPoint(x, y);
        position.write();
        AXValueRef axvalue = carbon.AXValueCreate(
                carbon.kAXValueCGPointType, position.getPointer());
        carbon.AXUIElementSetAttributeValue(window, kAXPosition, axvalue);
    }

    /**
     * When min < max,
     * Returns x if min <= x <= max
     * Returns min if x < min
     * Returns max if x> max
     */
    private static double betweenOrLimit(double n, double min, double max) {
        return Math.min(Math.max(n, min), max);
    }

    /**
     * On a Mac, trying to move a window completely off-screen pushes it back into the screen.
     * Going beyond the dock is also considered offscreen. This returns an area where that won't happen
     *
     * @return A rectangle where the window will still be visible on screen
     */
    private static Rectangle getWindowVisibleArea() {
        return new Rectangle(
                MAX_DOCK_SIZE,
                MENUBAR_HEIGHT,
                getScreenWidth() - 2 * MAX_DOCK_SIZE,
                getScreenHeight() - MENUBAR_HEIGHT
        );
    }

    private void updateFrontmostWindow() {
        final Rectangle frontmostWindowRect = getFrontmostAppRect();
        final Rectangle windowVisibleArea = getWindowVisibleArea();

        frontmostWindow.setVisible(
                (frontmostWindowRect != null)
                        && frontmostWindowRect.intersects(windowVisibleArea)
                        && !frontmostWindowRect.contains(windowVisibleArea) // Exclude desktop
        );
        frontmostWindow.set(
                frontmostWindowRect == null
                        ? new Rectangle(-1, -1, 0, 0)
                        : frontmostWindowRect
        );
    }

    private static void updateFrontmostApp() {
        long newPID = getFrontmostAppsPID();
        setFrontMostAppPid(newPID);
    }

    @Override
    public void tick() {
        super.tick();
        updateFrontmostApp();
        this.updateFrontmostWindow();
    }

    /**
     * As mentioned above, if you try to move completely off the screen, you will be pushed back,
     * so switch to moving as much as possible for such a position specification.
     */
    @Override
    public void moveActiveIE(final Point point) {
        final Rectangle visibleRect = getWindowVisibleArea();
        final Rectangle windowRect = getFrontmostAppRect();

        // Wrap coordinates to the left
        final double minX = visibleRect.getMinX() - windowRect.getWidth();
        // Wrap coordinates to the right
        final double maxX = visibleRect.getMaxX();

        // Upward wrapping coordinates (Cannot move above the menu bar)
        final double minY = visibleRect.getMinY();
        // Downward wrapping coordinates
        final double maxY = visibleRect.getMaxY();

        double pX = point.getX();
        double pY = point.getY();

        // Folding in the X direction
        pX = betweenOrLimit(pX, minX, maxX);

        // Folding in the Y direction
        pY = betweenOrLimit(pY, minY, maxY);

        point.setLocation(pX, pY);
        moveFrontmostWindow(point);
    }

    @Override
    public void restoreIE() {
        restoreWindows();
        getTouchedProcesses().clear();
    }

    @Override
    public Area getWorkArea() {
        return getScreen();
    }

    @Override
    public Area getActiveIE() {
        return activeIE;
    }

    @Override
    public String getActiveIETitle() {
        return null;
    }

    @Override
    public void refreshCache() {}


    private static void setFrontMostAppPid(long pid) {
        if (pid != myPID) {
            frontMostAppPid = pid;
            getTouchedProcesses().add(pid);
        }
    }

    private static long getFrontMostAppPid() {
        return frontMostAppPid;
    }

    private static HashSet<Long> getTouchedProcesses() {
        return touchedProcesses;
    }

    private static int getScreenWidth() {
        return screenWidth;
    }

    private static int getScreenHeight() {
        return screenHeight;
    }

}
