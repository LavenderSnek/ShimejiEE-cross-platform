package com.group_finity.mascotnative.macjni;

import com.group_finity.mascot.NativeFactory;
import com.group_finity.mascot.environment.Area;
import com.group_finity.mascot.environment.BaseNativeEnvironment;
import com.group_finity.mascot.environment.NativeEnvironment;
import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.window.TranslucentWindow;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

@SuppressWarnings("unused")
public class NativeFactoryImpl extends NativeFactory {

    private static final String LIB_FILENAME = "libShimejiMacJni.dylib";

    static {
        // Native code crashes if the toolkit hasn't been loaded first
        Toolkit.getDefaultToolkit();
        try {
            System.load(nativeLibDir.resolve(LIB_FILENAME).toAbsolutePath().toString());
        } catch (UnsatisfiedLinkError error) {
            throw new RuntimeException(
                    "Unable to load library for macjni."
                    + "\nMake sure " + LIB_FILENAME + " is in the lib folder of the jar file's parent directory."
                    + "\nSet com.group_finity.mascotnative to use a different native implementation."
            );
        }
    }

    private final NativeEnvironment environment;
    {
        var envProp = System.getProperty("com.group_finity.mascotnative.macjni.env", "jni");
        if (envProp.equalsIgnoreCase("generic")) {
            environment = new BaseNativeEnvironment() {
                @Override
                protected void updateIe(Area ieToUpdate) {
                    ieToUpdate.set(new Rectangle(1,1, -10_000, -10_000));
                    ieToUpdate.setVisible(false);
                }
            };
        } else {
            environment = new MacJniEnvironment();
        }
    }

    @Override
    public NativeEnvironment getEnvironment() {
        return environment;
    }

    @Override
    public NativeImage newNativeImage(BufferedImage src) {
        return new MacJniNativeImage(src);
    }

    @Override
    public TranslucentWindow newTransparentWindow() {
        return new MacJniShimejiWindow();
    }

}
