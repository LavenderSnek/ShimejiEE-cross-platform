cmake_minimum_required(VERSION 3.24)

# todo: figure out glfw install (the arm64 version doesnt work)
# if (APPLE)
#    set(CMAKE_OSX_ARCHITECTURES arm64 x86_64)
#    set(ONLY_ACTIVE_ARCH NO)
# endif()

project(ShimejiPanama)

option(SHIMEJI_GLFW_PLATFORM_BUILD "Build glfw backend" ON)
if (SHIMEJI_GLFW_PLATFORM_BUILD)
    add_subdirectory(platform/glfw)
endif ()

# MacOS only
if (APPLE)
    option(SHIMEJI_MAC_PLATFORM_BUILD "Build macOS (cocoa) backend" ON)
    if (SHIMEJI_MAC_PLATFORM_BUILD)
        add_subdirectory(platform/mac)
    endif ()
endif ()

