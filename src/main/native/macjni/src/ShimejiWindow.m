
#import "ShimejiWindow.h"
#import "JniHelper.h"

@interface ShimejiImageView : NSView

-(void)setImage:(NSImage *)image;

@end

@implementation ShimejiImageView {
    NSImage* image;
}

- (void)setImage:(NSImage *)image {
    self->image = image;
    // doesn't set needsDisplay since the update gets done later.
}

- (void)drawRect:(NSRect)dirtyRect {
    if (image) {
        [[NSGraphicsContext currentContext] setImageInterpolation:NSImageInterpolationNone];
        [image drawInRect:dirtyRect];
    }
}

@end

#pragma mark -Peer Window

// todo : release this global ref at the end
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
        [JniHelper getEnvAndPerform:^(JNIEnv* env){
            jclass tmpClassRef;
            
            tmpClassRef = (*env)->FindClass(env, "com/group_finity/mascotnative/macjni/MacJniShimejiWindow");
            JC_MacJniShimejiWindow = (*env)->NewGlobalRef(env, tmpClassRef);
            (*env)->DeleteLocalRef(env, tmpClassRef);

            JMID_MacJniShimejiWindow_onLeftMouseDown =
            (*env)->GetMethodID(env, JC_MacJniShimejiWindow, "_onLeftMouseDown", "()V");

            JMID_MacJniShimejiWindow_onLeftMouseUp =
            (*env)->GetMethodID(env, JC_MacJniShimejiWindow, "_onLeftMouseUp", "()V");

            JMID_MacJniShimejiWindow_getNSMenuPtrForPopup =
            (*env)->GetMethodID(env, JC_MacJniShimejiWindow, "_getNSMenuPtrForPopup", "()J");
        }];
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
        self->javaRep = javaRep;
    }
    return self;
}

-(void)setImage:(NSImage *)image {
    [(ShimejiImageView *)self.contentView setImage:image];
}

- (void)mouseDown:(NSEvent *)event {
    [JniHelper getEnvAndPerform:^(JNIEnv* env){
        (*env)->CallVoidMethod(env, self->javaRep, JMID_MacJniShimejiWindow_onLeftMouseDown);
    }];
}

- (void)mouseUp:(NSEvent *)event {
    [JniHelper getEnvAndPerform:^(JNIEnv* env){
        (*env)->CallVoidMethod(env, self->javaRep, JMID_MacJniShimejiWindow_onLeftMouseUp);
    }];
}

- (void)rightMouseUp:(NSEvent *)event {
    
    // todo: autorealease pool for the menu

    __block jlong menuPtr = 0L;

    [JniHelper getEnvAndPerform:^(JNIEnv* env){
        menuPtr =
        (*env)->CallLongMethod(env, self->javaRep, JMID_MacJniShimejiWindow_getNSMenuPtrForPopup);
    }];

    if (menuPtr) {
        //show menu
    }
}

- (void)dealloc {
    [JniHelper getEnvAndPerform:^(JNIEnv* env){
        (*env)->DeleteGlobalRef(env, self->javaRep);
    }];
    [super dealloc];
}

@end
