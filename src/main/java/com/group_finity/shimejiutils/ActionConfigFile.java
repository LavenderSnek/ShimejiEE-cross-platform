package com.group_finity.shimejiutils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

public class ActionConfigFile extends AbstractConfigFile {

    public static final String[] POSSIBLE_FILENAMES = {
            "actions.xml", "action.xml",
            "動作.xml",
            "#U52d5#U4f5c.xml",
            "Õïòõ¢£.xml", "¦-º@.xml", "ô«ìý",
            "one.xml", "1.xml",
    };

    public ActionConfigFile(Path location) throws IOException, SAXException {
        super(location);
    }

    public ActionConfigFile(Document document, ConfigLang language) {
        super(document, language);
    }

    @Override
    protected String getTopTagNameFor(ConfigLang configLang) {
        switch (configLang) {
            case EN -> {return "ActionList";}
            case JP -> {return "動作リスト";}
            default -> {return null;}
        }
    }

    public void forEachPoseElement(Consumer<Element> poseConsumer) {
        // there's no schema key for it, so it's just any child of an animation element
        // the program actually only loads elements that are inside action elements (but this is good enough)
        forEachElementWithTagName("Animation", animElement -> {
            NodeList poseNodes = animElement.getChildNodes();
            for (int i = 0; i < poseNodes.getLength(); i++) {
                if (poseNodes.item(i) instanceof Element poseElement) {
                    poseConsumer.accept(poseElement);
                }
            }
        });
    }

}
