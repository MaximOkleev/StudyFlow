#include "input/InputHandler.h"
#include "raylib.h"
#include "ui/UiLayout.h"
#include <algorithm>
#include <cmath>

void InputHandler::handleMaterialHotkeys() {
    const int keys[] = {KEY_ONE, KEY_TWO, KEY_THREE, KEY_FOUR, KEY_FIVE, KEY_SIX, KEY_SEVEN, KEY_EIGHT, KEY_NINE, KEY_ZERO, KEY_Q, KEY_W, KEY_E, KEY_R, KEY_T, KEY_Y};
    for (int i = 0; i < static_cast<int>(sizeof(keys) / sizeof(keys[0])); ++i) {
        if (IsKeyPressed(keys[i])) selected = materialFromIndex(i);
    }

    float wheel = GetMouseWheelMove();
    if (wheel > 0) brush = std::min(42, brush + 1);
    if (wheel < 0) brush = std::max(1, brush - 1);
    if (IsKeyPressed(KEY_LEFT_BRACKET)) brush = std::max(1, brush - 1);
    if (IsKeyPressed(KEY_RIGHT_BRACKET)) brush = std::min(42, brush + 1);
}

bool InputHandler::handleMaterialPanel(int worldOffsetX) {
    Vector2 mouse = GetMousePosition();
    const auto& materials = paletteMaterials();
    bool insideAny = false;

    for (std::size_t i = 0; i < materials.size(); ++i) {
        Rectangle row = UiLayout::materialCellRect(i, worldOffsetX);
        if (CheckCollisionPointRec(mouse, row)) {
            SetMouseCursor(MOUSE_CURSOR_POINTING_HAND);
            insideAny = true;
            if (IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) selected = materials[i];
        }
    }

    return insideAny;
}

void InputHandler::handleDrawing(World& world, int offsetX, int offsetY, int cellSize) {
    Vector2 mouse = GetMousePosition();
    int x = static_cast<int>((mouse.x - offsetX) / cellSize);
    int y = static_cast<int>((mouse.y - offsetY) / cellSize);

    if (!world.inBounds(x, y)) {
        lastX = lastY = lastEraseX = lastEraseY = -1;
        return;
    }

    if (IsMouseButtonDown(MOUSE_BUTTON_LEFT)) {
        if (lastX >= 0 && lastY >= 0) paintLine(world, lastX, lastY, x, y, selected);
        else world.paintCircle(x, y, brush, selected);
        lastX = x;
        lastY = y;
    } else {
        lastX = lastY = -1;
    }

    if (IsMouseButtonDown(MOUSE_BUTTON_RIGHT)) {
        if (lastEraseX >= 0 && lastEraseY >= 0) eraseLine(world, lastEraseX, lastEraseY, x, y);
        else world.eraseCircle(x, y, brush);
        lastEraseX = x;
        lastEraseY = y;
    } else {
        lastEraseX = lastEraseY = -1;
    }
}

void InputHandler::paintLine(World& world, int x0, int y0, int x1, int y1, PixelMaterial material) {
    int dx = x1 - x0;
    int dy = y1 - y0;
    int steps = std::max(std::abs(dx), std::abs(dy));
    if (steps <= 0) {
        world.paintCircle(x1, y1, brush, material);
        return;
    }
    for (int i = 0; i <= steps; ++i) {
        int x = x0 + dx * i / steps;
        int y = y0 + dy * i / steps;
        world.paintCircle(x, y, brush, material);
    }
}

void InputHandler::eraseLine(World& world, int x0, int y0, int x1, int y1) {
    int dx = x1 - x0;
    int dy = y1 - y0;
    int steps = std::max(std::abs(dx), std::abs(dy));
    if (steps <= 0) {
        world.eraseCircle(x1, y1, brush);
        return;
    }
    for (int i = 0; i <= steps; ++i) {
        int x = x0 + dx * i / steps;
        int y = y0 + dy * i / steps;
        world.eraseCircle(x, y, brush);
    }
}
