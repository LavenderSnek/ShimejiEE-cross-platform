#pragma once
#include "NativeRenderer.h"
#include <AppKit/AppKit.h>

// can move this to a file later
@interface Util : NSObject {}
    + (void) runOnMainSync:(void(^)(void))block;
    + (void) runOnMainAsync:(void(^)(void))block;
@end
