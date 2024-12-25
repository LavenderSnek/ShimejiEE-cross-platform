package com.group_finity.mascot.sound;

import javax.sound.sampled.Clip;
import java.util.List;

public interface SoundStore {

    /**
     * Loads the sound and returns they key.
     * <p>
     * Each volume of the same sound may be considered a separate clip sound.
     *
     * @param soundText The raw sound text.
     * @param volume    The parsed volume attribute.
     * @return A key to loaded copy of the sound
     */
    String load(String soundText, float volume) throws Exception;

    /**
     * Gets the sound corresponding to the key.
     * @param key The key to the sound obtained from {@link #load(String, float)}.
     * @return The clip/sound if it has been loaded, null otherwise.
     */
    Clip get(String key);

    /**
     * A list of clips loaded from the given filename.
     * @param name The raw sound text.
     */
    List<Clip> getIgnoringVolume(String name);


    /**
     * Clears store and releases resources used by all sounds
     */
    void disposeAll() throws Exception;

}
