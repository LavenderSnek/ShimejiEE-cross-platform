#pragma once
#import <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif

//--- Image
struct Image {
    void* data;
    int w;
    int h;
};

struct ImageLoadingOptions {
    double scaling;
    bool flipped;
    bool anti_alias;
};

struct Image image_load(char* path, struct ImageLoadingOptions options);
void image_dispose(struct Image image);

//----Menu
struct Menu {
    void* data;
};

typedef void (*MenuCallback)();

struct MenuCallbacks {
    MenuCallback on_open;
    MenuCallback on_close;
};

struct Menu menu_create(char* title, struct MenuCallbacks callbacks);
struct Menu menu_create_submenu(struct Menu parent, char* title);
void menu_add_button(struct Menu menu, char* title, MenuCallback on_click);
void menu_add_disabled(struct Menu menu, char* title);
void menu_add_separator(struct Menu menu);

//---Renderer

void renderer_init_event_loop(void);

struct Renderer {
    void* data;
};

typedef void (*MouseHandler)(int rel_x, int rel_y);
typedef struct Menu (*MenuProducer)();

struct RendererCallbacks {
    MouseHandler on_left_mousedown;
    MouseHandler on_left_mouseup;
    MenuProducer create_menu;
};

struct Renderer renderer_create(struct RendererCallbacks callbacks);
void renderer_dispose(struct Renderer renderer);
void renderer_update(bool visible, struct Renderer renderer, struct Image image, int x, int y);

#ifdef __cplusplus
}
#endif