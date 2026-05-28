cmake_minimum_required(VERSION 3.20)
project(PixelMatter VERSION 1.1.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

include(FetchContent)
FetchContent_Declare(
    raylib
    GIT_REPOSITORY https://github.com/raysan5/raylib.git
    GIT_TAG 5.5
)
set(BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(raylib)

set(PIXELMATTER_CORE_SOURCES
    src/simulation/PixelMaterial.cpp
    src/simulation/World.cpp
    src/storage/SaveLoad.cpp
)

set(PIXELMATTER_APP_SOURCES
    src/main.cpp
    src/app/Application.cpp
    src/input/InputHandler.cpp
    src/rendering/Renderer.cpp
    src/scenes/DemoScenes.cpp
)

add_library(pixelmatter_core STATIC ${PIXELMATTER_CORE_SOURCES})
target_include_directories(pixelmatter_core PUBLIC src)

add_executable(PixelMatter ${PIXELMATTER_APP_SOURCES})
target_include_directories(PixelMatter PRIVATE src)
target_link_libraries(PixelMatter PRIVATE pixelmatter_core raylib)

foreach(target pixelmatter_core PixelMatter)
    if (MSVC)
        target_compile_options(${target} PRIVATE /W4 /permissive-)
    else()
        target_compile_options(${target} PRIVATE -Wall -Wextra -Wpedantic)
    endif()
endforeach()

add_custom_command(TARGET PixelMatter POST_BUILD
    COMMAND ${CMAKE_COMMAND} -E make_directory $<TARGET_FILE_DIR:PixelMatter>/saves
)

include(CTest)
if(BUILD_TESTING)
    enable_testing()
    add_executable(pixelmatter_tests tests/PixelMatterTests.cpp)
    target_link_libraries(pixelmatter_tests PRIVATE pixelmatter_core)
    add_test(NAME pixelmatter_tests COMMAND pixelmatter_tests)
endif()
