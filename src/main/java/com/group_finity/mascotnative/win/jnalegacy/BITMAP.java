package com.group_finity.mascotnative.win.jnalegacy;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

@FieldOrder({"bmType", "bmWidth", "bmHeight", "bmWidthBytes", "bmPlanes", "bmBitsPixel", "bmBits"})
public class BITMAP extends Structure {

    public int bmType;
    public int bmWidth;
    public int bmHeight;
    public int bmWidthBytes;
    public short bmPlanes;
    public short bmBitsPixel;
    public Pointer bmBits;

}
