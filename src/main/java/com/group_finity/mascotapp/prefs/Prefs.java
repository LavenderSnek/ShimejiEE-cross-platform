package com.group_finity.mascotapp.prefs;


import com.group_finity.mascotapp.Constants;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Prefs  {

    // i should just take the L and use json already (but dependencies)
    private static void deserialize(Object target, Map<String, String> prefs) {
        Class<?> clazz = target.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (!field.accessFlags().contains(AccessFlag.PUBLIC)) { continue; }

            var prefName = field.getName().replace('_', '.');
            var raw = getSetting(prefs, prefName);

            if (raw == null) { continue; }

            try {
                if (field.getType() == double.class) {
                    field.set(target, Double.parseDouble(raw));
                } else if (field.getType() == boolean.class) {
                    field.set(target, Boolean.parseBoolean(raw));
                } else if (field.getType() == String.class) {
                    field.set(target, raw);
                }
            } catch (NumberFormatException | IllegalAccessException _) {
                // ignore
            }
        }
    }

    private static Map<String, String> serialize(Object target) {
        Map<String, String> values = new HashMap<>();
        if (target == null) {
            return values;
        }

        Class<?> clazz = target.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (!field.accessFlags().contains(AccessFlag.PUBLIC)) { continue; }

            try {
                var value = field.get(target);
                if (value != null) {
                    values.put(field.getName().replace('_', '.'), value.toString());
                }
            } catch (IllegalAccessException _) {
            }
        }

        return values;
    }

    public static MutablePrefs deserializeMutable(Map<String, String> prefs) {
        var ret = new MutablePrefs();
        deserialize(ret, prefs);
        return ret;
    }

    public static MutablePrefs deserializeMutable(Map<String, String> prefs, MutablePrefs defaults) {
        var ret = defaults.clone();
        deserialize(ret, prefs);
        return ret;
    }

    public static Map<String, String> serializeMutable(MutablePrefs prefs) {
        return serialize(prefs);
    }

    public static ComplexPrefs deserializeComplex(Map<String, String> prefs) {
        var ret = new ComplexPrefs();
        deserialize(ret, prefs);
        return ret;
    }

    /**
     * Looks for the given pref in system props and then the given map, returns null if found in neither
     * @param prefs the default values
     */
    private static String getSetting(Map<String, String> prefs, String key) {
        var sp = System.getProperty(Constants.PREF_PROP_PREFIX + key);
        if (sp != null) {
            return sp;
        } else if (prefs != null) {
            return prefs.get(key);
        }
        return null;
    }

    /**
     * Tries to read a properties file from the given location
     * <p>
     * Silently fails and returns an empty map on any error
     */
    public static Map<String, String> readProps(Path path) {
        var props = new Properties();

        if (path != null && Files.isRegularFile(path)) {
            try (var input = new InputStreamReader(new FileInputStream(path.toFile()), StandardCharsets.UTF_8)) {
                props.load(input);
            } catch (Exception _) {}
        }

        Map<String, String> ret = new HashMap<>();
        props.forEach((k,v) -> ret.put((String) k, (String) v));

        return ret;
    }

    /**
     * Tries to write the map to a properties file with the given path
     */
    public static void writeProps(Map<String, String> propsMap, Path path) {
        var props = new Properties(propsMap.size());
        props.putAll(propsMap);

        try (var out = new OutputStreamWriter(new FileOutputStream(path.toFile()), StandardCharsets.UTF_8)) {
            props.store(out, path.getFileName().toString());
        } catch (Exception _) {}
    }

}
