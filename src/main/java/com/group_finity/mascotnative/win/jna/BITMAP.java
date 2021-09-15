package com.group_finity.mascotnative.win.jna;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class BITMAP extends Structure {

    public int bmType;
    public int bmWidth;
    public int bmHeight;
    public int bmWidthBytes;
    public short bmPlanes;
    public short bmBitsPixel;
    public Pointer bmBits;

}
