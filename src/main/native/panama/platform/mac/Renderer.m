
#include "Renderer.h"

@implementation Util
+ (void)runOnMainAsync:(void (^)(void))block {
    dispatch_async(dispatch_get_main_queue(), block);
}
+ (void)runOnMainSync:(void (^)(void))block {
    if ([NSThread isMainThread]) {
        block();
    } else {
        dispatch_sync(dispatch_get_main_queue(), block);
    }
}
@end

@interface RenderImageView : NSView
-(void)setImage:(NSImage *)image;
@end

@implementation RenderImageView {
    NSImage* _image;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        [self setCanDrawConcurrently:YES];
    }
    return self;
}

- (void)setImage:(NSImage *)image {
    if (self->_image != image) {
        self->_image = image;
        [self setNeedsDisplay:YES];
    }
}

- (void)drawRect:(NSRect)dirtyRect {
    if (_image) {
        [[NSGraphicsContext currentContext] setImageInterpolation:NSImageInterpolationNone];
        [_image drawInRect:dirtyRect];
    }
}

- (void)dealloc {
    _image = nil;
    [super dealloc];
}
@end

@interface RenderWindow : NSWindow {
    struct RendererCallbacks _callbacks;
}
- (instancetype)initWithCallbacks:(struct RendererCallbacks)callbacks;

- (void)setImage:(NSImage *)image;
@end


@implementation RenderWindow

- (instancetype)initWithCallbacks:(struct RendererCallbacks)callbacks {
    self = [super initWithContentRect:NSMakeRect(0, 0, 120, 120)
                            styleMask:NSWindowStyleMaskBorderless
                              backing:NSBackingStoreBuffered
                                defer:NO];
    if (self) {
        _callbacks = callbacks;
        NSView *cv = [[[RenderImageView alloc] init] autorelease];
        [self setContentView:cv];
        [self setBackgroundColor:NSColor.clearColor];
        [self setLevel:NSStatusWindowLevel];
        [self setCollectionBehavior:NSWindowCollectionBehaviorStationary | NSWindowCollectionBehaviorMoveToActiveSpace |
                                    NSWindowCollectionBehaviorIgnoresCycle |
                                    NSWindowCollectionBehaviorFullScreenAuxiliary];
    }
    return self;
}

- (void)setImage:(NSImage *)image {
    [(RenderImageView *) self.contentView setImage:image];
}

- (void)mouseDown:(NSEvent *)event {
    _callbacks.on_left_mousedown((int) [event locationInWindow].x,
                                 (int) ([self frame].size.height - [event locationInWindow].y));
}

- (void)mouseUp:(NSEvent *)event {
    _callbacks.on_left_mouseup((int) [event locationInWindow].x,
                               (int) ([self frame].size.height - [event locationInWindow].y));
}

- (void)rightMouseUp:(NSEvent *)event {
    @autoreleasepool {
        NSMenu* menu = (NSMenu*)_callbacks.create_menu().data;

        if (menu != nil) {
            [menu autorelease];
            [NSMenu popUpContextMenu:menu withEvent:event forView:self.contentView];
        }
    }
}

@end

void renderer_init_event_loop() {}

struct Renderer renderer_create(struct RendererCallbacks callbacks) {
    __block struct Renderer renderer;

    [Util runOnMainSync: ^ {
        NSWindow* win = [[RenderWindow alloc] initWithCallbacks:callbacks];
        [win setReleasedWhenClosed:YES];
        [win display];

        renderer.data = win;
    }];

    return renderer;
}

void renderer_dispose(struct Renderer renderer) {
    [Util runOnMainAsync: ^ {
        [(NSWindow*)renderer.data close];
    }];
}

void renderer_update(bool visible, struct Renderer renderer, struct Image image, int x, int y) {
    NSRect screenRect = [[[NSScreen screens] firstObject] frame];
    NSRect bounds = NSMakeRect(x, y, image.w, image.h);
    bounds.origin.y = screenRect.size.height - bounds.origin.y - bounds.size.height;

    [Util runOnMainAsync: ^ {
        RenderWindow* win = (RenderWindow*)renderer.data;

        [win setIsVisible:visible ? YES : NO];
        [win setFrame:bounds display:NO];
        [win setImage:(NSImage*)image.data];
        [win displayIfNeeded];
    }];
}
