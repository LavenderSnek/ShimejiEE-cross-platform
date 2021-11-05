package com.group_finity.mascotnative.mac.jna;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

@FieldOrder({"x", "y"})
public class CGPoint extends Structure {

    public double x;
    public double y;

    public CGPoint() {
        this(0, 0);
    }

    public CGPoint(double x, double y) {
        super();
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return (int) Math.round(this.x);
    }

    public int getY() {
        return (int) Math.round(this.y);
    }

}
