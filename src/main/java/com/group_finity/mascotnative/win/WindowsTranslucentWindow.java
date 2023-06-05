package com.group_finity.mascotnative.win;

import com.group_finity.mascot.window.TranslucentWindow;
import com.group_finity.mascotnative.shared.BaseTranslucentSwingWindow;

import com.group_finity.mascotnative.win.jna.GDI32;
import com.group_finity.mascotnative.win.jna.User32;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinUser;

import java.awt.Graphics;
import java.awt.Rectangle;

import static com.sun.jna.platform.win32.WinDef.HBITMAP;
import static com.sun.jna.platform.win32.WinDef.HWND;
import static com.sun.jna.platform.win32.WinDef.POINT;
import static com.sun.jna.platform.win32.WinDef.RECT;
import static com.sun.jna.platform.win32.WinUser.BLENDFUNCTION;
import static com.sun.jna.platform.win32.WinUser.SIZE;

class WindowsTranslucentWindow extends BaseTranslucentSwingWindow<WindowsNativeImage> implements TranslucentWindow {

    @Override
    protected void setUp() {
        setAlwaysOnTop(true);
    }

    @Override
    public void updateImage() {
        repaint();
    }

    @Override
    public void paint(final Graphics g) {
        if (getImage() != null) {
            paintNative(getImage().getBmpHandle());
        }
    }

    private void paintNative(HBITMAP bmpHandle) {
        final HWND hWnd = new HWND(Native.getComponentPointer(this));

        if (User32.INSTANCE.IsWindow(hWnd)) {

            final int exStyle = User32.INSTANCE.GetWindowLong(hWnd, User32.GWL_EXSTYLE);
            if ((exStyle & User32.WS_EX_LAYERED) == 0) {
                User32.INSTANCE.SetWindowLong(hWnd, User32.GWL_EXSTYLE, exStyle | User32.WS_EX_LAYERED);
            }

            // Create a DC source of the image
            final var clientDC = User32.INSTANCE.GetDC(hWnd);
            final var memDC = GDI32.INSTANCE.CreateCompatibleDC(clientDC);
            final var oldBmp = GDI32.INSTANCE.SelectObject(memDC, bmpHandle);

            User32.INSTANCE.ReleaseDC(hWnd, clientDC);

            // Destination Area
            final RECT windowRect = new RECT();
            User32.INSTANCE.GetWindowRect(hWnd, windowRect);

            // Forward
            final BLENDFUNCTION bf = new BLENDFUNCTION();
            bf.BlendOp = WinUser.AC_SRC_OVER;
            bf.BlendFlags = 0;
            bf.SourceConstantAlpha = (byte) 255;
            bf.AlphaFormat = WinUser.AC_SRC_ALPHA;

            final POINT lt = new POINT(windowRect.left, windowRect.top);
            final SIZE size = new SIZE();

            Rectangle winRectangle = windowRect.toRectangle();
            size.cx = winRectangle.width;
            size.cy = winRectangle.height;

            final POINT zero = new POINT(0,0);

            User32.INSTANCE.UpdateLayeredWindow(
                    hWnd,
                    null,
                    lt,
                    size,
                    memDC,
                    zero,
                    0,
                    bf,
                    User32.ULW_ALPHA
            );

            // Replace the bitmap you
            GDI32.INSTANCE.SelectObject(memDC, oldBmp);
            GDI32.INSTANCE.DeleteDC(memDC);
        }
    }

}