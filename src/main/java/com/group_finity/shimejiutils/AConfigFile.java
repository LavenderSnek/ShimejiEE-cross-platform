package com.group_finity.shimejiutils;

import java.nio.file.Path;

abstract class AConfigFile {

    private final Path location;

    protected AConfigFile(Path location) {
        this.location = location;
    }


}
