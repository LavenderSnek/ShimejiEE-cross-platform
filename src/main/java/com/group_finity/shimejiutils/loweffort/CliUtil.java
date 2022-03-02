package com.group_finity.shimejiutils.loweffort;

import java.util.HashMap;
import java.util.Map;

public class CliUtil {

    private CliUtil() {
    }

    static Map<String, String> makeArgsMap(String[] args) {
        Map<String, String> ret = new HashMap<>();
        for (String arg : args) {
            if (arg.startsWith("--")) {
                String[] parts = arg.split("=", 2);
                ret.put(parts[0], parts[1]);
            }
        }
        return ret;
    }

}
