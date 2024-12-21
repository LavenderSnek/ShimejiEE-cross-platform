
#include "Renderer.h"

NSImage* flipImageHorizontally(NSImage * inputImage) {
    NSImage *tmpImage;
    NSAffineTransform *transform = [NSAffineTransform transform];

    NSSize dimensions = [inputImage size];
    NSAffineTransformStruct flip = {-1.0, 0.0, 0.0, 1.0,
                                    dimensions.width, 0.0 };

    tmpImage = [[NSImage alloc] initWithSize:dimensions];
    [tmpImage lockFocus];
    [transform setTransformStruct:flip];
    [transform concat];

    [inputImage drawAtPoint:NSMakePoint(0,0)
                   fromRect:NSMakeRect(0,0, dimensions.width, dimensions.height)
                  operation:NSCompositingOperationCopy fraction:1.0];

    [tmpImage unlockFocus];

    return tmpImage;
}

struct Image image_load(char *path, struct ImageLoadingOptions options) {
    struct Image r;

    NSString *p = [NSString stringWithUTF8String:path];
    NSImage* img = [[NSImage alloc] initWithContentsOfFile:p];

    if (img != nil) {
        r.data = img;
        r.w = (int)round(([img size].width) * options.scaling);
        r.h = (int)round(([img size].height) * options.scaling);

        if (options.flipped) {
            r.data = flipImageHorizontally(img);
            [img release];
        }
    } else {
        r.w = 0;
        r.h = 0;
    }

    return r;
}

void image_dispose(struct Image image) {
    [(NSImage*)image.data release];
}
