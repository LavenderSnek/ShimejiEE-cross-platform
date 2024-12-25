
#include "NativeRenderer.h"

#include <iostream>

int id = 0;

// guh
void renderer_init_event_loop() {

}

struct Image image_load(char *path, struct ImageLoadingOptions options) {
    struct Image r;

    return r;
}

void image_dispose(struct Image image) {

}

struct Renderer renderer_create(struct RendererCallbacks callbacks) {
    id++;

    auto idp = new int();
    *idp = id;

    Renderer renderer{idp}; // not threadsafe but this is for fun
    std::cout << "Rc|." << std::to_string(*idp) << std::endl;

    return renderer;
}

void renderer_dispose(struct Renderer renderer) {
    std::cout << "Rx|." << std::to_string(*((int*)renderer.data)) << std::endl;
    delete (int*)renderer.data;
}

void renderer_update(bool visible, struct Renderer renderer, struct Image image, int x, int y) {
    std::cout << "Ru|." << std::to_string(*((int*)renderer.data))
    << "|i" <<  std::to_string(*((int*)image.data))
    << "|v" <<  (visible ? "1" : "0")
    << "|x" <<  std::to_string(x)
    << "|y" <<  std::to_string(y)
    << std::endl;
}
