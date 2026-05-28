#pragma once

#include <string>
#include "simulation/World.h"

bool saveWorld(const World& world, const std::string& path);
bool loadWorld(World& world, const std::string& path);
