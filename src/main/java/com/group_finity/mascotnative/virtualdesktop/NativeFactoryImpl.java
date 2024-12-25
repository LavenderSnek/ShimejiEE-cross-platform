package com.group_finity.mascotnative.virtualdesktop;

import com.group_finity.mascot.NativeFactory;
import com.group_finity.mascot.environment.NativeEnvironment;
import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.window.TranslucentWindow;
import com.group_finity.mascotnative.shared.ImageUtil;
import com.group_finity.mascotnative.virtualdesktop.display.VirtualEnvironmentDisplay;

import java.io.IOException;
import java.nio.file.Path;

@SuppressWarnings("unused")
public class NativeFactoryImpl extends NativeFactory {

    static final VirtualEnvironmentDisplay display;
    static final VirtualEnvironment environment;

    static {
        display = new VirtualEnvironmentDisplay();
        environment = new VirtualEnvironment(display);
    }

    @Override
    public NativeEnvironment getEnvironment() {
        return environment;
    }

    @Override
    public NativeImage newNativeImage(Path path, double scaling, boolean flipped, boolean antialiasing) throws IOException {
        return new VirtualImage(ImageUtil.newBufferedImage(path, scaling, flipped, antialiasing));
    }

    @Override
    public TranslucentWindow newTransparentWindow() {
        var panel = new VirtualWindowPanel();
        display.addShimejiWindow(panel);
        return panel;
    }
}
