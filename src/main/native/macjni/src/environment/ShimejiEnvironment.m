
#import <AppKit/AppKit.h>
#import "ShimejiEnvironment.h"
#import "JniHelper.h"


static AXUIElementRef copyAXFocusedWindowOf(AXUIElementRef target) {
    AXUIElementRef ret;
    AXError result =  AXUIElementCopyAttributeValue(target, kAXFocusedWindowAttribute, (CFTypeRef*)&ret);
    return ret;
}

static CFArrayRef copyAXChildrenOf(AXUIElementRef target) {
    CFArrayRef children;
    AXError result =  AXUIElementCopyAttributeValue(target, kAXChildrenAttribute, (CFTypeRef*)&children);
    return children;
}


static CGSize getAXSizeOf(AXUIElementRef target) {
    AXValueRef retRef;
    AXError result = AXUIElementCopyAttributeValue(target, kAXSizeAttribute, (CFTypeRef*)&retRef);
    
    CGSize ret = CGSizeZero;
    if (result == kAXErrorSuccess) {
        AXValueGetValue(retRef, kAXValueCGSizeType, &ret);
        CFRelease(retRef);
    }
    
    return ret;
}

static CGPoint getAXPositionOf(AXUIElementRef target) {
    AXValueRef retRef;
    AXError result = AXUIElementCopyAttributeValue(target, kAXPositionAttribute, (CFTypeRef*)&retRef);
    
    CGPoint ret = CGPointZero;
    if (result == kAXErrorSuccess) {
        AXValueGetValue(retRef, kAXValueCGPointType, &ret);
        CFRelease(retRef);
    }
    
    return ret;
}

static void setAXPositionOf(AXUIElementRef target, CGPoint val) {
    AXValueRef value =  AXValueCreate(kAXValueCGPointType, &val);
    AXUIElementSetAttributeValue(target, kAXPositionAttribute, value);
    CFRelease(value);
}


jclass JC_Rectangle;
jmethodID JMIDConstructor_Rectangle;

jclass JC_IeRep;
jmethodID JMIDConstructor_IeRep;

jclass JC_ScreenRep;
jmethodID JMIDConstructor_ScreenRep;

@implementation ShimejiEnvironment {
    NSRunningApplication* currentFrontmostApp;
}

+ (void)initialize
{
    if (self == [ShimejiEnvironment class]) {
        JC_Rectangle = [JniHelper makeGlobalClassRefOf:"java/awt/Rectangle"];
        JMIDConstructor_Rectangle = [JniHelper getMethodIdFromClass:JC_Rectangle ofMethodNamed:"<init>" withSignature:"(IIII)V"];
        
        JC_IeRep = [JniHelper makeGlobalClassRefOf:"com/group_finity/mascotnative/macjni/MacJniEnvironment$IeRep"];
        JMIDConstructor_IeRep = [JniHelper getMethodIdFromClass:JC_IeRep ofMethodNamed:"<init>" withSignature:"(Ljava/awt/Rectangle;Ljava/lang/String;)V"];
        
        JC_ScreenRep = [JniHelper makeGlobalClassRefOf:"com/group_finity/mascotnative/macjni/MacJniEnvironment$ScreenRep"];
        JMIDConstructor_ScreenRep = [JniHelper getMethodIdFromClass:JC_ScreenRep ofMethodNamed:"<init>" withSignature:"(Ljava/awt/Rectangle;Ljava/awt/Rectangle;)V"];
    }
}

- (instancetype)init
{
    self = [super init];
    if (self) {
        //--
    }
    return self;
}

- (jobject)getUpdatedIeRep {
    
    NSRunningApplication* fmApp = NSWorkspace.sharedWorkspace.frontmostApplication;

    if (fmApp != nil && ![fmApp.localizedName isEqual: @"ShimejiEE"]) {
        currentFrontmostApp = fmApp;
    }
    
    if (currentFrontmostApp == nil || currentFrontmostApp.terminated || currentFrontmostApp.hidden) {
        return NULL;
    }
    
    AXUIElementRef appRef = AXUIElementCreateApplication(currentFrontmostApp.processIdentifier);
    
    CGSize winSize;
    CGPoint winPos;
    
    if (appRef != NULL) {
        AXUIElementRef windowRef = copyAXFocusedWindowOf(appRef);
        CFRelease(appRef);
        
        if (windowRef != NULL) {
            winSize = getAXSizeOf(windowRef);
            winPos = getAXPositionOf(windowRef);
            CFRelease(windowRef);
        } else {
            return NULL;
        }
        
    } else {
        return NULL;
    }
    
    if (CGSizeEqualToSize(winSize, CGSizeZero)) {
        return NULL;
    }
    
    __block jobject ieRep = NULL;
    
    [JniHelper getEnvAndPerform:^(JNIEnv* env) {
        jobject winRect = (*env)->NewObject(env, JC_Rectangle, JMIDConstructor_Rectangle, (int)winPos.x, (int)winPos.y, (int)winSize.width, (int)winSize.height);
        jstring appTitle = NSStringToJstring(env, currentFrontmostApp.localizedName);
        
        ieRep = (*env)->NewObject(env, JC_IeRep, JMIDConstructor_IeRep, winRect, appTitle);
    }];
    
    return ieRep;
}

- (void)setTopLeftOfCurrentIeToX:(jint)x Y:(jint)y {
    if (currentFrontmostApp == nil || currentFrontmostApp.terminated || currentFrontmostApp.hidden) {
        return;
    }
    AXUIElementRef appRef = AXUIElementCreateApplication(currentFrontmostApp.processIdentifier);
    
    if (appRef != NULL) {
        AXUIElementRef windowRef = copyAXFocusedWindowOf(appRef);
        CFRelease(appRef);
        
        if (windowRef != NULL) {
            setAXPositionOf(windowRef, CGPointMake(x, y));
            CFRelease(windowRef);
        }
    }
}

- (void)restoreMovedIes {
}

+ (jobjectArray)getCurentScreenReps {
    
    NSArray<NSScreen*>* screens = NSScreen.screens;
    CGFloat primaryHeight = screens.firstObject.frame.size.height;
    
    __block jobjectArray ret = NULL;
    
    [JniHelper getEnvAndPerform:^(JNIEnv* env) {
        ret = (*env)->NewObjectArray(env, (jsize)[screens count], JC_ScreenRep, NULL);
        
        for (int i = 0; i < [screens count]; i++) {
            NSScreen* screen = screens[i];
            
            NSRect f = screen.frame;
            jobject bounds = (*env)->NewObject(env, JC_Rectangle, JMIDConstructor_Rectangle,
                                      (int)f.origin.x,
                                      (int)primaryHeight - (int)f.origin.y - (int)f.size.height,
                                      (int)f.size.width,
                                      (int)f.size.height);
            
            NSRect vf = screen.visibleFrame;
            jobject visibleBounds = (*env)->NewObject(env, JC_Rectangle, JMIDConstructor_Rectangle,
                                      (int)vf.origin.x,
                                      (int)primaryHeight - (int)vf.origin.y - (int)vf.size.height,
                                      (int)vf.size.width,
                                      (int)vf.size.height);
            
            
            jobject screenRep = (*env)->NewObject(env, JC_ScreenRep, JMIDConstructor_ScreenRep, bounds, visibleBounds);
            (*env)->SetObjectArrayElement(env, ret, i, screenRep);
        }
    }];
    
    return ret;
}

@end

