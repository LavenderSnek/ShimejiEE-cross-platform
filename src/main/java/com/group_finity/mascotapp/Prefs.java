package com.group_finity.mascotapp;


import com.group_finity.mascot.image.ImagePairLoaderBuilder;
import com.group_finity.mascot.image.ImagePairStore;
import com.group_finity.mascot.imageset.ShimejiProgramFolder;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Prefs  {

    private static final Logger log = Logger.getLogger(Prefs.class.getName());

    static String serializeImageSets(Collection<String> sets) {
        var sb = new StringBuilder();
        for (String set : sets) {
            sb.append(URLEncoder.encode(set, StandardCharsets.UTF_8)).append('/');
        }
        return sb.toString();
    }

    static Set<String> getActiveImageSets(ShimejiProgramFolder pf, Map<String, String> prefs) {
        var selectionsProp = Prefs.getSetting(prefs, "ActiveShimeji");
        if (selectionsProp == null) {
            return new HashSet<>(2);
        }

        var ret = new HashSet<String>();
        var selections = selectionsProp.split("/");

        for (String s : selections) {
            var decoded = URLDecoder.decode(s, StandardCharsets.UTF_8);
            var p = pf.imgPath().resolve(decoded);
            if (Files.isDirectory(p)) {
                ret.add(decoded);
            }
        }

        if (pf.isMonoImageSet()) {
            ret.add("");
        } else {
            ret.remove("");
        }

        return ret;
    }

    static ShimejiProgramFolder getProgramFolder(ShimejiProgramFolder base, Map<String, String> prefs) {
        var pfProp = Prefs.getSetting(prefs, "ProgramFolder");
        if (pfProp != null) {
            base = ShimejiProgramFolder.fromFolder(Path.of(pfProp));
        }

        var altConfSp = Prefs.getSetting(prefs, "ProgramFolder.conf");
        var altImgSp = Prefs.getSetting(prefs, "ProgramFolder.img");
        var altSoundSp = Prefs.getSetting(prefs, "ProgramFolder.sound");
        var altMonoSp = Prefs.getSetting(prefs, "ProgramFolder.mono");

        return new ShimejiProgramFolder(
                altConfSp != null ? Path.of(altConfSp) : base.confPath(),
                altImgSp != null ? Path.of(altImgSp) : base.imgPath(),
                altSoundSp != null ? Path.of(altSoundSp) : base.soundPath(),
                altMonoSp != null ? Boolean.parseBoolean(altMonoSp) : base.isMonoImageSet());
    }

    //---Util

    /**
     * Looks for the given pref in system props and then the given map, returns null if found in neither
     * @param prefs the default values
     */
    static String getSetting(Map<String, String> prefs, String key) {
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
            } catch (Exception e) {
                log.log(Level.WARNING, "Unable to read properties:" + path, e);
            }
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
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to write properties:" + path, e);
        }
    }

}
