
#import "JniMenuListener.h"
#import "JniHelper.h"

jclass JC_MacJniMneu;
jmethodID JMID_MacJniMneu_onOpen;
jmethodID JMID_MacJniMneu_onClose;

@implementation JniMenuListener {
    jobject javaRep;
}

+ (void)initialize {
    if (self == [JniMenuListener class]) {
        [JniHelper getEnvAndPerform:^(JNIEnv* env){
            jclass localClsRef = (*env)->FindClass(env, "com/group_finity/mascotnative/macjni/menu/MacJniMenu");
            JC_MacJniMneu = (*env)->NewGlobalRef(env, localClsRef);
            (*env)->DeleteLocalRef(env, localClsRef);

            JMID_MacJniMneu_onOpen =
            (*env)->GetMethodID(env, JC_MacJniMneu, "_onOpen", "()V");

            JMID_MacJniMneu_onClose =
            (*env)->GetMethodID(env, JC_MacJniMneu, "_onClose", "()V");
        }];
    }
}

- (instancetype)initWithJavaRep:(jobject)javaRep {

    self = [super init];

    if (self) {
        self->javaRep = javaRep;
    }

    return self;
}


- (void)menuWillOpen:(NSMenu*)menu {
    [JniHelper getEnvAndPerform:^(JNIEnv* env) {
        (*env)->CallVoidMethod(env, self->javaRep, JMID_MacJniMneu_onOpen);
    }];
}

- (void)menuDidClose:(NSMenu *)menu {
    [JniHelper getEnvAndPerform:^(JNIEnv* env) {
        (*env)->CallVoidMethod(env, self->javaRep, JMID_MacJniMneu_onClose);
    }];
}

- (void)dealloc {
    
    [JniHelper getEnvAndPerform:^(JNIEnv* env) {
        (*env)->DeleteGlobalRef(env, self->javaRep);
    }];
    
    [super dealloc];
}

@end
