package com.group_finity.mascot;

import com.group_finity.mascot.Main;

public class Tr {

    /**
     * Translates a string. Throws an error if the string is not present in the language resource bundle.
     * @param key property key
     * @return translated string.
     */
    public static String tr(String key) {
        return Main.getInstance().getLanguageBundle().getString(key);
    }

    /**
     * Translates a behavior name. Return the input if the name is not present in the behaviornames resource bundle.
     * @param behaviorName behavior name
     * @return translated name if available, input name otherwise.
     */
    public static String trBv(String behaviorName) {
        if (Main.getInstance().getBehaviorNamesBundle().containsKey(behaviorName)) {
            return Main.getInstance().getBehaviorNamesBundle().getString(behaviorName);
        }
        return behaviorName;
    }
}
