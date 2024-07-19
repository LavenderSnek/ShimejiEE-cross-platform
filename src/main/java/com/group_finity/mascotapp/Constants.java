package com.group_finity.mascotapp;

import com.group_finity.mascot.NativeFactory;

import java.nio.file.Path;

public class Constants {

    public static final Path JAR_DIR;

    public static final Path NATIVE_LIB_DIR;

    public static final String NATIVE_PKG_DEFAULT;

    public static final String APP_VERSION = "2.1.0";

    static {
        Path tmp;
        try {
            tmp = Path.of(NativeFactory.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        } catch (Error | Exception e) {
            throw new RuntimeException(e);
        }
        JAR_DIR = tmp;

        NATIVE_LIB_DIR = JAR_DIR.resolve("lib");

        // preparing to get rid of JNA and switch to panama
        final String os = System.getProperty("os.name").toLowerCase();
        if (os.startsWith("windows")) {
            NATIVE_PKG_DEFAULT = "win";
        } else if (os.startsWith("mac") || os.startsWith("darwin")) {
            NATIVE_PKG_DEFAULT = "macjni";
        } else {
            NATIVE_PKG_DEFAULT = "generic";
        }
    }

}
