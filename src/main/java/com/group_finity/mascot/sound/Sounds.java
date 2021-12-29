package com.group_finity.mascot.sound;

import com.group_finity.mascot.Main;

import javax.sound.sampled.Clip;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * This static class contains all the sounds loaded by Shimeji-ee.
 * <p>
 * Visit kilkakon.com/shimeji for updates
 *
 * @author Kilkakon
 */
public class Sounds {

    private final static Hashtable<String, Clip> SOUNDS = new Hashtable<String, Clip>();

    public static void load(final String filename, final Clip clip) {
        if (!SOUNDS.containsKey(filename)) {
            SOUNDS.put(filename, clip);
        }
    }

    public static boolean contains(String identifier) {
        return SOUNDS.containsKey(identifier);
    }

    public static Clip getSound(String identifier) {
        if (!SOUNDS.containsKey(identifier)) {
            return null;
        }
        return SOUNDS.get(identifier);
    }

    private static ArrayList<Clip> getSoundsIgnoringVolume(String imageSetName, String filename) {
        String namePath;
        ArrayList<Clip> ret = new ArrayList<>(5);
        try {
            namePath = Main.getInstance().getProgramFolder().getSoundFilePath(imageSetName, filename).toString();
        } catch (FileNotFoundException e) {
            return ret;
        }

        for (String name : SOUNDS.keySet()) {
            if (name.startsWith(namePath)) {
                ret.add(SOUNDS.get(name));
            }
        }

        return ret;
    }

    public static void muteSpecifiedSound(String imageSetName, String filename) {
        ArrayList<Clip> affectedSounds = getSoundsIgnoringVolume(imageSetName, filename);
        for (Clip clip : affectedSounds) {
            if (clip != null && clip.isRunning()) {
                clip.stop();
            }
        }
    }

    public static boolean isMuted() {
        return !Main.getInstance().isSoundAllowed();
    }

    public static void setMuted(boolean mutedFlag) {
        if (mutedFlag) {
            // mute everything
            for (Clip clip : SOUNDS.values()) {
                clip.stop();
            }
        }
    }

}
