package com.group_finity.mascotnative.generic;

import com.group_finity.mascot.NativeFactory;
import com.group_finity.mascot.environment.NativeEnvironment;
import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.window.TranslucentWindow;
import com.group_finity.mascotnative.shared.ImageUtil;

import java.io.IOException;
import java.nio.file.Path;

@SuppressWarnings("unused")
public class NativeFactoryImpl extends NativeFactory {

    private final NativeEnvironment environment = new GenericEnvironment();

    @Override
    public NativeEnvironment getEnvironment() {
        return this.environment;
    }

    @Override
    public NativeImage newNativeImage(Path path, double scaling, boolean flipped, boolean antialiasing) throws IOException {
        return new GenericNativeImage(ImageUtil.newBufferedImage(path, scaling, flipped, antialiasing));
    }

    @Override
    public TranslucentWindow newTransparentWindow() {
        return new GenericTranslucentWindow();
    }

}
