#pragma once

#include <string>
#include "input/InputHandler.h"
#include "rendering/Renderer.h"
#include "simulation/World.h"

class Application {
public:
    Application();
    void run();

private:
    static constexpr int kCellSize = 4;
    static constexpr int kWorldWidth = 260;
    static constexpr int kWorldHeight = 180;
    static constexpr int kPanelWidth = 360;

    static constexpr int kMaxCellSize = 6;

    World world;
    Renderer renderer;
    InputHandler input;
    bool paused = false;
    bool stepOnce = false;
    bool helpVisible = true;
    std::string message = "Ready";

    void handleShortcuts();
    void saveQuick();
    void loadQuick();
    void screenshot();
    void updateLayout();
};
