package com.group_finity.mascot.sound;

import com.group_finity.mascot.imageset.ShimejiProgramFolder;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SoundLoader implements SoundStore {

    private final Map<String, Clip> sounds = new ConcurrentHashMap<>(4, 0.75f, 2);
    private final Map<String, List<String>> fileNameMap = new ConcurrentHashMap<>(4, 0.75f, 2);

    private final ShimejiProgramFolder programFolder;
    private final String imageSetName;

    private boolean fixRelativeGlobalSound = false;

    public SoundLoader(ShimejiProgramFolder programFolder, String imageSetName) {
        this.programFolder = programFolder;
        this.imageSetName = imageSetName;
    }

    @Override
    public String load(String soundText, float volume) throws IOException, UnsupportedAudioFileException, LineUnavailableException {

        final String key = volume + ":" + soundText;
        if (sounds.containsKey(key)) {
            return key;
        }

        Path path = null;

        try {
            path = getProgramFolder().getSoundFilePath(getImageSetName(), soundText);
        } catch (FileNotFoundException e) {
            if (!fixRelativeGlobalSound) {
                throw e;
            }
            else if (soundText.toLowerCase(Locale.ROOT).replaceAll("^/+", "").startsWith("../../sound/")) {
                String cleanSoundText = soundText.toLowerCase(Locale.ROOT)
                        .replaceAll("^/+", "")
                        .replaceFirst("\\.\\./\\.\\./sound/", "")
                        .replaceAll("^/+", "");
                path = getProgramFolder().getSoundFilePath(getImageSetName(), cleanSoundText);
            }
        }
        assert path != null;

        fileNameMap.putIfAbsent(soundText, new ArrayList<>(4));
        fileNameMap.get(soundText).add(key);

        Clip clip = createClipFrom(path, volume);
        sounds.put(key, clip);

        return key;
    }

    @Override
    public Clip get(String key) {
        return key == null ? null : sounds.get(key);
    }

    @Override
    public List<Clip> getIgnoringVolume(String name) {
        return fileNameMap.get(name).stream().map(this::get).toList();
    }

    private ShimejiProgramFolder getProgramFolder() {
        return programFolder;
    }

    private String getImageSetName() {
        return imageSetName;
    }

    public boolean isFixRelativeGlobalSound() {
        return fixRelativeGlobalSound;
    }

    /**
     * Whether to automatically fix missing sound files when they begin with '/../../sound/'
     * <p>
     * This was a common way to make sound files global before the global sound folder was introduced.
     */
    public void setFixRelativeGlobalSound(boolean fixRelativeGlobalSound) {
        this.fixRelativeGlobalSound = fixRelativeGlobalSound;
    }

    private static Clip createClipFrom(Path soundPath, float volume) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundPath.toFile());

        Clip clip = AudioSystem.getClip();

        clip.open(audioInputStream);
        ((FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN)).setValue(volume);

        clip.addLineListener(e -> {
            if (e.getType() == LineEvent.Type.STOP) {
                ((Clip) e.getLine()).stop();
            }
        });

        return clip;
    }

}
