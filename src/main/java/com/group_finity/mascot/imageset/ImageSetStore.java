package com.group_finity.mascot.imageset;

public interface ImageSetStore {

    ImageSet get(String name);

    default ImageSet getAsDependency(String imageSet, String dependent) { return get(imageSet); }

}
