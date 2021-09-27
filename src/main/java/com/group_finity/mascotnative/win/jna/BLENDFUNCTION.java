package com.group_finity.mascotnative.win.jna;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class BLENDFUNCTION extends Structure {

    public static final byte AC_SRC_OVER = 0;
    public static final byte AC_SRC_ALPHA = 1;

    public byte BlendOp;
    public byte BlendFlags;
    public byte SourceConstantAlpha;
    public byte AlphaFormat;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("AC_SRC_OVER", "AC_SRC_ALPHA", "BlendOp", "BlendFlags", "SourceConstantAlpha", "AlphaFormat");
    }

}
