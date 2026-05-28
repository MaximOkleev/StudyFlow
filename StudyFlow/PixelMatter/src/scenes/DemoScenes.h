#pragma once

#include "simulation/World.h"

enum class DemoScene {
    Volcano,
    Aquarium,
    FireLab,
    AcidLab,
    Garden
};

void loadDemoScene(World& world, DemoScene scene);
const char* demoSceneName(DemoScene scene);
