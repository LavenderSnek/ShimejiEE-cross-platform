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

+ (jclass) makeGlobalClassRefOf:(char[])clsName {
    __block jclass ret;
    [JniHelper getEnvAndPerform:^(JNIEnv* env){
        jclass tmpClassRef = (*env)->FindClass(env, clsName);
        ret = (*env)->NewGlobalRef(env, tmpClassRef);
        (*env)->DeleteLocalRef(env, tmpClassRef);
    }];
    return ret;
}

+ (jmethodID) getMethodIdFromClass:(jclass)parentCls ofMethodNamed:(char[])methodName withSignature:(char[])signature {
    __block jmethodID ret;
    [JniHelper getEnvAndPerform:^(JNIEnv* env){
        ret = (*env)->GetMethodID(env, parentCls, methodName, signature);
    }];
    return ret;
}

@end

NSString* jstringToNSString(JNIEnv *env, jstring str) {
    if (str == NULL) {
        return NULL;
    }
    
    const jchar* characters = (*env)->GetStringChars(env, str, NULL);
    
    NSString* ret = [NSString stringWithCharacters:(UniChar*)characters
                                            length:(*env)->GetStringLength(env, str)];
    
    (*env)->ReleaseStringChars(env, str, characters);
    
    return ret;
}

jstring NSStringToJstring(JNIEnv* env, NSString* str) {
    if (str == NULL) {
       return NULL;
    }

    jsize len = (jint)[str length];
    unichar *buffer = (unichar*)calloc(len, sizeof(unichar));
    if (buffer == NULL) {
       return NULL;
    }

    NSRange crange = NSMakeRange(0, len);
    [str getCharacters:buffer range:crange];
    jstring jStr = (*env)->NewString(env, buffer, len);
    free(buffer);

    return jStr;
}

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
