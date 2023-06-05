package com.group_finity.mascotnative.win.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.W32APIOptions;

import static com.sun.jna.platform.win32.WinNT.HANDLE;

public interface GDI32 extends com.sun.jna.platform.win32.GDI32 {

    GDI32 INSTANCE = Native.load("gdi32", GDI32.class, W32APIOptions.DEFAULT_OPTIONS);

    /**
     * <a href="https://docs.microsoft.com/en-us/windows/win32/api/wingdi/nf-wingdi-getobjectw">Microsoft docs: GetObjectW</a>
     * @param h A handle to the graphics object of interest. This can be a handle to one of the following:
     *          a logical bitmap, a brush, a font, a palette, a pen, or a device independent bitmap created by calling
     *          the CreateDIBSection function.
     * @param c The number of bytes of information to be written to the buffer.
     * @param pv A pointer to a buffer that receives the information about the specified graphics object.
     */
    int GetObjectW(HANDLE h, int c, Pointer pv);

}
