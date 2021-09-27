package com.group_finity.mascotnative.win.jna;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

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

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("biSize", "biWidth", "biHeight", "biPlanes", "biBitCount", "biCompression", "biSizeImage", "biXPelsPerMeter", "biYPelsPerMeter", "BiClrUsed", "BiClrImportant");
    }

}
