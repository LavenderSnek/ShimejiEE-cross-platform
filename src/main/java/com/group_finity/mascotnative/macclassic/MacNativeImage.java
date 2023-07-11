package com.group_finity.mascotnative.macclassic;

import com.group_finity.mascot.image.NativeImage;

import java.awt.image.BufferedImage;

/**
 * Not actually a native image, this is just to separate it from the generic code
 */
record MacNativeImage(BufferedImage bufferedImage) implements NativeImage {
}
