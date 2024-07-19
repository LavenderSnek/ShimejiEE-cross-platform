package com.group_finity.mascot.imageset;

public interface ImageSetStore {

    ImageSet get(String name);

    default ImageSet getAsDependency(String name, String dependent) { return get(name); }

}
