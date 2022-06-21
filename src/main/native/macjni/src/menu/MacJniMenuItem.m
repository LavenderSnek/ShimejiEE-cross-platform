
#import "com_group_finity_mascotnative_macjni_menu_MacJniMenuItem.h"
#import "MacJniMenuItem.h"
#import "JniHelper.h"


jclass JC_MacJniMenuItem;
jmethodID JMID_MacJniMenuItem_onClick;

@implementation JniMenuItem {
    jobject javaRep;
}

+ (void)initialize {

    if (self == [JniMenuItem class]) {
        JC_MacJniMenuItem = [JniHelper makeGlobalClassRefOf:"com/group_finity/mascotnative/macjni/menu/MacJniMenuItem"];
        JMID_MacJniMenuItem_onClick = [JniHelper getMethodIdFromClass:JC_MacJniMenuItem ofMethodNamed:"_onClick" withSignature:"()V"];
    }

}

- (instancetype)initWithTitle:(NSString*)title javaRep:(jobject)javaRep {
    
    self = [super initWithTitle:title action:@selector(onClickAction) keyEquivalent:@""];
    
    if (self) {
        self->javaRep = javaRep;
        [self setTarget:self];
    }
    
    return self;
}

- (void)onClickAction {
    [JniHelper getEnvAndPerform:^(JNIEnv* env){
        (*env)->CallVoidMethod(env, self->javaRep, JMID_MacJniMenuItem_onClick);
    }];
}

- (void)dealloc {
    
    [JniHelper getEnvAndPerform:^(JNIEnv* env){
        (*env)->DeleteGlobalRef(env, self->javaRep);
    }];
    
    [super dealloc];
}

@end

/*
 * Class:     com_group_finity_mascotnative_macjni_menu_MacJniMenuItem
 * Method:    createNativeMacJniMenuItemWithParent
 * Signature: (Ljava/lang/String;J)J
 */
JNIEXPORT void JNICALL Java_com_group_1finity_mascotnative_macjni_menu_MacJniMenuItem_createNativeMacJniMenuItemWithParent
(JNIEnv* env, jobject macJniMenuItemObj, jstring title, jlong parentNsMenuPtr) {

    NSMenu* parentMenu = (NSMenu*)parentNsMenuPtr;
    NSString* nssTitle = jstringToNSString(env, title);
    jobject javaRep = (*env)->NewGlobalRef(env, macJniMenuItemObj);
    
    @autoreleasepool {
        [JniHelper runOnMainQueueSync:^{
            // item deallocated after parent
            JniMenuItem* item = [[[JniMenuItem alloc] initWithTitle:nssTitle
                                                            javaRep:javaRep ]autorelease];
            [parentMenu addItem:item];
        }];
    }
}
