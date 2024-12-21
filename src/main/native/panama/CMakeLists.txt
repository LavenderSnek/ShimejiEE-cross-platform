
cmake_minimum_required(VERSION 3.24)

set(CMAKE_OSX_ARCHITECTURES arm64 x86_64)
set(ONLY_ACTIVE_ARCH NO)

# mac only for now
project(ShimejiPanama)
enable_language(OBJC)

add_library(ShimejiMacPanama SHARED)

# disable ARC
target_compile_options(ShimejiMacPanama PUBLIC -fno-objc-arc)
# Cocoa
find_library(COCOA_LIB Cocoa)
target_link_libraries(ShimejiMacPanama ${COCOA_LIB})

# sources
target_include_directories(ShimejiMacPanama PUBLIC src/mac)
target_include_directories(ShimejiMacPanama PUBLIC interfaces)

target_sources(ShimejiMacPanama PRIVATE
        interfaces/NativeRenderer.h
        interfaces/NativeEnvironment.h
        src/mac/Renderer.h
        src/mac/Renderer.m
        src/mac/Image.m
        src/mac/Menu.m
        src/mac/Environment.m
)
