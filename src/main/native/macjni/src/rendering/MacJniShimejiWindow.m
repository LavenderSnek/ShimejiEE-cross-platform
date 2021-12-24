
#import "com_group_finity_mascotnative_macjni_MacJniShimejiWindow.h"
#import "ShimejiWindow.h"
#import "JniHelper.h"


/*
 * Class:     com_group_finity_mascotnative_macjni_MacJniShimejiWindow
 * Method:    setImageForShimejiWindow
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_com_group_1finity_mascotnative_macjni_MacJniShimejiWindow_setImageForShimejiWindow
(JNIEnv *env, jclass cls, jlong shimejiWindowPtr, jlong nsImagePtr) {
    
    [JniHelper runOnMainQueueAsync:^{
        [(ShimejiWindow*)shimejiWindowPtr setImage:(NSImage*)nsImagePtr];
    }];
}

/*
 * Class:     com_group_finity_mascotnative_macjni_MacJniShimejiWindow
 * Method:    repaintShimejiWindow
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_group_1finity_mascotnative_macjni_MacJniShimejiWindow_repaintShimejiWindow
(JNIEnv *env, jclass cls, jlong shimejiWindowPtr) {
    
    [JniHelper runOnMainQueueAsync:^{
        ShimejiWindow* sw = (ShimejiWindow*)shimejiWindowPtr;
        [sw.contentView setNeedsDisplay:YES];
        [sw displayIfNeeded];
    }];
    
}


/*
 * Class:     com_group_finity_mascotnative_macjni_MacJniShimejiWindow
 * Method:    setJavaBoundsForNSWindow
 * Signature: (JIIII)V
 */
JNIEXPORT void JNICALL Java_com_group_1finity_mascotnative_macjni_MacJniShimejiWindow_setJavaBoundsForNSWindow
(JNIEnv *env, jclass cls, jlong nsWindowPtr, jint x, jint y, jint width, jint height) {
    NSRect screenRect = [[[NSScreen screens] firstObject] frame];
    NSRect bounds = NSMakeRect(x, y, width, height);
    bounds.origin.y = screenRect.size.height - bounds.origin.y - bounds.size.height;
    
    [JniHelper runOnMainQueueAsync:^{
        [(NSWindow*)nsWindowPtr setFrame:bounds display:NO];
    }];
}

/*
 * Class:     com_group_finity_mascotnative_macjni_MacJniShimejiWindow
 * Method:    setVisibilityForNSWindow
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_com_group_1finity_mascotnative_macjni_MacJniShimejiWindow_setVisibilityForNSWindow
(JNIEnv *env, jclass cls, jlong nsWindowPtr, jboolean visible) {
    [JniHelper runOnMainQueueAsync:^{
        [(NSWindow*)nsWindowPtr setIsVisible:visible];
    }];
}

/*
 * Class:     com_group_finity_mascotnative_macjni_MacJniShimejiWindow
 * Method:    disposeShimejiWindow
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_group_1finity_mascotnative_macjni_MacJniShimejiWindow_disposeShimejiWindow
(JNIEnv *env, jclass cls, jlong shimejiWindowPtr) {
    [JniHelper runOnMainQueueAsync:^{
        [(ShimejiWindow*)shimejiWindowPtr release];
    }];
    
}

/*
 * Class:     com_group_finity_mascotnative_macjni_MacJniShimejiWindow
 * Method:    createNativeShimejiWindow
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_group_1finity_mascotnative_macjni_MacJniShimejiWindow_createNativeShimejiWindow
(JNIEnv *env, jobject macJniShimejiWindowObj) {
    
    __block ShimejiWindow* window = nil;
    
    NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
    @try {
        
        jobject javaRep = (*env)->NewGlobalRef(env, macJniShimejiWindowObj);
        
        [JniHelper runOnMainQueueSync:^{
            window = [[ShimejiWindow alloc] initWithJavaRep:javaRep];
        }];
    
    } @finally {
       [pool drain];
    }
    
    return (long)window;
}
