package com.group_finity.mascot.sound;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * Loads in new Clip objects into the Sounds collection. It will not duplicate
 * sounds already in the collection.
 * <p>
 * Visit kilkakon.com/shimeji for updates
 *
 * @author Kilkakon
 */
public class SoundLoader {

    public static void load(final String name, final float volume) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        if (Sounds.contains(name + volume)) {
            return;
        }

        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(name));

        final Clip clip = AudioSystem.getClip();

        clip.open(audioInputStream);
        ((FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN)).setValue(volume);

        clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                ((Clip) event.getLine()).stop();
            }
        });

        Sounds.load(name + volume, clip);
    }

}
