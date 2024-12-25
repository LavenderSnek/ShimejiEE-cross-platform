
#include "NativeEnvironment.h"

void init_native_environment() {}

void update_native_environment() {}

int get_display_count() {
    return 0;
}

struct NativeRect* get_display_bounds_list() {
    return nullptr;
}

struct NativePoint get_cursor_pos() {
    NativePoint pt{0,0};
    return pt;
}

struct NativeRect get_active_ie_bounds() {
    NativeRect r {0,0,0,0};
    return r;
}

void move_ie_window(int x, int y) {}

void restore_ie() {}
