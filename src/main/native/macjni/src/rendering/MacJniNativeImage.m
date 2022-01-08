
#import "com_group_finity_mascotnative_macjni_MacJniNativeImage.h"
#import "JniHelper.h"
#import "JdkCode.h"

/*
 * Class:     com_group_finity_mascotnative_macjni_MacJniNativeImage
 * Method:    createNSImageFromArray
 * Signature: ([III)J
 */
JNIEXPORT jlong JNICALL Java_com_group_1finity_mascotnative_macjni_MacJniNativeImage_createNSImageFromArray
(JNIEnv *env, jclass cls, jintArray pixels, jint width, jint height) {

    jlong result = 0L;

    NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
    @try {
        NSBitmapImageRep* imageRep = CImage_CreateImageRep(env, pixels, width, height);
        if (imageRep) {
            NSImage *nsImage = [[NSImage alloc] initWithSize:NSMakeSize(width, height)];
            [nsImage addRepresentation:imageRep];
            result = (jlong)nsImage;
        }
    } @finally {
       [pool drain];
    }

    return result;

}

JNIEXPORT void JNICALL Java_com_group_1finity_mascotnative_macjni_MacJniNativeImage_disposeNsImage
(JNIEnv *env, jclass cls, jlong nsImagePtr) {
    
    [JniHelper runOnMainQueueAsync:^{
        [(NSImage*)nsImagePtr release];
    }];

}
