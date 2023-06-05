package com.group_finity.mascotnative.win;

import com.sun.jna.platform.win32.WinDef.HWND;

import java.awt.Rectangle;

public record WindowsIe (
        HWND hWnd,
        IeStatus status,
        Rectangle shadowedRect,
        String title
) {

    enum IeStatus {
        /** The IE is valid. */
        VALID,
        /** The IE is invalid and blocks any other valid IEs. */
        INVALID,
        /** The IE is invalid but does not prevent other IEs from being valid. */
        PASS_THROUGH,
        /** The IE is out of bounds and does not prevent other IEs from being valid.*/
        OUT_OF_BOUNDS,
    }

    public WindowsIe(HWND hWnd, IeStatus status) {
        this(hWnd, status, null, null);
    }

}
