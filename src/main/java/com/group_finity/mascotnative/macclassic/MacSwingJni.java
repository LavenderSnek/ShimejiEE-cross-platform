package com.group_finity.mascotnative.macclassic;

import com.group_finity.mascot.Main;
import com.sun.jdi.NativeMethodException;

import java.awt.Window;

/**
 * @author snek
 * */
class MacSwingJni {

    private static final String LIB_FILENAME = "libShimejiMacNative.dylib";
    static boolean loaded;

    private MacSwingJni() {
    }

    static {
        try {
            System.load(Main.JAR_PARENT_DIR.resolve("lib").resolve(LIB_FILENAME).toAbsolutePath().toString());
            loaded = true;
        } catch (UnsatisfiedLinkError error){
            error.printStackTrace();
            loaded = false;
        }
    }

    static final int NSStatusWindowLevel = 25;

    /**
     * Tries to set the window level to the specified level.
     * <p>
     * This might not succeed if the window is not correctly configured for that level.
     * For example, only undecorated frames can have certain window levels.
     * <p>
     * You also must not call setAlwaysOnTop with this method as it resets the window levels.
     * */
    static void setNSWindowLevel(Window window, int nsWindowLevel) {
        if (!loaded) {
            throw new NativeMethodException("Native library not loaded.");
        } else {
            try {
                window.pack(); // makes sure it has a native peer first
                nativeSetNSWindowLevel(window, nsWindowLevel);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    private static native void nativeSetNSWindowLevel(Window window, int nsWindowLevel);


}
