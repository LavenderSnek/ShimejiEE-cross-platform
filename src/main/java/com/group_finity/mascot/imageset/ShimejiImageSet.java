package com.group_finity.mascot.imageset;

import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.image.ImagePairStore;
import com.group_finity.mascot.sound.SoundStore;

public record ShimejiImageSet(
        Configuration configuration,
        ImagePairStore imagePairs,
        SoundStore sounds
)
implements ImageSet {

    // might be ok to inline this and remove interface, it was a refactoring step for sound/images

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public ImagePairStore getImagePairs() {
        return imagePairs;
    }

    @Override
    public SoundStore getSounds() {
        return sounds;
    }

}
