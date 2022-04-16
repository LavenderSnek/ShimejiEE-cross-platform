package com.group_finity.mascotnative.generic;

import com.group_finity.mascot.NativeFactory;
import com.group_finity.mascot.environment.NativeEnvironment;
import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.window.TranslucentWindow;

import java.awt.image.BufferedImage;

public class NativeFactoryImpl extends NativeFactory {

    private NativeEnvironment environment = new GenericEnvironment();

    @Override
    public NativeEnvironment getEnvironment() {
        return this.environment;
    }

    @Override
    public NativeImage newNativeImage(final BufferedImage src, int scaling) {
        return new GenericNativeImage(src);
    }

    @Override
    public TranslucentWindow newTransparentWindow() {
        return new GenericTranslucentWindow();
    }

}
