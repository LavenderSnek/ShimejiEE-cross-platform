cmake_minimum_required(VERSION 3.24)

project(ShimejiPanama)

# MacOS only
if (APPLE)
    set(CMAKE_OSX_ARCHITECTURES arm64 x86_64)
    set(ONLY_ACTIVE_ARCH NO)

    option(SHIMEJI_MAC_PLATFORM_BUILD "Build macOS (cocoa) backend" ON)
    if (SHIMEJI_MAC_PLATFORM_BUILD)
        add_subdirectory(platform/mac)
    endif ()
endif ()

