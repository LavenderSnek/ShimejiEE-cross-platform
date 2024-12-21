package com.group_finity.mascotnative.panama;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascotnative.panama.bindings.render.Image;
import com.group_finity.mascotnative.panama.bindings.render.ImageLoadingOptions;
import com.group_finity.mascotnative.panama.bindings.render.NativeRenderer_h;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.file.Path;

public class PanamaImage implements NativeImage, AutoCloseable {
    final Arena arena;
    final MemorySegment image;
    public final int w;
    public final int h;

    private PanamaImage(Arena arena, MemorySegment image, int w, int h) {
        this.arena = arena;
        this.image = image;
        this.w = w;
        this.h = h;
    }

    public static PanamaImage loadFrom(Path path, double scaling, boolean flipped, boolean antialiasing) {
        try (var a = Arena.ofConfined()) {
            var cPath = a.allocateFrom(path.toAbsolutePath().toString());

            var opts = ImageLoadingOptions.allocate(a);
            ImageLoadingOptions.scaling(opts, scaling);
            ImageLoadingOptions.flipped(opts, flipped);
            ImageLoadingOptions.anti_alias(opts, antialiasing);

            var arena = Arena.ofShared();
            var img = NativeRenderer_h.image_load(arena, cPath, opts);
            return new PanamaImage(arena, img, Image.w(img), Image.h(img));
        }
    }

    @Override
    public void close() {
        NativeRenderer_h.image_dispose(image);
        arena.close();
    }
}
