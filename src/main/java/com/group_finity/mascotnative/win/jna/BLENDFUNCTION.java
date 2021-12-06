package com.group_finity.mascotnative.win.jna;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

@FieldOrder({"BlendOp", "BlendFlags", "SourceConstantAlpha", "AlphaFormat"})
public class BLENDFUNCTION extends Structure {

    public static final byte AC_SRC_OVER = 0;
    public static final byte AC_SRC_ALPHA = 1;

    public byte BlendOp;
    public byte BlendFlags;
    public byte SourceConstantAlpha;
    public byte AlphaFormat;

}
