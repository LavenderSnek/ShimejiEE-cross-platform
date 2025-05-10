package com.group_finity.mascotapp;

import com.group_finity.mascot.NativeFactory;

import java.nio.file.Path;

public class Constants {

    public static final String APP_VERSION = "2.1.0";

    public static final Path JAR_DIR;

    public static final Path NATIVE_LIB_DIR;

    public static final String NATIVE_PKG_DEFAULT;

    public static final String PREF_PROP_PREFIX = "com.group_finity.mascot.prefs.";

    // languages
    public static final String[][] LANGUAGE_TABLE = {
            //{{langName, langCode}}
            {"English", "en-GB"}, //English
            {"Català", "ca-ES"},//Catalan
            {"Deutsch", "de-DE"},//German
            {"Español", "es-ES"},//Spanish
            {"Français Canadien", "fr-CA"}, // Canadian french
            {"Hrvatski", "hr-HR"},//Croatian
            {"Italiano", "it-IT"},//Italian
            {"Nederlands", "nl-NL"},//Dutch
            {"Polski", "pl-PL"},//Polish
            {"Português Brasileiro", "pt-BR"},//Brazilian Portuguese
            {"Português", "pt-PT"},//Portuguese
            {"ру́сский язы́к", "ru-RU"},//Russian
            {"Română", "ro-RO"},//Romanian
            {"Srpski", "sr-RS"},//Serbian
            {"Suomi", "fi-FI"},//Finnish
            {"tiếng Việt", "vi-VN"},//Vietnamese
            {"简体中文", "zh-CN"},//Chinese(simplified)
            {"繁體中文", "zh-TW"},//Chinese(traditional)
            {"한국어", "ko-KR"},// Korean
            {"\u0639\u0631\u0628\u064A","ar"}//arabic
    };


    // user toggles
    public static final String[] USER_SWITCH_KEYS = {
            "Breeding",
            "Transients",
            "Transformation",
            "Throwing",
            "Sounds",
            "TranslateBehaviorNames",
            "AlwaysShowShimejiChooser",
            "IgnoreImagesetProperties"
    };

    //--- image set settings
    public static final String[] IMGSET_DEFAULTS_KEYS = {
            "Scaling",
            "LogicalAnchors",
            "AsymmetryNameScheme",
            "PixelArtScaling",
            "FixRelativeGlobalSound"
    };

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
            NATIVE_PKG_DEFAULT = "panama";
        } else {
            NATIVE_PKG_DEFAULT = "generic";
        }
    }

}
