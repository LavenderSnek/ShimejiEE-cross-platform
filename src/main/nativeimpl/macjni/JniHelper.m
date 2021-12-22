#import <Foundation/Foundation.h>
#import "JniHelper.h"


static const jint JNI_VERSION = JNI_VERSION_1_8;
static JavaVM* jvm = NULL;
static JNIEnv* mainThreadEnv = NULL;


@implementation JniHelper

+ (JavaVM*) getJvm {
    return jvm;
}
+ (JNIEnv*) getMainThreadEnv {
    return mainThreadEnv;
}

+ (void)getEnvAndPerform:(void (^)(JNIEnv *))block {
    if ([NSThread isMainThread]) {
        block(mainThreadEnv);
        return;
    }
    
    JNIEnv *env;
    jint s = (*jvm)->GetEnv(jvm, (void **)&env, JNI_VERSION);
    if (s == JNI_EDETACHED) {
        (*jvm)->AttachCurrentThread(jvm, (void **)env, NULL);
    }
    
    block(env);
    
    if (s == JNI_EDETACHED) {
        (*jvm)->DetachCurrentThread(jvm);
    }
}

+ (void) runOnMainQueueSync:(void(^)(void))block {
    if ([NSThread isMainThread]) {
        block();
    } else {
        dispatch_sync(dispatch_get_main_queue(), block);
    }
}
+ (void) runOnMainQueueAsync:(void(^)(void))block {
    dispatch_async(dispatch_get_main_queue(), block);
}


@end

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    
    jvm = vm;
    
    JNIEnv* env;
    if ((*vm)->GetEnv(vm, (void**)&env, JNI_VERSION) != JNI_OK) {
        return JNI_ERR;
    }
    
    if ([NSThread isMainThread]) {
        mainThreadEnv = env;
    } else {
        [JniHelper runOnMainQueueSync:^{
            JNIEnv* mainEnv;
            (*vm)->GetEnv(vm, (void**)&mainEnv, JNI_VERSION);
            mainThreadEnv = mainEnv;
        }];
    }
    
    NSLog(@"loaded");
    
    return JNI_VERSION;
}


void JNI_OnUnload(JavaVM *vm, void *reserved) {

    JNIEnv* env;
    (*vm)->GetEnv(vm, (void**)&env, JNI_VERSION);

}
