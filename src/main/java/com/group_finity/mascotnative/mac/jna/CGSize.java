package com.group_finity.mascotnative.mac.jna;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class CGSize extends Structure {

    public double width;
    public double height;

    public int getWidth() {
        return (int) Math.round(this.width);
    }

    public int getHeight() {
        return (int) Math.round(this.height);
    }

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("width", "height");
    }

}

