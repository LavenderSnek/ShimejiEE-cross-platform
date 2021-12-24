
#import "com_group_finity_mascotnative_macjni_menu_MacJniMenu.h"
#import "JniMenuListener.h"
#import "JniHelper.h"

/*
 * Class:     com_group_finity_mascotnative_macjni_menu_MacJniMenu
 * Method:    createRegularNSMenuAsSubmenuOf
 * Signature: (JLjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_group_1finity_mascotnative_macjni_menu_MacJniMenu_createRegularNSMenuAsSubmenuOf
(JNIEnv* env, jclass cls, jlong parentNsMenuPtr, jstring title) {
    
    __block jlong menuPtr = 0;
    NSString* convertedTitle = jstringToNSString(env, title);
    
    @autoreleasepool {
        [JniHelper runOnMainQueueSync:^{
            NSMenuItem* item = [[[NSMenuItem alloc] initWithTitle:convertedTitle
                                                           action:NULL
                                                    keyEquivalent:@""] autorelease];
            
            NSMenu* menu = [[[NSMenu alloc] initWithTitle:convertedTitle] autorelease];
            
            [item setSubmenu:menu];
            [(NSMenu*)parentNsMenuPtr addItem:item];
            
            menuPtr = (jlong)menu;
        }];
    }
    
    // retained only as long as the parent lives
    return menuPtr;
}

/*
 * Class:     com_group_finity_mascotnative_macjni_menu_MacJniMenu
 * Method:    addDisabledItemToNSMenuWithTitle
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_group_1finity_mascotnative_macjni_menu_MacJniMenu_addDisabledItemToNSMenuWithTitle
(JNIEnv* env, jclass cls, jlong nsMenuPtr, jstring title) {
    
    NSString* convertedTitle = jstringToNSString(env, title);
    
    @autoreleasepool {
        [JniHelper runOnMainQueueSync:^{
            NSMenuItem* item = [[[NSMenuItem alloc] initWithTitle:convertedTitle
                                                           action:NULL
                                                    keyEquivalent:@""] autorelease];
            
            [item setEnabled:NO];
            [(NSMenu*)nsMenuPtr addItem:item];
        }];
    }
}

/*
 * Class:     com_group_finity_mascotnative_macjni_menu_MacJniMenu
 * Method:    addSeparatorToNSMenu
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_group_1finity_mascotnative_macjni_menu_MacJniMenu_addSeparatorToNSMenu
(JNIEnv* env, jclass cls, jlong nsMenuPtr) {
    [JniHelper runOnMainQueueSync:^{
        [(NSMenu*)nsMenuPtr addItem: NSMenuItem.separatorItem];
    }];
}

/*
 * Class:     com_group_finity_mascotnative_macjni_menu_MacJniMenu
 * Method:    createMacJniMenu
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_group_1finity_mascotnative_macjni_menu_MacJniMenu_createMacJniMenu
(JNIEnv* env, jobject macJniMenuObject) {
    
    __block jlong menuPtr = 0;
    
    jobject javaRep = (*env)->NewGlobalRef(env, macJniMenuObject);
    
    @autoreleasepool {
        [JniHelper runOnMainQueueSync:^{
            // released by ShimejiWindow
            NSMenu* menu = [[NSMenu alloc] initWithTitle:@""];
            JniMenuListener* listener = [[JniMenuListener alloc] initWithJavaRep:javaRep];
            [menu setDelegate: listener];
            menuPtr = (jlong)menu;
        }];
    }
    
    return menuPtr;
}
