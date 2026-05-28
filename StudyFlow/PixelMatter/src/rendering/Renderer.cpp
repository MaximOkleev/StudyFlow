#include "rendering/Renderer.h"
#include "raylib.h"
#include "ui/UiLayout.h"
#include <algorithm>
#include <cmath>

namespace {
using UiLayout::kPanelX;
using UiLayout::kMaterialStartY;
using UiLayout::kMaterialColumns;
constexpr int kPanelBottomPadding = 18;

unsigned char clampByte(int v) {
    return static_cast<unsigned char>(std::clamp(v, 0, 255));
}

Color toRaylib(PixelColor c) {
    return Color{c.r, c.g, c.b, c.a};
}

Color tint(Color c, int delta) {
    return Color{clampByte(c.r + delta), clampByte(c.g + delta), clampByte(c.b + delta), c.a};
}

Color withAlpha(Color c, unsigned char alpha) {
    c.a = alpha;
    return c;
}

bool hovered(Rectangle rect) {
    return CheckCollisionPointRec(GetMousePosition(), rect);
}

const char* materialHotkey(std::size_t index) {
    static const char* keys[] = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "Q", "W", "E", "R", "T", "Y"};
    return index < sizeof(keys) / sizeof(keys[0]) ? keys[index] : "";
}

void drawTinyText(const char* text, int x, int y, Color color) {
    DrawText(text, x, y, 10, color);
}
}

Renderer::Renderer(int cellSize, int offsetX, int offsetY) : cell(cellSize), x0(offsetX), y0(offsetY) {}

void Renderer::setLayout(int cellSize, int offsetX, int offsetY) {
    cell = cellSize;
    x0 = offsetX;
    y0 = offsetY;
}

Rectangle Renderer::panelRect() const {
    return UiLayout::panelRect(x0);
}

void Renderer::draw(const World& world, PixelMaterial selected, int brush, bool paused, bool helpVisible, const std::string& message) {
    BeginDrawing();
    ClearBackground(Color{6, 9, 16, 255});
    drawWorld(world);
    drawPanel(world, selected, brush, paused, helpVisible, message);
    EndDrawing();
}

Color Renderer::colorFor(const Cell& c) const {
    Color base = toRaylib(materialInfo(c.material).baseColor);
    int delta = static_cast<int>(c.variant) - 15;

    switch (c.material) {
        case PixelMaterial::Fire:
            return Color{clampByte(248 + delta), clampByte(86 + delta * 2), clampByte(12), base.a};
        case PixelMaterial::Lava:
            return Color{255, clampByte(70 + delta), 12, base.a};
        case PixelMaterial::Water:
            return Color{clampByte(base.r + delta / 2), clampByte(base.g + delta), clampByte(base.b + delta * 2), base.a};
        case PixelMaterial::Acid:
            return Color{clampByte(base.r + delta), clampByte(base.g + delta * 2), clampByte(base.b + delta), base.a};
        case PixelMaterial::Mud:
            return Color{clampByte(base.r + delta / 2), clampByte(base.g + delta / 3), clampByte(base.b + delta / 4), base.a};
        case PixelMaterial::Ice:
            return Color{clampByte(base.r + delta), clampByte(base.g + delta), clampByte(base.b + delta * 2), base.a};
        case PixelMaterial::Electricity:
            return Color{clampByte(80 + delta * 2), clampByte(205 + delta), 255, base.a};
        case PixelMaterial::Smoke:
        case PixelMaterial::Steam:
            return Color{clampByte(base.r + delta), clampByte(base.g + delta), clampByte(base.b + delta), base.a};
        default:
            return Color{clampByte(base.r + delta), clampByte(base.g + delta), clampByte(base.b + delta), base.a};
    }
}

