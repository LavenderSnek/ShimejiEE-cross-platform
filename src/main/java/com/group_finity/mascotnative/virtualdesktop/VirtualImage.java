package com.group_finity.mascotnative.virtualdesktop;

import com.group_finity.mascot.image.NativeImage;

import java.awt.image.BufferedImage;

public record VirtualImage(BufferedImage bufferedImage) implements NativeImage {
}
