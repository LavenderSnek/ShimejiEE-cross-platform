package com.group_finity.mascot.sound;

import com.group_finity.mascot.Main;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Loads in new Clip objects into the Sounds collection. It will not duplicate
 * sounds already in the collection. Different volumes of the same sound are considered different sounds
 *
 * @author <a href="https://kilkakon.com/shimeji/">Kilkakon</a>
 */
public class SoundLoader {

    /**
     * @param imageSetName Name of the image set to search in
     * @param soundFileName Name of the sound file
     * @param volume {@link FloatControl}
     *
     * @return The key with which the sound can be accessed.
     * */
    public static String load(String imageSetName, String soundFileName, final float volume) throws IOException, LineUnavailableException, UnsupportedAudioFileException {

        Path path = Main.getInstance().getProgramFolder().getSoundFilePath(imageSetName, soundFileName).toAbsolutePath();
        String identifier = path + "/" + volume;

        if (Sounds.contains(identifier)) {
            return identifier;
        }

        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(path.toFile());

        final Clip clip = AudioSystem.getClip();

        clip.open(audioInputStream);
        ((FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN)).setValue(volume);

        clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                ((Clip) event.getLine()).stop();
            }
        });

        Sounds.load(identifier, clip);

        return identifier;
    }

}
