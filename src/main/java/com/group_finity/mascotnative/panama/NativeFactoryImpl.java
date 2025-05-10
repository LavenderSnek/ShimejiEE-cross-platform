package com.group_finity.mascotnative.panama;

import com.group_finity.mascot.NativeFactory;
import com.group_finity.mascot.environment.NativeEnvironment;
import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.window.TranslucentWindow;
import com.group_finity.mascotnative.panama.bindings.render.NativeRenderer_h;

import java.awt.*;
import java.nio.file.Path;

@SuppressWarnings("unused")
public class NativeFactoryImpl extends NativeFactory {

    private static final String LIBRARY_PREFIX = "shimejinative";

    private final NativeEnvironment environment = new PanamaEnvironment();

    static {
        // Native code crashes if the toolkit hasn't been loaded first
        try {
            var backend = System.getProperty("com.group_finity.mascotnative.panama", "mac");
            System.load(nativeLibDir.resolve(System.mapLibraryName(LIBRARY_PREFIX + "_" + backend)).toAbsolutePath().toString());
        } catch (UnsatisfiedLinkError error) {
            throw new RuntimeException("Unable to load library for panama.", error);
        }

        NativeRenderer_h.renderer_init_event_loop();
    }

    @Override
    public NativeEnvironment getEnvironment() {
        return environment;
    }

    @Override
    public NativeImage newNativeImage(Path path, double scaling, boolean flipped, boolean antialiasing) {
        return PanamaImage.loadFrom(path, scaling, flipped, antialiasing);
    }

    @Override
    public TranslucentWindow newTransparentWindow() {
        return new PanamaWindow();
    }
}