void Renderer::drawWorld(const World& world) const {
    const int boardW = world.getWidth() * cell;
    const int boardH = world.getHeight() * cell;

    DrawRectangleRounded(Rectangle{static_cast<float>(x0 - 10), static_cast<float>(y0 - 10), static_cast<float>(boardW + 20), static_cast<float>(boardH + 20)}, 0.025f, 14, Color{2, 6, 14, 255});
    DrawRectangleRounded(Rectangle{static_cast<float>(x0 - 5), static_cast<float>(y0 - 5), static_cast<float>(boardW + 10), static_cast<float>(boardH + 10)}, 0.018f, 14, Color{34, 42, 62, 255});
    DrawRectangleGradientV(x0, y0, boardW, boardH, Color{8, 13, 24, 255}, Color{2, 4, 9, 255});

    for (int y = 0; y < world.getHeight(); ++y) {
        for (int x = 0; x < world.getWidth(); ++x) {
            const Cell& c = world.getCell(x, y);
            if (c.material == PixelMaterial::Empty) continue;

            int px = x0 + x * cell;
            int py = y0 + y * cell;
            Color primary = colorFor(c);

            switch (c.material) {
                case PixelMaterial::Sand:
                    DrawRectangle(px, py, cell, cell, primary);
                    if (cell >= 4) DrawRectangle(px, py + cell - 1, cell, 1, tint(primary, -35));
                    break;
                case PixelMaterial::Mud:
                    DrawRectangle(px, py, cell, cell, primary);
                    if (cell >= 4) {
                        DrawRectangle(px, py + cell - 1, cell, 1, tint(primary, -30));
                        if (c.variant % 4 == 0) DrawRectangle(px + 1, py + 1, 1, 1, tint(primary, 25));
                    }
                    break;
                case PixelMaterial::Water:
                    DrawRectangle(px, py, cell, cell, withAlpha(primary, 205));
                    if (cell >= 4) DrawRectangle(px, py, cell, 1, Color{132, 205, 255, 130});
                    break;
                case PixelMaterial::Acid:
                    DrawRectangle(px, py, cell, cell, withAlpha(primary, 215));
                    if (cell >= 4) DrawRectangle(px, py, cell, 1, Color{188, 255, 121, 135});
                    break;
                case PixelMaterial::Lava:
                    DrawRectangleGradientV(px, py, cell, cell, Color{255, 176, 40, 255}, primary);
                    if (cell >= 4) DrawRectangle(px, py, 1, cell, Color{255, 236, 93, 115});
                    break;
                case PixelMaterial::Fire:
                    DrawRectangleGradientV(px, py, cell, cell, Color{255, 230, 63, 235}, primary);
                    if (cell >= 4) DrawRectangle(px, py - 1, cell, 1, Color{255, 190, 45, 90});
                    break;
                case PixelMaterial::Smoke:
                case PixelMaterial::Steam:
                    DrawRectangle(px + 1, py + 1, std::max(1, cell - 1), std::max(1, cell - 1), primary);
                    break;
                case PixelMaterial::Glass:
                    DrawRectangle(px, py, cell, cell, withAlpha(primary, 125));
                    if (cell >= 4) DrawRectangle(px, py, cell, 1, Color{225, 247, 255, 125});
                    break;
                case PixelMaterial::Ice:
                    DrawRectangle(px, py, cell, cell, withAlpha(primary, 170));
                    if (cell >= 4) {
                        DrawRectangle(px, py, cell, 1, Color{235, 250, 255, 160});
                        DrawRectangle(px, py, 1, cell, Color{235, 250, 255, 80});
                    }
                    break;
                case PixelMaterial::Electricity:
                    DrawRectangleGradientV(px, py, cell, cell, Color{220, 250, 255, 255}, primary);
                    if (cell >= 4) {
                        DrawRectangle(px, py, cell, 1, Color{255, 255, 255, 180});
                        DrawRectangle(px + cell / 2, py, 1, cell, Color{255, 255, 255, 120});
                    }
                    break;
                case PixelMaterial::Metal:
                    DrawRectangleGradientV(px, py, cell, cell, tint(primary, 35), tint(primary, -38));
                    break;
                case PixelMaterial::Wood:
                    DrawRectangle(px, py, cell, cell, primary);
                    if (cell >= 4 && (c.variant % 3 == 0)) DrawRectangle(px, py + cell / 2, cell, 1, tint(primary, -28));
                    break;
                case PixelMaterial::Plant:
                    DrawRectangle(px, py, cell, cell, primary);
                    if (cell >= 4) DrawRectangle(px + cell - 1, py, 1, cell, tint(primary, 34));
                    break;
                default:
                    DrawRectangle(px, py, cell, cell, primary);
                    break;
            }
        }
    }

    DrawRectangleLinesEx(Rectangle{static_cast<float>(x0), static_cast<float>(y0), static_cast<float>(boardW), static_cast<float>(boardH)}, 1.0f, Color{80, 93, 123, 120});
}

