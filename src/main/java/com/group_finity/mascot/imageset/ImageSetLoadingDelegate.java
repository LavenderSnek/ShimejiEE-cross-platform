package com.group_finity.mascot.imageset;

public interface ImageSetLoadingDelegate {
    ImageSet load(String name);

    default ImageSet loadAsDependency(String name, String dependent) {
        return load(name);
    }
}
