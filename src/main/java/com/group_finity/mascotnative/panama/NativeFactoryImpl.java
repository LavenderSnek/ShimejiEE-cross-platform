package com.group_finity.mascotnative.panama;

import com.group_finity.mascot.NativeFactory;
import com.group_finity.mascot.environment.NativeEnvironment;
import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.window.TranslucentWindow;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

public class NativeFactoryImpl extends NativeFactory {

    private final NativeEnvironment environment = new PanamaEnvironment();

    @Override
    public NativeEnvironment getEnvironment() {
        return environment;
    }

    @Override
    public NativeImage newNativeImage(BufferedImage src) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public NativeImage newNativeImage(Path path, double scaling, boolean flipped, boolean antialiasing) {
        return PanamaImage.loadFrom(path, scaling, flipped, antialiasing);
    }

    @Override
    public TranslucentWindow newTransparentWindow() {
        return new PanamaWindow();
    }
}
