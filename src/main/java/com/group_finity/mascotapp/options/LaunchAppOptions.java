package com.group_finity.mascotapp.options;

import com.group_finity.mascotapp.Constants;

import java.nio.file.Path;
import java.util.Optional;

import static picocli.CommandLine.*;

// these are all specifiable by cli only, the ui version of the launch code can set all of these before launch
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class LaunchAppOptions {

    @Option(names = {"--log"}, description = "Is logging enabled", negatable = true)
    public boolean log = true;

    @Option(names = {"--tray"}, description = "Is tray menu shown", negatable = true)
    public boolean showTrayMenu = true;

    @Option(names = {"--ic-tray"}, description = "Tray icon override")
    public Optional<Path> trayIcon = Optional.empty();

    // native
    @Option(names = {"--native"}, description = "Renderer/environment")
    public String nativePkg = Constants.NATIVE_PKG_DEFAULT;
}
