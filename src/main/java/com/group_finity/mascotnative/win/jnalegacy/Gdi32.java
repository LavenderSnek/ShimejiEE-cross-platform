package com.group_finity.mascotnative.win.jnalegacy;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

public interface Gdi32 extends StdCallLibrary {

    Gdi32 INSTANCE = Native.load("Gdi32", Gdi32.class);

    int DeleteObject(Pointer hObject);

    Pointer CreateRectRgn(
            int nLeftRect,
            int nTopRect,
            int nRightRect,
            int nBottomRect
    );

    int GetRgnBox(Pointer hrgn, RECT lprc);

}
