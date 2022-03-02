package com.group_finity.shimejiutils.loweffort;

import com.group_finity.shimejiutils.ConfigLang;
import com.group_finity.shimejiutils.MiscUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

class ConfLangTranslator {

    private final Map<String, String> xmlRenameMap;
    private final List<String> xmlRenameKeys;

    private final Map<String, String> scriptTranslations;

    private final Map<String, String> actionTranslations;
    private final List<String> actionTranslationsKeys;

    ConfLangTranslator(Map<String, String> xmlRenameMap, Map<String, String> scriptTranslations, Map<String, String> actionsTranslations) {
        this.xmlRenameMap = xmlRenameMap;
        this.xmlRenameKeys = xmlRenameMap.keySet().stream().toList();
        this.scriptTranslations = scriptTranslations;
        this.actionTranslations = actionsTranslations;
        this.actionTranslationsKeys = actionsTranslations.keySet().stream().toList();
    }

    void translate(Document doc) {
        renameXmlElements(doc);
        // param values - not including names
        MiscUtil.forEachElementIn(doc, el -> {
            if (el.getTagName().equals(xmlRenameMap.getOrDefault("Pose", "Pose"))) {
                return;
            }
            MiscUtil.forEachAttrIn(el, this::translateAttrValue);
        });
    }

    private void translateAttrValue(Attr attr) {
        var av = attr.getValue().trim();

        if ((av.startsWith("${") || av.startsWith("#{")) && av.endsWith("}")) {
            var script = av.substring(2, av.length() - 1).trim();

            if (scriptTranslations.containsKey(script)) {
                attr.setValue(av.charAt(0) + "{" + scriptTranslations.get(script) + "}");
            }
            else if (xmlRenameKeys.stream().anyMatch(script::contains)) {
                System.err.println("UntranslatedScript:" + av);
                attr.setValue("???" + av); // breaks it on purpose, so it can't just be ignored
            }
            else if (actionTranslationsKeys.stream().anyMatch(script::contains)) {
                System.err.println("ScriptMayContainActionName:" + av);
            }
        } else {
            var actVal = actionTranslations.getOrDefault(av, av);
            attr.setValue(xmlRenameMap.getOrDefault(av, actVal));
        }
    }

    private void renameXmlElements(Document doc) {
        MiscUtil.forEachElementIn(doc, el -> {
            if (xmlRenameMap.containsKey(el.getTagName())) {
                doc.renameNode(el, null, xmlRenameMap.get(el.getTagName()));
            }

            MiscUtil.forEachAttrIn(el, attr -> {
                var name = attr.getName();
                if (xmlRenameMap.containsKey(name)) {
                    el.setAttribute(xmlRenameMap.get(name), attr.getValue());
                    el.removeAttribute(name);
                }
            });

            // I don't know why this doesn't work with getAttributes
            if (xmlRenameMap.containsKey("IEの端Y") && el.hasAttribute("IEの端Y")) {
                var ieY = el.getAttributeNode("IEの端Y").getValue();
                el.setAttribute(xmlRenameMap.get("IEの端Y"), ieY);
                el.removeAttribute("IEの端Y");
            }
        });
    }

    //==========Main============//

    private static void printUsage() {
        String msg =
        """
                        
        Partially converts JP shimeji config to EN.
        Manual touch-up is usually required.
                
        Untranslated scripts are prefixed with "???".
        This ensures that possibly broken scripts are reviewed.
                
        Usage:
        [input.xml] [output.xml] <options>
                
        Options:
        --script=<script translation properties path>
            A properties file with only script content (#{} and ${} removed)
                
        --action=<action translation properties path>
            Any keys already available in the JP schema will be ignored.
            user-behaviornames.properties can be used.
                
        """;
        System.err.println(msg);
    }


    public static void main(String[] args) {
        if (args.length < 2) {
            printUsage();
            System.exit(0);
        }

        Map<String, String> argsMap = CliUtil.makeArgsMap(args);

        try {
            translateAndWrite(
                    Path.of(args[0]), Path.of(args[1]),
                    Path.of(argsMap.get("--script")),
                    Path.of(argsMap.get("--action"))
            );
        } catch (Exception e) {
            e.printStackTrace();
            printUsage();
        }
    }

    static void translateAndWrite(Path inPath, Path outPath, Path scriptTrPropPath, Path actionTrPropPath) throws IOException, SAXException, TransformerException {
        if (inPath.toString().isBlank() || outPath.toString().isBlank()) {
            throw new IllegalArgumentException("Input/Output cannot be empty");
        }

        if (!Files.isRegularFile(inPath)) {
            throw new IllegalArgumentException("Input file is not valid: " + inPath.toAbsolutePath());
        }

        var scriptTr = MiscUtil.propertiesToMap(scriptTrPropPath);
        var actionTr = MiscUtil.propertiesToMap(actionTrPropPath);

        var jpRb = ConfigLang.JP.getRb();
        var enRb = ConfigLang.EN.getRb();

        var jpKeys = new HashSet<>(jpRb.keySet());
        jpKeys.removeIf(k -> jpRb.getString(k).equals(enRb.getString(k)));

        Map<String, String> renameMap = new HashMap<>();
        for (String key : jpKeys) {
            renameMap.put(jpRb.getString(key), enRb.getString(key));
        }

        var xmlEnTr = new ConfLangTranslator(renameMap, scriptTr, actionTr);
        var doc = MiscUtil.parseDoc(inPath);

        xmlEnTr.translate(doc);

        MiscUtil.writeDocToFile(doc, outPath);
    }

}
