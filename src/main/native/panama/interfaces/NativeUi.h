#pragma once
#import <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif

// blocking function, starts app loop and needs to be called on main thread
void start_app_loop();

void request_image_set_selection(void);
void show_error(const char* message);

typedef void (*Action)();
typedef void (*MascotAction)(int id);

struct ControllerCallbacks {
};

#ifdef __cplusplus
}
#endif