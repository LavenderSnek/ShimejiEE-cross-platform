#import <Foundation/Foundation.h>
#import <jni.h>

@interface JniHelper : NSObject

+ (JavaVM*) getJvm;
+ (JNIEnv*) getMainThreadEnv;

+ (void) getEnvAndPerform:(void(^)(JNIEnv*))block;

+ (void) runOnMainQueueSync:(void(^)(void))block;
+ (void) runOnMainQueueAsync:(void(^)(void))block;

+ (jclass) makeGlobalClassRefOf:(char[])clsName;
+ (jmethodID) getMethodIdFromClass:(jclass)parentCls ofMethodNamed:(char[])methodName withSignature:(char[])signature;

@end

NSString* jstringToNSString(JNIEnv *env, jstring str);
jstring NSStringToJstring(JNIEnv* env, NSString* str);
