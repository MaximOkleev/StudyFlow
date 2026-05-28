#include "app/Application.h"
#include "raylib.h"
#include "scenes/DemoScenes.h"
#include "storage/SaveLoad.h"
#include <algorithm>
#include <filesystem>

Application::Application()
    : world(kWorldWidth, kWorldHeight), renderer(kCellSize, kPanelWidth, 18) {}

void Application::run() {
    const int screenW = kPanelWidth + kWorldWidth * kCellSize + 18;
    const int screenH = kWorldHeight * kCellSize + 36;
    SetConfigFlags(FLAG_WINDOW_RESIZABLE | FLAG_VSYNC_HINT);
    InitWindow(screenW, screenH, "PixelMatter");
    SetWindowMinSize(screenW, screenH);
    SetTargetFPS(120);
    std::filesystem::create_directories("saves");
    loadDemoScene(world, DemoScene::Volcano);

    while (!WindowShouldClose()) {
        updateLayout();
        SetMouseCursor(MOUSE_CURSOR_DEFAULT);
        bool materialHovered = input.handleMaterialPanel(renderer.offsetX());
        bool uiHovered = materialHovered || CheckCollisionPointRec(GetMousePosition(), renderer.panelRect());
        input.handleMaterialHotkeys();
        handleShortcuts();
        if (!uiHovered) input.handleDrawing(world, renderer.offsetX(), renderer.offsetY(), renderer.cellSize());
        if (!paused || stepOnce) {
            world.update();
            stepOnce = false;
        }
        renderer.draw(world, input.selectedMaterial(), input.brushSize(), paused, helpVisible, message);
    }
    CloseWindow();
}

void Application::handleShortcuts() {
    if (IsKeyPressed(KEY_SPACE)) { paused = !paused; message = paused ? "Paused" : "Running"; }
    if (IsKeyPressed(KEY_H)) { helpVisible = !helpVisible; message = helpVisible ? "Help shown" : "Help hidden"; }
    if (IsKeyPressed(KEY_N)) { stepOnce = true; paused = true; message = "Single step"; }
    if (IsKeyPressed(KEY_C)) { world.clear(); message = "World cleared"; }
    if (IsKeyPressed(KEY_G)) { world.cycleGravity(); message = std::string("Gravity: ") + world.gravityName(); }
    if (IsKeyPressed(KEY_F5)) saveQuick();
    if (IsKeyPressed(KEY_F9)) loadQuick();
    if (IsKeyPressed(KEY_F11)) { ToggleFullscreen(); message = IsWindowFullscreen() ? "Fullscreen ON" : "Fullscreen OFF"; }
    if (IsKeyPressed(KEY_F12)) screenshot();
    if (IsKeyPressed(KEY_V)) { loadDemoScene(world, DemoScene::Volcano); message = "Demo: Volcano"; }
    if (IsKeyPressed(KEY_B)) { loadDemoScene(world, DemoScene::Aquarium); message = "Demo: Aquarium"; }
    if (IsKeyPressed(KEY_M)) { loadDemoScene(world, DemoScene::FireLab); message = "Demo: Fire Lab"; }
    if (IsKeyPressed(KEY_P)) { loadDemoScene(world, DemoScene::AcidLab); message = "Demo: Acid Lab"; }
    if (IsKeyPressed(KEY_O)) { loadDemoScene(world, DemoScene::Garden); message = "Demo: Garden"; }
}

void Application::saveQuick() {
    std::filesystem::create_directories("saves");
    message = saveWorld(world, "saves/quick_save.pmat") ? "Saved quick_save.pmat" : "Save failed";
}

void Application::loadQuick() {
    message = loadWorld(world, "saves/quick_save.pmat") ? "Loaded quick_save.pmat" : "Load failed";
}

void Application::screenshot() {
    std::filesystem::create_directories("saves");
    std::string path = "saves/pixelmatter_screenshot.png";
    TakeScreenshot(path.c_str());
    message = "Screenshot saved";
}

void Application::updateLayout() {
    const int availableW = std::max(1, GetScreenWidth() - kPanelWidth - 26);
    const int availableH = std::max(1, GetScreenHeight() - 36);
    int dynamicCell = std::min(availableW / kWorldWidth, availableH / kWorldHeight);
    dynamicCell = std::clamp(dynamicCell, 2, kMaxCellSize);
    const int boardH = kWorldHeight * dynamicCell;
    const int offsetY = std::max(18, (GetScreenHeight() - boardH) / 2);
    renderer.setLayout(dynamicCell, kPanelWidth, offsetY);
}
