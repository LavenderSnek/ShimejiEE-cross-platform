package com.group_finity.mascot.imageset;

import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.image.ImagePairStore;
import com.group_finity.mascot.sound.SoundStore;

public interface ImageSet extends AutoCloseable {

    Configuration getConfiguration();

    ImagePairStore getImagePairs();

    SoundStore getSounds();

    @Override
    default void close() throws Exception {
        getImagePairs().disposeAll();
        getSounds().disposeAll();
    }
}
