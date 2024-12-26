
#include "NativeRenderer.h"

#include <glad/glad.h>
#include <GLFW/glfw3.h>

#include <thread>
#include <variant>
#include <queue>
#include <unordered_map>

struct WinUpdateCmd {
    int id;
    Image image;
    bool visible;
    int x;
    int y;
};

struct WinCreateCmd {
    int id;
};

struct WinDisposeCmd {
    int id;
};

struct TexLoadCmd {
    int id;
    std::string path;
    ImageLoadingOptions options;
};

struct TextDisposeCmd {
    int id;
};

typedef std::variant<
    WinCreateCmd,
    WinDisposeCmd,
    WinUpdateCmd,
    TexLoadCmd,
    TextDisposeCmd
> RenderCmd;

struct Tex {
    GLuint tex;
    int w;
    int h;
};

struct Win {
    GLFWwindow* win;
};

std::queue<RenderCmd> cmd_queue;

std::unordered_map<int, Tex> texture_map;
std::unordered_map<int, Win> window_map;

void render_loop() {
    GLFWwindow* window;

    /* Initialize the library */
    if (!glfwInit()) {
        return;
    }
    /* Create a windowed mode window and its OpenGL context */
    window = glfwCreateWindow(640, 480, "Hello World", NULL, NULL);
    if (!window)
    {
        glfwTerminate();
        return;
    }

    /* Make the window's context current */
    glfwMakeContextCurrent(window);

    /* Loop until the user closes the window */
    while (!glfwWindowShouldClose(window))
    {
        /* Render here */
        glClearColor(0.7, 0.7, 0.3, 1);
        glClear(GL_COLOR_BUFFER_BIT);

        /* Swap front and back buffers */
        glfwSwapBuffers(window);

        /* Poll for and process events */
        glfwPollEvents();
    }

    glfwTerminate();
}

void renderer_init_event_loop() {
    auto nt = new std::thread(render_loop);
}

struct Image image_load(char *path, struct ImageLoadingOptions options) {
    Image r{nullptr};

    return r;
}

void image_dispose(struct Image image) {

}

struct Renderer renderer_create(struct RendererCallbacks callbacks) {
    Renderer renderer{nullptr}; // not threadsafe but this is for fun

    return renderer;
}

void renderer_dispose(struct Renderer renderer) {

}

void renderer_update(bool visible, struct Renderer renderer, struct Image image, int x, int y) {

}
