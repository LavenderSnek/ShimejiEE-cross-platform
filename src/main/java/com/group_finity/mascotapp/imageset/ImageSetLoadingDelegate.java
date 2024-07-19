package com.group_finity.mascotapp.imageset;

import com.group_finity.mascot.imageset.ImageSet;

public interface ImageSetLoadingDelegate {
    ImageSet load(String name);

    default ImageSet loadAsDependency(String name, String dependent) {
        return load(name);
    }
}
