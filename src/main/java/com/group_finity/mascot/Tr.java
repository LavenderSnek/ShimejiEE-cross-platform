package com.group_finity.mascot;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.ResourceBundle;

public class Tr {

    private Tr() {}

    private static final String USER_BEHAVIORNAMES_FILE = "user-behaviornames.properties";
    private static final String LANGUAGE_RB = "language";
    private static final String BEHAVIORNAMES_RB = "behaviornames";

    private static final Properties userBehaviorNames = new Properties();
    private static ResourceBundle languageBundle;
    private static ResourceBundle behaviorNamesBundle;

    static {
        Path userBehaviorNamesFp = Main.getInstance().getProgramFolder().confPath().resolve(USER_BEHAVIORNAMES_FILE);
        if (Files.isRegularFile(userBehaviorNamesFp)) {
            try (var ins = new InputStreamReader(new FileInputStream(userBehaviorNamesFp.toString()))) {
                userBehaviorNames.load(ins);
            } catch (IOException ignored) {
            }
        }
    }

    public static synchronized void loadLanguage() {
        try {
            languageBundle = ResourceBundle.getBundle(LANGUAGE_RB, Main.getInstance().getLocale());
            behaviorNamesBundle = ResourceBundle.getBundle(BEHAVIORNAMES_RB, Main.getInstance().getLocale());
        } catch (Exception ex) {
            Main.showError("The language files could not be loaded. Make sure java is set up properly");
        }
    }

    /**
     * Translates a string. Throws an error if the string is not present in the language resource bundle.
     *
     * @param key property key
     * @return translated string.
     */
    public static String tr(String key) {
        return languageBundle.getString(key);
    }

    /**
     * Translates a behavior name. Return the input if the name is not present in the behaviornames resource bundle.
     *
     * @param behaviorName behavior name
     * @return translated name if available, input name otherwise.
     */
    public static String trBv(String behaviorName) {
        if (userBehaviorNames.containsKey(behaviorName)) {
            return userBehaviorNames.getProperty(behaviorName);
        }
        if (behaviorNamesBundle.containsKey(behaviorName)) {
            return behaviorNamesBundle.getString(behaviorName);
        }
        return behaviorName;
    }
}
