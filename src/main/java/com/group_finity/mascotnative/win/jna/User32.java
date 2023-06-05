package com.group_finity.mascotnative.win.jna;

import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;

public interface User32 extends com.sun.jna.platform.win32.User32 {

    User32 INSTANCE = Native.load("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);

    /**
     * <a href="https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-isiconic">Microsoft Docs: IsIconic</a>
     * <p>
     * Determines whether the specified window is minimized (iconic).
     *
     * @param hWnd A handle to the window to be tested.
     * @return If the window is iconic, the return value is true.
     */
    boolean IsIconic(HWND hWnd);

    /**
     * <a href="https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-iszoomed">Microsoft docs: IsZoomed</a>
     * <p>
     * Determines whether a window is maximized.
     *
     * @param hWnd A handle to the window to be tested.
     * @return If the window is zoomed, the return value is true.
     */
    boolean IsZoomed(HWND hWnd);

}
