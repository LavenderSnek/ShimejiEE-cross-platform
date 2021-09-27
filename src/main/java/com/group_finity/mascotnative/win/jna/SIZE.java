package com.group_finity.mascotnative.win.jna;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class SIZE extends Structure {

    public int cx;
    public int cy;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("cx", "cy");
    }

}
