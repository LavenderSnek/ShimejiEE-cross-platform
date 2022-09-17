
#import "ShimejiWindow.h"
#import "JniHelper.h"

@interface ShimejiImageView : NSView

-(void)setImage:(NSImage *)image;

@end

@implementation ShimejiImageView {
    NSImage* image;
}

- (instancetype)init
{
    self = [super init];
    if (self) {
        [self setCanDrawConcurrently:YES];
    }
    return self;
}

- (void)setImage:(NSImage *)image {
    if (self->image != image) {
        self->image = image;
        [self setNeedsDisplay:YES];
    }
}

- (void)drawRect:(NSRect)dirtyRect {
    if (image) {
        [[NSGraphicsContext currentContext] setImageInterpolation:NSImageInterpolationNone];
        [image drawInRect:dirtyRect];
    }
}

- (void)dealloc {
    image = nil;
    [super dealloc];
}

@end

#pragma mark -Peer Window

jclass JC_MacJniShimejiWindow;
jmethodID JMID_MacJniShimejiWindow_onLeftMouseDown;
jmethodID JMID_MacJniShimejiWindow_onLeftMouseUp;
jmethodID JMID_MacJniShimejiWindow_getNSMenuPtrForPopup;

@implementation ShimejiWindow {
    jobject javaRep;
}

+ (void)initialize
{
    if (self == [ShimejiWindow class]) {
        JC_MacJniShimejiWindow = [JniHelper makeGlobalClassRefOf:"com/group_finity/mascotnative/macjni/MacJniShimejiWindow"];
        JMID_MacJniShimejiWindow_onLeftMouseDown = [JniHelper getMethodIdFromClass:JC_MacJniShimejiWindow ofMethodNamed:"_onLeftMouseDown" withSignature:"(II)V"];
        JMID_MacJniShimejiWindow_onLeftMouseUp = [JniHelper getMethodIdFromClass:JC_MacJniShimejiWindow ofMethodNamed:"_onLeftMouseUp" withSignature:"(II)V"];
        JMID_MacJniShimejiWindow_getNSMenuPtrForPopup = [JniHelper getMethodIdFromClass:JC_MacJniShimejiWindow ofMethodNamed:"_getNSMenuPtrForPopup" withSignature:"()J"];
    }
}

-(instancetype)initWithJavaRep:(jobject)javaRep {
    self = [super initWithContentRect:NSMakeRect(0, 0, 120, 120)
                            styleMask:NSWindowStyleMaskBorderless
                              backing:NSBackingStoreBuffered
                                defer:NO];
    if (self) {
        ShimejiImageView* cv = [[[ShimejiImageView alloc]init]autorelease];
        [self setContentView:cv];
        [self setBackgroundColor:NSColor.clearColor];
        [self setLevel:NSStatusWindowLevel];
        [self setCollectionBehavior:NSWindowCollectionBehaviorStationary | NSWindowCollectionBehaviorMoveToActiveSpace | NSWindowCollectionBehaviorIgnoresCycle | NSWindowCollectionBehaviorFullScreenAuxiliary];
        self->javaRep = javaRep;
    }
    return self;
}

-(void)setImage:(NSImage *)image {
    [(ShimejiImageView *)self.contentView setImage:image];
}

- (void)mouseDown:(NSEvent *)event {
    [JniHelper getEnvAndPerform:^(JNIEnv* env){
        (*env)->CallVoidMethod(env, self->javaRep, JMID_MacJniShimejiWindow_onLeftMouseDown,
                               (int) [event locationInWindow].x,
                               (int) ([self frame].size.height - [event locationInWindow].y));
    }];
}

- (void)mouseUp:(NSEvent *)event {
    [JniHelper getEnvAndPerform:^(JNIEnv* env){
        (*env)->CallVoidMethod(env, self->javaRep, JMID_MacJniShimejiWindow_onLeftMouseUp,
                               (int) [event locationInWindow].x,
                               (int) ([self frame].size.height - [event locationInWindow].y));
    }];
}

- (void)rightMouseUp:(NSEvent *)event {
    
    __block jlong menuPtr = 0L;

    [JniHelper getEnvAndPerform:^(JNIEnv* env){
        menuPtr =
        (*env)->CallLongMethod(env, self->javaRep, JMID_MacJniShimejiWindow_getNSMenuPtrForPopup);
    }];

    if (menuPtr) {
        NSMenu* menu = (NSMenu*)menuPtr;
        [NSMenu popUpContextMenu:menu withEvent:event forView:self.contentView];
        [menu.delegate release];
        [menu release];
        
    }
}

- (void)dealloc {
    [JniHelper getEnvAndPerform:^(JNIEnv* env){
        (*env)->DeleteGlobalRef(env, self->javaRep);
    }];
    [super dealloc];
}

@end
