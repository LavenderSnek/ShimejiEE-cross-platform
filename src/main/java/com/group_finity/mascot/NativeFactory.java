package com.group_finity.mascot;

import com.group_finity.mascot.environment.NativeEnvironment;
import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.window.TranslucentWindow;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

/**
 * Picks the appropriate package of native code based on the operating system
 */
public abstract class NativeFactory {

    private static final String NATIVE_PKG = "com.group_finity.mascotnative";

    private static NativeFactory instance;

    protected static Path LIB_DIR;

    static void init(String subpkg, Path libDir) {
        LIB_DIR = libDir;

        try {
            @SuppressWarnings("unchecked")
            final Class<? extends NativeFactory> impl = (Class<? extends NativeFactory>) Class
                    .forName(NATIVE_PKG + "." + subpkg + ".NativeFactoryImpl");

            instance = impl.getDeclaredConstructor().newInstance();

        } catch (Error | Exception e) {
            System.err.println("ERROR: could not load native code package");
            throw new RuntimeException(e);
        }
    }

    static void init() {
        //---pick native pkg
        final String os = System.getProperty("os.name").toLowerCase();

        String subpkg = System.getProperty(NATIVE_PKG);
        if (subpkg == null) {
            if (os.startsWith("windows")) {
                subpkg = "win";
            } else if (os.startsWith("mac") || os.startsWith("darwin")) {
                subpkg = "macjni";
            } else {
                subpkg = "generic";
            }
        }

        //---init lib folder path
        Path jarDir;
        try {
            jarDir = Path.of(NativeFactory.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        } catch (Error | Exception e) {
            throw new RuntimeException(e);
        }

        //---init
        init(subpkg, jarDir.resolve("lib"));
    }

    static {
        init();
    }

    public static NativeFactory getInstance() {
        return instance;
    }

    public abstract NativeEnvironment getEnvironment();

    public abstract NativeImage newNativeImage(BufferedImage src);

    public abstract TranslucentWindow newTransparentWindow();

    protected void shutdown() {}

}
