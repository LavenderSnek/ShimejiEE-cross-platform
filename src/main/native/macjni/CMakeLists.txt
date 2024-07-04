
cmake_minimum_required(VERSION 3.24)

set(CMAKE_OSX_ARCHITECTURES arm64 x86_64)
set(ONLY_ACTIVE_ARCH NO)

enable_language(OBJC)

project(ShimejiMacJni)

add_library(ShimejiMacJni SHARED)

# disable ARC
target_compile_options(ShimejiMacJni PUBLIC -fno-objc-arc)

# JNI
find_package(Java 17 REQUIRED COMPONENTS Development)
find_package(JNI REQUIRED)
target_include_directories(ShimejiMacJni PUBLIC ${JNI_INCLUDE_DIRS})

# Cocoa
find_library(COCOA_LIB Cocoa)
target_link_libraries(ShimejiMacJni ${COCOA_LIB})

# sources
target_include_directories(ShimejiMacJni PUBLIC src)
target_include_directories(ShimejiMacJni PUBLIC ../headers)

target_sources(ShimejiMacJni PRIVATE
        src/JdkCode.m
        src/JdkCode.h
        src/JniHelper.m
        src/JniHelper.h

        src/rendering/MacJniNativeImage.m
        src/rendering/MacJniShimejiWindow.m
        src/rendering/ShimejiWindow.m
        src/rendering/ShimejiWindow.h

        src/environment/MacJniEnvironment.m
        src/environment/ShimejiEnvironment.m
        src/environment/ShimejiEnvironment.h

        src/menu/MacJniMenu.m
        src/menu/JniMenuListener.m
        src/menu/JniMenuListener.h
        src/menu/MacJniMenuItem.m
        src/menu/MacJniMenuItem.h
)

