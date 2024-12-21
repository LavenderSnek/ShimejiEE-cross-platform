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

    protected static Path nativeLibDir;

    public static void init(String subpkg, Path libDir) {
        if (instance != null) {
            instance.shutdown();
        }

        nativeLibDir = libDir;

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

    public static NativeFactory getInstance() {
        return instance;
    }

    public abstract NativeEnvironment getEnvironment();

    public abstract NativeImage newNativeImage(BufferedImage src);

    public NativeImage newNativeImage(Path path, double scaling, boolean flipped, boolean antialiasing) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public abstract TranslucentWindow newTransparentWindow();

    protected void shutdown() {}

}
