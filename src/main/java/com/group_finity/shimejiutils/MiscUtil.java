package com.group_finity.shimejiutils;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

public class MiscUtil {

    private static DocumentBuilder docBuilder;
    private static Transformer transformer;

    static {
        try {
            docBuilder = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
            transformer = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException | ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private MiscUtil() {
    }

    public static Document parseDoc(Path path) throws IOException, SAXException {
        return docBuilder.parse(path.toFile());
    }

    public static void writeDocToFile(Document doc, Path pathToFile) throws TransformerException {
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(pathToFile.toFile());
        transformer.transform(source, result);
    }

    public static void forEachAttrIn(Element el, Consumer<Attr> attrConsumer) {
        var attrs = el.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            var node = attrs.item(i);
            if (node instanceof Attr attr) {
                attrConsumer.accept(attr);
            }
        }
    }

    public static void forEachElementIn(Document doc, Consumer<Element> elementConsumer) {
        forEachElementWithTagName(doc, "*", elementConsumer);
    }

    public static void forEachElementWithTagName(Document doc, String targetTagName, Consumer<Element> elementConsumer) {
        var elements = doc.getElementsByTagName(targetTagName);
        for (int i = 0; i < elements.getLength(); i++) {
            var node = elements.item(i);
            if (node instanceof Element el) {
                elementConsumer.accept(el);
            }
        }
    }

    public static Map<String, String> propertiesToMap(Path propsPath) {
        Properties props = new Properties();

        if (propsPath != null && Files.isRegularFile(propsPath)) {
            try (var ins = new InputStreamReader(new FileInputStream(propsPath.toString()), StandardCharsets.UTF_8)) {
                props.load(ins);
            } catch (IOException ignored) {
            }
        }

        Map<String, String> ret = new HashMap<>();
        props.forEach((k, v) -> ret.put((String) k, (String) v));

        return ret;
    }

}
