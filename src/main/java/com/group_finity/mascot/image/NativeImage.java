package com.group_finity.mascot.image;

/**
 * Raw image used by native implementations
 */
public interface NativeImage {

    int getWidth();
    int getHeight();

    default void dispose() {}

}
