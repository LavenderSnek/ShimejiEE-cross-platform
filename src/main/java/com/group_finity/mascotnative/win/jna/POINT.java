package com.group_finity.mascotnative.win.jna;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class POINT extends Structure {

    public int x;
    public int y;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("x", "y");
    }

}
