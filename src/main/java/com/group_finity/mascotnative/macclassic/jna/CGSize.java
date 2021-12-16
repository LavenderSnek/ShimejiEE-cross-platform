package com.group_finity.mascotnative.macclassic.jna;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

@FieldOrder({"width", "height"})
public class CGSize extends Structure {

    public double width;
    public double height;

    public int getWidth() {
        return (int) Math.round(this.width);
    }

    public int getHeight() {
        return (int) Math.round(this.height);
    }

}

