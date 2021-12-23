#import <jni.h>

@interface JniHelper : NSObject

+ (JavaVM*) getJvm;
+ (JNIEnv*) getMainThreadEnv;

+ (void) getEnvAndPerform:(void(^)(JNIEnv*))block;

+ (void) runOnMainQueueSync:(void(^)(void))block;
+ (void) runOnMainQueueAsync:(void(^)(void))block;

@end


