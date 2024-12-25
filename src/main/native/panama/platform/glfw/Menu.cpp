#include "NativeRenderer.h"

struct Menu menu_create(char *title, struct MenuCallbacks callbacks) {
    struct Menu menu{};

    return menu;
}

struct Menu menu_create_submenu(struct Menu parent, char *title) {
    struct Menu menu{};

    return menu;
}

void menu_add_button(struct Menu menu, char *title, MenuCallback on_click) {

}

void menu_add_disabled(struct Menu menu, char *title) {

}

void menu_add_separator(struct Menu menu) {

}
