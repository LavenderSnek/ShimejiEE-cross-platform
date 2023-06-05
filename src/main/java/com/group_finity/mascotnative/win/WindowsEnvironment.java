package com.group_finity.mascotnative.win;

import com.group_finity.mascot.environment.Area;
import com.group_finity.mascot.environment.BaseNativeEnvironment;
import com.group_finity.mascotnative.win.WindowsIe.IeStatus;
import com.group_finity.mascotnative.win.jna.Dwmapi;
import com.group_finity.mascotnative.win.jna.User32;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.LongByReference;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import static com.sun.jna.platform.win32.WinDef.HWND;
import static com.sun.jna.platform.win32.WinDef.RECT;
import static com.sun.jna.platform.win32.WinUser.HMONITOR;
import static com.sun.jna.platform.win32.WinUser.MONITORINFO;
import static com.sun.jna.platform.win32.WinUser.WNDENUMPROC;

class WindowsEnvironment extends BaseNativeEnvironment {

    @Override
    protected List<Rectangle> getNewDisplayBoundsList() {
        List<Rectangle> ret = new ArrayList<>(5);
        User32.INSTANCE.EnumDisplayMonitors(null, null,
                (hMonitor, hdcMonitor, lprcMonitor, dwData) -> {
                    ret.add(getBoundsOf(hMonitor));
                    return 1;
                }, null);
        return ret;
    }

    //======== IE stuff ==========//

    private WindowsIe currentIe = new WindowsIe(null, IeStatus.PASS_THROUGH);

    private WindowsIe createIeOf(HWND hWnd) {
        if (!User32.INSTANCE.IsWindowVisible(hWnd) || User32.INSTANCE.IsIconic(hWnd) || isWindowCloaked(hWnd)) {
            return new WindowsIe(hWnd, IeStatus.PASS_THROUGH);
        }

        if (User32.INSTANCE.IsZoomed(hWnd)) {
            return new WindowsIe(hWnd, IeStatus.INVALID);
        }

        String title = getTitleOf(hWnd);
        if (title.isEmpty() || title.equals("Program Manager")) {
            return new WindowsIe(hWnd, IeStatus.PASS_THROUGH);
        }

        RECT rect = new RECT();
        User32.INSTANCE.GetWindowRect(hWnd, rect);
        Rectangle shadowedRectangle = rect.toRectangle();

        if (!shadowedRectangle.intersects(getScreen().toRectangle())) {
            return new WindowsIe(hWnd, IeStatus.OUT_OF_BOUNDS, shadowedRectangle, title);
        }

        return new WindowsIe(hWnd, IeStatus.VALID, shadowedRectangle, title);
    }

    @Override
    protected void updateIe(Area ieToUpdate) {
        User32.INSTANCE.EnumWindows((hWnd, data) -> {
            WindowsIe ieObj = createIeOf(hWnd);

            switch (ieObj.status()) {
                case VALID, INVALID -> {
                    currentIe = ieObj;
                    return false;
                }
                case PASS_THROUGH, OUT_OF_BOUNDS -> {return true;}
                default -> throw new RuntimeException("Unrecognized IE status");
            }
        }, null);

        if (currentIe.status() == IeStatus.VALID) {
            ieToUpdate.set(currentIe.shadowedRect());
            ieToUpdate.setVisible(true);
        } else {
            ieToUpdate.set(new Rectangle());
            ieToUpdate.setVisible(false);
        }
    }

    @Override
    public String getActiveIETitle() {
        return currentIe.title();
    }

    //---IE movement

    @Override
    public void moveActiveIE(Point point) {
        HWND hWnd = currentIe.hWnd();

        // minimal checks
        if (currentIe.status() != IeStatus.VALID
            || User32.INSTANCE.IsZoomed(hWnd)
            || !User32.INSTANCE.IsWindowVisible(hWnd)) {
            return;
        }

        Rectangle rect = currentIe.shadowedRect();

        User32.INSTANCE.MoveWindow(hWnd, point.x, point.y, rect.width, rect.height, true);
    }

    @Override
    public void restoreIE() {
        User32.INSTANCE.EnumWindows(new WNDENUMPROC() {
            int offset = 5;

            @Override
            public boolean callback(HWND hWnd, Pointer data) {
                WindowsIe ieObj = createIeOf(hWnd);

                if (ieObj.status() == IeStatus.OUT_OF_BOUNDS) {
                    Rectangle nb = ieObj.shadowedRect().getBounds();
                    nb.setLocation(offset, 0);

                    User32.INSTANCE.MoveWindow(ieObj.hWnd(), nb.x, nb.y, nb.width, nb.height, true);
                    User32.INSTANCE.BringWindowToTop(ieObj.hWnd());

                    offset += 25;
                }
                return true;
            }
        }, null);
    }

    // == Util == //

    private static Rectangle getBoundsOf(HMONITOR hMonitor) {
        MONITORINFO info = new MONITORINFO();
        User32.INSTANCE.GetMonitorInfo(hMonitor, info);
        return info.rcWork.toRectangle();
    }

    private static boolean isWindowCloaked(HWND hWnd) {
        // doesn't support win 7, but this uses jdk 17 so win 7 is unlikely
        LongByReference flags = new LongByReference();
        var result = Dwmapi.INSTANCE.DwmGetWindowAttribute(hWnd, Dwmapi.DWMWA_CLOAKED, flags.getPointer(), 8);
        return result.equals(WinNT.S_OK) && flags.getValue() != 0;
    }

    private static String getTitleOf(HWND hWnd) {
        char[] title = new char[1024];
        int titleLen = User32.INSTANCE.GetWindowText(hWnd, title, 1024);

        return new String(title, 0, titleLen);
    }

}
