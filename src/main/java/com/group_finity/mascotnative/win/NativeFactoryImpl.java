package com.group_finity.mascotnative.win;

import com.group_finity.mascot.NativeFactory;
import com.group_finity.mascot.environment.NativeEnvironment;
import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.window.TranslucentWindow;

import java.awt.image.BufferedImage;

public class NativeFactoryImpl extends NativeFactory {

    private final NativeEnvironment environment = new WindowsEnvironment();

    @Override
    public NativeEnvironment getEnvironment() {
        return this.environment;
    }

    @Override
    public NativeImage newNativeImage(final BufferedImage src) {
        return new WindowsNativeImage(src, 1);
    }

    @Override
    public TranslucentWindow newTransparentWindow() {
        return new WindowsTranslucentWindow();
    }

}
