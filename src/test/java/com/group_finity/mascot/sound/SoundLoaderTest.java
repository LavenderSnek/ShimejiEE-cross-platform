package com.group_finity.mascot.sound;

import com.group_finity.mascot.imageset.ShimejiProgramFolder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SoundLoaderTest {

    private static final String IMG_SET_NAME = "shimeji";
    private static final String SOUND_NAME = "/example.wav";
    private static final String REL_SOUND = "/../../sound/example.wav";

    static ShimejiProgramFolder PF;

    @BeforeAll
    static void setUp(@TempDir Path pfBase) throws IOException {
        Files.createDirectories(pfBase.resolve("img"));
        PF = ShimejiProgramFolder.fromFolder(pfBase);
        Path imgSound = PF.imgPath().resolve(IMG_SET_NAME).resolve("sound");
        Files.createDirectories(imgSound);
        Files.createDirectories(PF.soundPath());
        Files.copy(Path.of("src/test/resources", SOUND_NAME), Path.of(imgSound.toString(), SOUND_NAME));
    }

    @Test
    void separateAudioVolumes() throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        var sl = new SoundLoader(PF, IMG_SET_NAME);

        String k1 = sl.load(SOUND_NAME, 3);
        String k2 = sl.load(SOUND_NAME, 2);
        sl.load(SOUND_NAME, 0);

        assertNotEquals(k1, k2);

        List<Clip> allSounds = sl.getIgnoringVolume(SOUND_NAME);
        assertTrue(allSounds.size() >= 3);
    }

    @Test
    void fixRelativeGlobalSound() throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        var sl = new SoundLoader(PF, IMG_SET_NAME);
        sl.setFixRelativeGlobalSound(false);

        boolean fileNotFound = false;
        try {
            sl.load(REL_SOUND, 0);
        } catch (IOException ignored) {
            fileNotFound = true;
        }
        assertTrue(fileNotFound);

        sl = new SoundLoader(PF, IMG_SET_NAME);
        sl.setFixRelativeGlobalSound(true);

        String k2 = sl.load(REL_SOUND, 0);
        assertNotNull(sl.get(k2));
    }

}