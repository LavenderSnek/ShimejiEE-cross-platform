package com.group_finity.shimejiutils;

import java.nio.file.Path;

class BehaviorConfigFile extends AConfigFile {

    public static final String[] POSSIBLE_FILENAMES = {
            "behaviors.xml", "behavior.xml",
            "行動.xml",
            "#U884c#U52d5.xml",
            "ÞíîÕïò.xml", "µ¦-.xml", "ìsô«.xml",
            "two.xml", "2.xml",
    };

    BehaviorConfigFile(Path location) {
        super(location);
    }

}
