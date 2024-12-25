enable_language(CXX)
set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(shimejinative_glfw SHARED)
target_include_directories(shimejinative_glfw PUBLIC "${CMAKE_SOURCE_DIR}/interfaces")

target_sources(shimejinative_glfw PRIVATE
        Renderer.cpp
        Menu.cpp
        Environment.cpp
)

# glad
target_sources(shimejinative_glfw PRIVATE glad/src/glad.c)
target_include_directories(shimejinative_glfw PRIVATE glad/include)

#-----
# just keeping everything separate (i'd rather not debug cmake)

if (APPLE)
    find_package(glfw3 REQUIRED)

    find_library(COCOA_LIBRARY Cocoa)
    find_library(OPENGL_LIBRARY OpenGL)
    find_library(IOKIT_LIBRARY IOKit)

    target_link_libraries(shimejinative_glfw PRIVATE glfw ${COCOA_LIBRARY} ${OPENGL_LIBRARY} ${IOKIT_LIBRARY})
endif ()
