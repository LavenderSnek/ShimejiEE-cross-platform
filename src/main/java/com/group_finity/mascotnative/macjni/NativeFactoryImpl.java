package com.group_finity.mascotnative.macjni;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.NativeFactory;
import com.group_finity.mascot.environment.Environment;
import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;

import java.awt.image.BufferedImage;

public class NativeFactoryImpl extends NativeFactory {

    private static final String LIB_FILENAME = "libShimejiMacJni.dylib";

    static {
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

    private final Environment environment = new MacJniEnvironment();

    @Override
    public Environment getEnvironment() {
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