void Renderer::drawPanel(const World& world, PixelMaterial selected, int brush, bool paused, bool helpVisible, const std::string& message) const {
    const int panelX = kPanelX;
    const Rectangle panel = panelRect();
    const int panelW = static_cast<int>(panel.width);

    DrawRectangleRounded(Rectangle{panel.x + 4, panel.y + 5, panel.width, panel.height}, 0.055f, 14, Color{0, 0, 0, 85});
    DrawRectangleRounded(panel, 0.055f, 14, Color{12, 20, 34, 246});
    DrawRectangleRoundedLines(panel, 0.055f, 14, Color{58, 72, 101, 170});

    DrawText("PixelMatter", panelX + 18, 28, 24, Color{238, 242, 255, 255});
    DrawText("falling-sand sandbox", panelX + 18, 55, 13, Color{139, 155, 180, 255});

    const auto& materials = paletteMaterials();

    for (std::size_t i = 0; i < materials.size(); ++i) {
        PixelMaterial m = materials[i];
        const PixelMaterialInfo& info = materialInfo(m);
        bool active = m == selected;
        Rectangle row = UiLayout::materialCellRect(i, x0);
        const int x = static_cast<int>(row.x);
        const int y = static_cast<int>(row.y);
        const int cellW = static_cast<int>(row.width);
        bool isHover = hovered(row);

        Color rowColor = active ? Color{82, 55, 161, 220} : (isHover ? Color{32, 45, 67, 235} : Color{18, 29, 48, 185});
        DrawRectangleRounded(row, 0.22f, 10, rowColor);
        if (active || isHover) DrawRectangleRoundedLines(row, 0.22f, 10, active ? Color{154, 120, 255, 210} : Color{79, 96, 130, 190});

        Rectangle chip{static_cast<float>(x + 9), static_cast<float>(y + 7), 14.0f, 14.0f};
        DrawRectangleRounded(chip, 0.25f, 8, toRaylib(info.baseColor));
        DrawRectangleRoundedLines(chip, 0.25f, 8, Color{255, 255, 255, 70});

        DrawText(info.name, x + 30, y + 7, 12, active ? Color{255, 255, 255, 255} : Color{214, 223, 238, 255});
        DrawText(materialHotkey(i), x + cellW - 18, y + 7, 11, active ? Color{225, 214, 255, 255} : Color{115, 131, 156, 255});
    }

    int y = kMaterialStartY + ((static_cast<int>(materials.size()) + 1) / kMaterialColumns) * UiLayout::kMaterialRowGap + 8;
    DrawRectangleRounded(Rectangle{static_cast<float>(panelX + 10), static_cast<float>(y), static_cast<float>(panelW - 20), 84.0f}, 0.10f, 10, Color{9, 15, 28, 230});
    DrawText(TextFormat("Brush %d", brush), panelX + 22, y + 10, 14, Color{228, 235, 249, 255});
    DrawRectangleRounded(Rectangle{static_cast<float>(panelX + 98), static_cast<float>(y + 15), static_cast<float>(panelW - 132), 7.0f}, 0.45f, 8, Color{31, 42, 62, 255});
    DrawRectangleRounded(Rectangle{static_cast<float>(panelX + 98), static_cast<float>(y + 15), static_cast<float>(std::clamp(brush * 4, 8, panelW - 132)), 7.0f}, 0.45f, 8, Color{124, 92, 255, 255});

    DrawText(TextFormat("Gravity: %s", world.gravityName()), panelX + 22, y + 32, 13, Color{204, 216, 234, 255});
    DrawText(TextFormat("FPS: %d", GetFPS()), panelX + 22, y + 52, 13, Color{204, 216, 234, 255});
    DrawText(paused ? "PAUSED" : "RUNNING", panelX + panelW - 92, y + 52, 13, paused ? Color{255, 203, 85, 255} : Color{67, 220, 145, 255});

    y += 96;
    if (!message.empty()) {
        DrawRectangleRounded(Rectangle{static_cast<float>(panelX + 10), static_cast<float>(y), static_cast<float>(panelW - 20), 28.0f}, 0.14f, 10, Color{14, 36, 58, 215});
        DrawText(message.c_str(), panelX + 22, y + 8, 12, Color{147, 197, 253, 255});
        y += 36;
    }

    DrawText(helpVisible ? "H hide help" : "H show help", panelX + 18, std::min(y, GetScreenHeight() - 32), 12, Color{139, 155, 180, 255});
    if (helpVisible && y + 132 < GetScreenHeight() - kPanelBottomPadding) {
        drawHelp(panelX + 18, y + 22, panelW - 36);
    } else if (helpVisible) {
        drawTinyText("LMB draw | RMB erase | Wheel brush | Space pause", panelX + 18, GetScreenHeight() - 50, Color{111, 129, 157, 255});
        drawTinyText("C clear | G gravity | F5/F9 save/load | V/B/M/P/O demos", panelX + 18, GetScreenHeight() - 34, Color{111, 129, 157, 255});
    }
}

void Renderer::drawHelp(int x, int y, int width) const {
    (void)width;
    DrawText("Controls", x, y, 13, Color{183, 196, 218, 255});
    y += 20;

    const char* lines[] = {
        "Click material = select",
        "LMB draw, RMB erase",
        "Wheel / [ ] brush size",
        "Space pause, N step",
        "C clear, G gravity",
        "F5 save, F9 load",
        "F12 screenshot",
        "V/B/M/P/O demo scenes"
    };

    for (const char* line : lines) {
        DrawText(line, x, y, 11, Color{111, 129, 157, 255});
        y += 14;
    }
}
