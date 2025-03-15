
#include "NativeEnvironment.h"
#include <AppKit/AppKit.h>

void init_native_environment() {

}

void update_native_environment() {

}

// macos just uses the java display + cursor so these are placeholder

int get_display_count() {
    return 1;
}

struct NativeRect* get_display_bounds_list() {
    struct NativeRect* rects = (struct NativeRect*)malloc(sizeof(struct NativeRect));

    rects[0].x = 0;
    rects[0].y = 0;
    rects[0].w = 1440;
    rects[0].h = 900;

    return NULL;
}

struct NativePoint get_cursor_pos() {
    struct NativePoint pt;
    pt.x = 0;
    pt.y = 0;
    return pt;
}

//---

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


NSRunningApplication* currentFrontmostApp = nil;

struct NativeRect get_active_ie_bounds() {
    NSRunningApplication* fmApp = NSWorkspace.sharedWorkspace.frontmostApplication;

    struct NativeRect bounds;
    bounds.x = 0;
    bounds.y = 0;
    bounds.w = 0;
    bounds.h = 0;

    if (fmApp != nil && ![fmApp.localizedName isEqual: @"ShimejiEE"]) {
        currentFrontmostApp = fmApp;
    }

    if (currentFrontmostApp == nil || currentFrontmostApp.terminated || currentFrontmostApp.hidden) {
        return bounds;
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
            return bounds;
        }
    } else {
        return bounds;
    }

    if (CGSizeEqualToSize(winSize, CGSizeZero)) {
        return bounds;
    }

    bounds.x = (int)winPos.x;
    bounds.y = (int)winPos.y;
    bounds.w = (int)winSize.width;
    bounds.h = (int)winSize.height;

    return bounds;
}

void move_ie_window(int x, int y) {

}

void restore_ie() {

}
