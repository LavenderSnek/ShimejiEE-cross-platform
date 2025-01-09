package com.group_finity.mascot;

import com.group_finity.mascot.environment.NativeEnvironment;
import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.window.NativeRenderer;
import com.group_finity.mascot.window.TranslucentWindow;
import com.group_finity.mascot.window.TranslucentWindowEventHandler;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

    public abstract NativeImage newNativeImage(Path path, double scaling, boolean flipped, boolean antialiasing) throws IOException;

    private static class DfltRenderer implements NativeRenderer {
        private ConcurrentMap<Integer, TranslucentWindow> windows = new ConcurrentHashMap<>();

        private static final NativeRenderer renderer = new DfltRenderer();
        private DfltRenderer() {}

        public static NativeRenderer getInstance() {
            return renderer;
        }

        @Override
        public void createWindow(int id, TranslucentWindowEventHandler callbacks) {
            var w = NativeFactory.getInstance().newTransparentWindow();
            w.setEventHandler(callbacks);
            windows.put(id, w);
        }

        @Override
        public void updateWindow(int id, boolean visible, NativeImage image, Rectangle bounds) {
            var w = windows.get(id);
            if (w != null) {
                w.setVisible(visible);
                if (visible) {
                    w.setImage(image);
                    w.setBounds(bounds);
                }

                w.updateImage();
            }
        }

        @Override
        public void disposeWindow(int id) {
            var w = windows.remove(id);
            if (w != null) {
                w.dispose();
            }
        }
    }

    public NativeRenderer getRenderer() {
        return DfltRenderer.getInstance();
    }

    protected TranslucentWindow newTransparentWindow() {
        throw new UnsupportedOperationException();
    }

    protected void shutdown() {}

}
