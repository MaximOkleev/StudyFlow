#pragma once

#include "simulation/PixelMaterial.h"

struct Cell {
    PixelMaterial material = PixelMaterial::Empty;
    int lifetime = 0;
    float temperature = 20.0f;
    unsigned char variant = 0;
    bool updated = false;
};
