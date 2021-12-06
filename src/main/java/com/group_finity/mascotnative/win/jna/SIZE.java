package com.group_finity.mascotnative.win.jna;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

@FieldOrder({"cx", "cy"})
public class SIZE extends Structure {

    public int cx;
    public int cy;

}
