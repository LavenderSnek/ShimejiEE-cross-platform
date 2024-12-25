enable_language(OBJC)

add_library(shimejinative_mac SHARED)
target_include_directories(shimejinative_mac PUBLIC "${CMAKE_SOURCE_DIR}/interfaces")

target_sources(shimejinative_mac PRIVATE
        Renderer.h
        Renderer.m
        Image.m
        Menu.m
        Environment.m
)

#-----

# disable ARC
target_compile_options(shimejinative_mac PUBLIC -fno-objc-arc)

# linking
find_library(COCOA_LIB Cocoa)
target_link_libraries(shimejinative_mac ${COCOA_LIB})
