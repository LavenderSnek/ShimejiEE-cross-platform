package com.group_finity.mascotnative.macjni;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.NativeFactory;
import com.group_finity.mascot.environment.NativeEnvironment;
import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.window.TranslucentWindow;
import com.group_finity.mascotnative.macclassic.MacEnvironment;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;

@SuppressWarnings("unused")
public class NativeFactoryImpl extends NativeFactory {

    private static final String LIB_FILENAME = "libShimejiMacJni.dylib";

    static {
        // Native code crashes if the toolkit hasn't been loaded first
        Toolkit.getDefaultToolkit();
        try {
            System.load(Main.JAR_PARENT_DIR.resolve("lib").resolve(LIB_FILENAME).toAbsolutePath().toString());
        } catch (UnsatisfiedLinkError error) {
            throw new RuntimeException(
                    "Unable to load library for macjni."
                    + "\nMake sure " + LIB_FILENAME + " is in the lib folder of the jar file's parent directory."
                    + "\nIf you would like to use the swing implementation instead, "
                    + "run java with this argument: -Dcom.group_finity.mascotnative=\"macclassic\""
            );
        }
    }

    private final NativeEnvironment environment;
    {
        environment =  Boolean.getBoolean("com.group_finity.mascotnative.macjni.ClassicEnv")
                ? new MacEnvironment()
                : new MacJniEnvironment();
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
