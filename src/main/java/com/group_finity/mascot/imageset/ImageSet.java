package com.group_finity.mascot.imageset;

import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.image.ImagePairStore;
import com.group_finity.mascot.sound.SoundStore;

public interface ImageSet {

    Configuration getConfiguration();

    ImagePairStore getImagePairs();

    SoundStore getSounds();

}
