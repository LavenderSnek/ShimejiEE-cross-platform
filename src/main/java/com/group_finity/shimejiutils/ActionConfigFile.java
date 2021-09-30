package com.group_finity.shimejiutils;

import java.nio.file.Path;

class ActionConfigFile extends AConfigFile {

    public static final String[] POSSIBLE_FILENAMES = {
            "actions.xml", "action.xml",
            "動作.xml",
            "#U52d5#U4f5c.xml",
            "Õïòõ¢£.xml", "¦-º@.xml", "ô«ìý",
            "one.xml", "1.xml",
    };

    ActionConfigFile(Path location) {
        super(location);
    }
}
