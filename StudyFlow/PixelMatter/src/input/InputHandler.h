#pragma once

#include "simulation/World.h"

class InputHandler {
public:
    void handleDrawing(World& world, int offsetX, int offsetY, int cellSize);
    void handleMaterialHotkeys();
    bool handleMaterialPanel(int worldOffsetX);

    PixelMaterial selectedMaterial() const { return selected; }
    int brushSize() const { return brush; }

private:
    PixelMaterial selected = PixelMaterial::Sand;
    int brush = 5;
    int lastX = -1;
    int lastY = -1;
    int lastEraseX = -1;
    int lastEraseY = -1;

    void paintLine(World& world, int x0, int y0, int x1, int y1, PixelMaterial material);
    void eraseLine(World& world, int x0, int y0, int x1, int y1);
};
