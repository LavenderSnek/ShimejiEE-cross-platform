package com.group_finity.shimejiutils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ResourceBundle;
import java.util.function.Consumer;

abstract class AbstractConfigFile{

    private static final DocumentBuilder docBuilder;

    static {
        DocumentBuilder builder = null;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        docBuilder = builder;
    }

    private final Document loadedDocument;
    private final ConfigLang language;

    AbstractConfigFile(Path location) throws IOException, SAXException {
        if (!Files.isRegularFile(location)) {
            throw new NoSuchFileException(location.toAbsolutePath().toString());
        }

        Document doc = docBuilder.parse(location.toFile());
        ConfigLang tmpLang = null;

        var docEl = doc.getDocumentElement();
        for (ConfigLang value : ConfigLang.values()) {
            NodeList nl = docEl.getElementsByTagName(getTopTagNameFor(value));
            if (nl.getLength() > 0) {
                tmpLang = value;
                break;
            }
        }

        if (tmpLang == null) {
            throw new SAXException(
                    "Unable to determine language of file. Make sure it is the correct type of config file."
                    + "\npath:" + location.toAbsolutePath());
        }

        loadedDocument = doc;
        language = tmpLang;
    }

    protected AbstractConfigFile(Document document, ConfigLang language) {
        this.loadedDocument = document;
        this.language = language;
    }

    protected abstract String getTopTagNameFor(ConfigLang configLang);

    /**
     * @param translationKey The translation key of the tag from schema properties
     */
    public void forEachElementWithTagName(String translationKey, Consumer<Element> consumer) {
        NodeList nodeList = getDocument()
                .getDocumentElement()
                .getElementsByTagName(getRb().getString(translationKey));

        for (int i = 0; i < nodeList.getLength(); i++) {
            var node = nodeList.item(i);
            if (node instanceof Element element) {
                consumer.accept(element);
            }
        }
    }

    public Document getDocument() {
        return loadedDocument;
    }

    public ConfigLang getLanguage() {
        return language;
    }

    /**
     * A convenience method {@link #getLanguage()}.{@link ConfigLang#getRb() getRb()}
     */
    public ResourceBundle getRb() {
        return getLanguage().getRb();
    }

    /**
     * Writes the current document (including modifications), to the specified file.
     * @param pathToFile The file location
     */
    public void writeToFile(Path pathToFile) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        DOMSource source = new DOMSource(getDocument());
        StreamResult result = new StreamResult(pathToFile.toFile());
        transformer.transform(source, result);
    }

}
