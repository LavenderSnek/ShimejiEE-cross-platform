
#import "JniMenuListener.h"
#import "JniHelper.h"

jclass JC_MacJniMenu;
jmethodID JMID_MacJniMenu_onOpen;
jmethodID JMID_MacJniMenu_onClose;

@implementation JniMenuListener {
    jobject javaRep;
}

+ (void)initialize {
    if (self == [JniMenuListener class]) {
        JC_MacJniMenu = [JniHelper makeGlobalClassRefOf:"com/group_finity/mascotnative/macjni/menu/MacJniMenu"];
        JMID_MacJniMenu_onOpen = [JniHelper getMethodIdFromClass:JC_MacJniMenu ofMethodNamed:"_onOpen" withSignature:"()V"];
        JMID_MacJniMenu_onClose = [JniHelper getMethodIdFromClass:JC_MacJniMenu ofMethodNamed:"_onClose" withSignature:"()V"];
    }
}

- (instancetype)initWithJavaRep:(jobject)jvr {

    self = [super init];

    if (self) {
        self->javaRep = jvr;
    }

    return self;
}


- (void)menuWillOpen:(NSMenu*)menu {
    [JniHelper getEnvAndPerform:^(JNIEnv* env) {
        (*env)->CallVoidMethod(env, self->javaRep, JMID_MacJniMenu_onOpen);
    }];
}

- (void)menuDidClose:(NSMenu *)menu {
    [JniHelper getEnvAndPerform:^(JNIEnv* env) {
        (*env)->CallVoidMethod(env, self->javaRep, JMID_MacJniMenu_onClose);
    }];
}

- (void)dealloc {
    
    [JniHelper getEnvAndPerform:^(JNIEnv* env) {
        (*env)->DeleteGlobalRef(env, self->javaRep);
    }];
    
    [super dealloc];
}

@end
