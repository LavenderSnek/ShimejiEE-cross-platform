
#include "NativeEnvironment.h"
#include <AppKit/AppKit.h>

void init_native_environment() {

}

void update_native_environment() {

}

int get_display_count() {
    return 0;
}

struct NativeRect* get_display_bounds_list() {
    return NULL;
}
struct NativePoint get_cursor_pos() {
    struct NativePoint pt;
    pt.x = 0;
    pt.y = 0;
    return pt;
}

struct NativeRect get_active_ie_bounds() {
    struct NativeRect r;
    r.x = 0;
    r.y = 0;
    r.w = 0;
    r.h = 0;
    return r;
}
void move_ie_window(int x, int y) {

}
void restore_ie() {

}
