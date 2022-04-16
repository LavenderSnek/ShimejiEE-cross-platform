package com.group_finity.mascotnative.win;

import com.group_finity.mascot.window.TranslucentWindow;
import com.group_finity.mascotnative.shared.BaseTranslucentSwingWindow;
import com.group_finity.mascotnative.win.jna.*;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.awt.Graphics;

class WindowsTranslucentWindow extends BaseTranslucentSwingWindow<WindowsNativeImage> implements TranslucentWindow {

    private static final int OPAQUE = 255;

    @Override
    protected void setUp() {
        setAlwaysOnTop(true);
    }

    @Override
    public void paint(final Graphics g) {
        if (getImage() != null) {
            paint(getImage().getNativeHandle());
        }
    }

    /**
     * Natively draws the given image.
     *
     * @param imageHandle native bitmap handle.
     */
    private void paint(final Pointer imageHandle) {

        final Pointer hWnd = Native.getComponentPointer(this);

        if (User32.INSTANCE.IsWindow(hWnd) != 0) {

            final int exStyle = User32.INSTANCE.GetWindowLongW(hWnd, User32.GWL_EXSTYLE);
            if ((exStyle & User32.WS_EX_LAYERED) == 0) {
                User32.INSTANCE.SetWindowLongW(hWnd, User32.GWL_EXSTYLE, exStyle | User32.WS_EX_LAYERED);
            }

            // Create a DC source of the image
            final Pointer clientDC = User32.INSTANCE.GetDC(hWnd);
            final Pointer memDC = Gdi32.INSTANCE.CreateCompatibleDC(clientDC);
            final Pointer oldBmp = Gdi32.INSTANCE.SelectObject(memDC, imageHandle);

            User32.INSTANCE.ReleaseDC(hWnd, clientDC);

            // Destination Area
            final RECT windowRect = new RECT();
            User32.INSTANCE.GetWindowRect(hWnd, windowRect);

            // Forward
            final BLENDFUNCTION bf = new BLENDFUNCTION();
            bf.BlendOp = BLENDFUNCTION.AC_SRC_OVER;
            bf.BlendFlags = 0;
            bf.SourceConstantAlpha = (byte) OPAQUE;
            bf.AlphaFormat = BLENDFUNCTION.AC_SRC_ALPHA;

            final POINT lt = new POINT();
            lt.x = windowRect.left;
            lt.y = windowRect.top;
            final SIZE size = new SIZE();
            size.cx = windowRect.Width();
            size.cy = windowRect.Height();
            final POINT zero = new POINT();
            User32.INSTANCE.UpdateLayeredWindow(
                    hWnd, Pointer.NULL,
                    lt, size,
                    memDC, zero, 0, bf, User32.ULW_ALPHA);

            // Replace the bitmap you
            Gdi32.INSTANCE.SelectObject(memDC, oldBmp);
            Gdi32.INSTANCE.DeleteDC(memDC);
        }
    }

    @Override
    public void updateImage() {
        repaint();
    }

}