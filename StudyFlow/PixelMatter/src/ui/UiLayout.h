#pragma once

#include <cstddef>
#include "raylib.h"

namespace UiLayout {
    inline constexpr int kWindowMargin = 16;
    inline constexpr int kPanelX = 16;
    inline constexpr int kPanelY = 16;
    inline constexpr int kPanelInnerPadding = 10;
    inline constexpr int kMaterialStartY = 80;
    inline constexpr int kMaterialRowHeight = 28;
    inline constexpr int kMaterialRowGap = 34;
    inline constexpr int kMaterialColumns = 2;
    inline constexpr int kMaterialGapX = 8;

    inline Rectangle panelRect(int worldOffsetX) {
        return Rectangle{
            static_cast<float>(kPanelX),
            static_cast<float>(kPanelY),
            static_cast<float>(worldOffsetX - 32),
            static_cast<float>(GetScreenHeight() - 32)
        };
    }

    inline Rectangle materialCellRect(std::size_t index, int worldOffsetX) {
        const int panelW = worldOffsetX - 32;
        const int innerX = kPanelX + kPanelInnerPadding;
        const int cellW = (panelW - 20 - kMaterialGapX * (kMaterialColumns - 1)) / kMaterialColumns;
        const int col = static_cast<int>(index) % kMaterialColumns;
        const int row = static_cast<int>(index) / kMaterialColumns;
        const int x = innerX + col * (cellW + kMaterialGapX);
        const int y = kMaterialStartY + row * kMaterialRowGap;
        return Rectangle{
            static_cast<float>(x),
            static_cast<float>(y),
            static_cast<float>(cellW),
            static_cast<float>(kMaterialRowHeight)
        };
    }
}
