#pragma once
#import <stdbool.h>

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

int get_display_count();
struct NativeRect* get_display_bounds_list();
struct NativePoint get_cursor_pos();

struct NativeRect get_active_ie_bounds();
void move_ie_window(int x, int y);
void restore_ie();

