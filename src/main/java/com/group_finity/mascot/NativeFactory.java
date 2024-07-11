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

    private static final NativeFactory instance;

    private static final String NATIVE_PKG_PROP = "com.group_finity.mascotnative";

    protected static final Path LIB_DIR;

    static {
        //---init lib folder path
        Path jarDir;
        try {
            jarDir = Path.of(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        LIB_DIR = jarDir.resolve("lib");

        //---pick native pkg
        final String os = System.getProperty("os.name").toLowerCase();

        String subpkg = System.getProperty(NATIVE_PKG_PROP);
        if (subpkg == null) {
            if (os.startsWith("windows")) {
                subpkg = "win";
            } else if (os.startsWith("mac") || os.startsWith("darwin")) {
                subpkg = "macjni";
            } else {
                subpkg = "generic";
            }
        }

        //---load native pkg
        try {
            @SuppressWarnings("unchecked")
            final Class<? extends NativeFactory> impl = (Class<? extends NativeFactory>) Class
                    .forName(NATIVE_PKG_PROP + "." + subpkg + ".NativeFactoryImpl");

            instance = impl.getDeclaredConstructor().newInstance();

        } catch (final Exception e) {
            System.err.println("ERROR: could not load native code package");
            throw new RuntimeException(e);
        }
    }

    public static NativeFactory getInstance() {
        return instance;
    }

    public abstract NativeEnvironment getEnvironment();

    public abstract NativeImage newNativeImage(BufferedImage src);

    public abstract TranslucentWindow newTransparentWindow();

}
