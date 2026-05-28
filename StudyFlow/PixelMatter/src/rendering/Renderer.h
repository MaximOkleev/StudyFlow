#pragma once

#include <string>
#include "raylib.h"
#include "simulation/World.h"

class Renderer {
public:
    Renderer(int cellSize, int offsetX, int offsetY);

    void draw(const World& world, PixelMaterial selected, int brush, bool paused, bool helpVisible, const std::string& message);
    int cellSize() const { return cell; }
    int offsetX() const { return x0; }
    int offsetY() const { return y0; }
    void setLayout(int cellSize, int offsetX, int offsetY);
    Rectangle panelRect() const;

private:
    int cell;
    int x0;
    int y0;

    Color colorFor(const Cell& cell) const;
    void drawWorld(const World& world) const;
    void drawPanel(const World& world, PixelMaterial selected, int brush, bool paused, bool helpVisible, const std::string& message) const;
    void drawHelp(int x, int y, int width) const;
};
