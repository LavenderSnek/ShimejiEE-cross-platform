#pragma once

#import <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif

void init_native_environment();
void update_native_environment();

struct NativeRect {
    int x;
    int y;
    int w;
    int h;
};

struct NativePoint {
    int x;
    int y;
};

struct NativeRect get_active_ie_bounds(void);
void move_ie_window(int x, int y);
void restore_ie(void);

#ifdef __cplusplus
}
#endif
