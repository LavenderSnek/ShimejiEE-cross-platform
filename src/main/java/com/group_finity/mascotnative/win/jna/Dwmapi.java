package com.group_finity.mascotnative.win.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

import static com.sun.jna.platform.win32.WinNT.HRESULT;
import static com.sun.jna.platform.win32.WinNT.HWND;

public interface Dwmapi extends StdCallLibrary {

    Dwmapi INSTANCE = Native.load("dwmapi", Dwmapi.class);

    int DWMWA_NCRENDERING_ENABLED = 1;
    int DWMWA_NCRENDERING_POLICY = 2;
    int DWMWA_TRANSITIONS_FORCEDISABLED = 3;
    int DWMWA_ALLOW_NCPAINT = 4;
    int DWMWA_CAPTION_BUTTON_BOUNDS = 5;
    int DWMWA_NONCLIENT_RTL_LAYOUT = 6;
    int DWMWA_FORCE_ICONIC_REPRESENTATION = 7;
    int DWMWA_FLIP3D_POLICY = 8;
    int DWMWA_EXTENDED_FRAME_BOUNDS = 9;
    int DWMWA_HAS_ICONIC_BITMAP = 10;
    int DWMWA_DISALLOW_PEEK = 11;
    int DWMWA_EXCLUDED_FROM_PEEK = 12;
    int DWMWA_CLOAK = 13;
    int DWMWA_CLOAKED = 14;
    int DWMWA_FREEZE_REPRESENTATION = 15;

    /**
     * <a href="https://learn.microsoft.com/en-us/windows/win32/api/dwmapi/nf-dwmapi-dwmgetwindowattribute">Microsoft docs: DwmGetWindowAttribute</a>
     * <p>
     * Retrieves the current value of a specified Desktop Window Manager (DWM) attribute applied to a window.
     * <p>
     * For programming guidance, and code examples, see
     * <a href="https://docs.microsoft.com/en-us/windows/desktop/dwm/composition-ovw#controlling-non-client-region-rendering">Controlling non-client region rendering</a>.
     *
     * @param hWnd        The handle to the window from which the attribute value is to be retrieved.
     * @param dwAttribute A flag describing which value to retrieve, specified as a value of the
     *                    <a href="https://docs.microsoft.com/en-us/windows/desktop/api/Dwmapi/ne-dwmapi-dwmwindowattribute">DWMWINDOWATTRIBUTE</a> enumeration.
     *                    This parameter specifies which attribute to retrieve, and the {@param pvAttribute} parameter points to
     *                    an object into which the attribute value is retrieved.
     * @param pvAttribute A pointer to a value which, when this function returns successfully, receives the current value of the attribute.
     *                    The type of the retrieved value depends on the value of the {@param dwAttribute} parameter. The
     *                    <a href="https://docs.microsoft.com/en-us/windows/desktop/api/Dwmapi/ne-dwmapi-dwmwindowattribute">DWMWINDOWATTRIBUTE</a>
     *                    enumeration topic indicates, in the row for each flag, what type of value you should pass a pointer to in
     *                    the {@param pvAttribute} parameter.
     * @param cbAttribute The size, in bytes, of the attribute value being received via the {@param pvAttribute} parameter.
     *                    The type of the retrieved value, and therefore its size in bytes, depends on the value of the
     *                    {@param dwAttribute} parameter.
     * @return If the function succeeds, it returns S_OK Otherwise, it returns an {@link HRESULT HRESULT}
     * <a href="https://docs.microsoft.com/en-us/windows/win32/com/com-error-codes-10">error code</a>.
     */
    HRESULT DwmGetWindowAttribute(HWND hWnd, int dwAttribute, Pointer pvAttribute, int cbAttribute);

}
