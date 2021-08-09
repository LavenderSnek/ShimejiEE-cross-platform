package com.group_finity.mascot.win.jna;

import com.sun.jna.Structure;

public class BITMAPINFOHEADER extends Structure {

    public int biSize;
    public int biWidth;
    public int biHeight;
    public short biPlanes;
    public short biBitCount;
    public int biCompression;
    public int biSizeImage;
    public int biXPelsPerMeter;
    public int biYPelsPerMeter;
    public int biClrUsed;
    public int biClrImportant;

}
