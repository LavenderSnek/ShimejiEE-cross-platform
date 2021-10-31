package com.group_finity.mascotnative.win.jna;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.win32.StdCallLibrary;

/**
 * Wraps up Dwmapi to get access to the new Cloaked variable.
 *
 * @author <a href="https://kilkakon.com/shimeji/">Kilkakon</a>
 */
public interface Dwmapi extends StdCallLibrary {

    Dwmapi INSTANCE = Native.load("Dwmapi", Dwmapi.class);

    int DWMWA_CLOAKED = 14;

    NativeLong DwmGetWindowAttribute(Pointer hwnd, int dwAttribute, LongByReference pvAttribute, int cbAttribute);

}
