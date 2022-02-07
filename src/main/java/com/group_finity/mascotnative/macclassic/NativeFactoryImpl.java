package com.group_finity.mascotnative.macclassic;

import com.group_finity.mascot.NativeFactory;
import com.group_finity.mascot.environment.NativeEnvironment;
import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;

import java.awt.image.BufferedImage;

/**
 * @author nonowarn
 */
@SuppressWarnings("unused")
public class NativeFactoryImpl extends NativeFactory {

    private final NativeEnvironment environment = new MacEnvironment();

    @Override
    public NativeEnvironment getEnvironment() {
        return this.environment;
    }

    @Override
    public NativeImage newNativeImage(final BufferedImage src) {
        return new MacNativeImage(src);
    }

    @Override
    public TranslucentWindow newTransparentWindow() {
        return new MacTranslucentWindow();
    }

}
