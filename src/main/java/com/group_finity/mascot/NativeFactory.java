package com.group_finity.mascot;

import com.group_finity.mascot.environment.Environment;
import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;
import com.sun.jna.Platform;

import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;

/**
 * Picks the appropriate package of native code based on the operating system
 */
public abstract class NativeFactory {

    private static final NativeFactory instance;

    static {
        String subpkg = "generic";

        if (Platform.isWindows()) {
            subpkg = "win";
        } else if (Platform.isMac()) {
            subpkg = "mac";
        }

        String basepkg = "com.group_finity.mascotnative";

        try {
            @SuppressWarnings("unchecked")
            final Class<? extends NativeFactory> impl = (Class<? extends NativeFactory>) Class
                    .forName(basepkg + "." + subpkg + ".NativeFactoryImpl");

            instance = impl.getDeclaredConstructor().newInstance();

        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            System.err.println("ERROR: could not load native code package");
            throw new RuntimeException(e);
        }
    }

    public static NativeFactory getInstance() {
        return instance;
    }

    public abstract Environment getEnvironment();

    public abstract NativeImage newNativeImage(BufferedImage src);

    public abstract TranslucentWindow newTransparentWindow();

}
