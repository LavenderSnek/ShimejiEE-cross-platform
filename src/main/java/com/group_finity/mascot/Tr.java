package com.group_finity.mascot;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class Tr {

    private Tr() {}

    private static final String LANGUAGE_RB = "language";
    private static final String BEHAVIORNAMES_RB = "behaviornames";

    private static final Map<String, String> userBehaviorNames = new HashMap<>();
    private static ResourceBundle languageBundle;
    private static ResourceBundle behaviorNamesBundle;

    static {
        loadLanguage(Locale.getDefault());
    }

    public static synchronized void setCustomBehaviorTranslations(Map<String, String> translations) {
        userBehaviorNames.clear();
        userBehaviorNames.putAll(translations);
    }

    public static synchronized void loadLanguage(Locale locale) {
        languageBundle = ResourceBundle.getBundle(LANGUAGE_RB, locale);
        behaviorNamesBundle = ResourceBundle.getBundle(BEHAVIORNAMES_RB, locale);
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
            return userBehaviorNames.get(behaviorName);
        }
        if (behaviorNamesBundle.containsKey(behaviorName)) {
            return behaviorNamesBundle.getString(behaviorName);
        }
        return behaviorName;
    }
}
