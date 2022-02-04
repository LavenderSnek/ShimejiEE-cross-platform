package com.group_finity.shimejiutils;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class BehaviorConfigFile extends AbstractConfigFile {

    public static final String[] POSSIBLE_FILENAMES = {
            "behaviors.xml", "behavior.xml",
            "行動.xml",
            "#U884c#U52d5.xml",
            "ÞíîÕïò.xml", "µ¦-.xml", "ìsô«.xml",
            "two.xml", "2.xml",
    };

    public BehaviorConfigFile(Path location) throws IOException, SAXException {
        super(location);
    }

    public BehaviorConfigFile(Document document, ConfigLang language) {
        super(document, language);
    }

    @Override
    protected String getTopTagNameFor(ConfigLang configLang) {
        switch (configLang) {
            case EN -> {return "BehaviorList";}
            case JP -> {return "行動リスト";}
            default -> {return null;}
        }
    }

}
