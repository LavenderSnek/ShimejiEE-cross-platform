package com.group_finity.mascotnative.win.jna;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

@FieldOrder({"x", "y"})
public class POINT extends Structure {

    public int x;
    public int y;

}
