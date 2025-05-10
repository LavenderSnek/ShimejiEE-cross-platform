package com.group_finity.mascotapp.prefs;

import com.group_finity.mascot.imageset.ShimejiProgramFolder;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

// set at the beginning and cant be changed (or need observers to be changed)
// these also need special/no serialization
public class ComplexPrefs {

    public String Language = null;

    public String ProgramFolder = null;
    public String ProgramFolder_conf = null;
    public String ProgramFolder_img = null;
    public String ProgramFolder_sound = null;
    public String ProgramFolder_mono = null;

    public ShimejiProgramFolder getProgramFolder(ShimejiProgramFolder base) {
        if (ProgramFolder != null) {
            base = ShimejiProgramFolder.fromFolder(Path.of(ProgramFolder));
        }

        return new ShimejiProgramFolder(
                ProgramFolder_conf != null ? Path.of(ProgramFolder_conf) : base.confPath(),
                ProgramFolder_img != null ? Path.of(ProgramFolder_img) : base.imgPath(),
                ProgramFolder_sound != null ? Path.of(ProgramFolder_sound) : base.soundPath(),
                ProgramFolder_mono != null ? Boolean.parseBoolean(ProgramFolder_mono) : base.isMonoImageSet());
    }

    public String ActiveShimeji = null;

    public Set<String> getValidActiveImageSets(ShimejiProgramFolder pf) {
        var ret = new HashSet<String>();

        if (pf.isMonoImageSet()) {
            ret.add("");
            return ret;
        }
        if (ActiveShimeji == null) {
            return ret;
        }

        var selections = ActiveShimeji.split("/");

        for (String s : selections) {
            var decoded = URLDecoder.decode(s, StandardCharsets.UTF_8);
            var p = pf.imgPath().resolve(decoded);
            if (Files.isDirectory(p)) {
                ret.add(decoded);
            }
        }

        ret.remove("");

        return ret;
    }

    public static String serializeActiveImageSets(Collection<String> sets) {
        var sb = new StringBuilder();
        for (String set : sets) {
            sb.append(URLEncoder.encode(set, StandardCharsets.UTF_8)).append('/');
        }
        return sb.toString();
    }

}
