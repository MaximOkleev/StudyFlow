#include "simulation/PixelMaterial.h"

namespace {
const std::vector<PixelMaterialInfo> kInfos = {
    {PixelMaterial::Empty, "Empty", PixelColor{0, 0, 0, 255}, 0, false, false, false, false, false},
    {PixelMaterial::Sand, "Sand", PixelColor{214, 184, 107, 255}, 60, false, false, false, false, true},
    {PixelMaterial::Water, "Water", PixelColor{55, 155, 230, 210}, 35, false, true, false, false, false},
    {PixelMaterial::Stone, "Stone", PixelColor{110, 113, 124, 255}, 100, true, false, false, false, true},
    {PixelMaterial::Wood, "Wood", PixelColor{130, 82, 42, 255}, 85, true, false, false, true, true},
    {PixelMaterial::Fire, "Fire", PixelColor{255, 96, 24, 245}, -15, false, false, true, false, false},
    {PixelMaterial::Smoke, "Smoke", PixelColor{120, 124, 132, 130}, -8, false, false, true, false, false},
    {PixelMaterial::Steam, "Steam", PixelColor{190, 220, 235, 120}, -12, false, false, true, false, false},
    {PixelMaterial::Lava, "Lava", PixelColor{255, 70, 16, 255}, 65, false, true, false, false, false},
    {PixelMaterial::Acid, "Acid", PixelColor{115, 255, 84, 220}, 34, false, true, false, false, false},
    {PixelMaterial::Metal, "Metal", PixelColor{185, 190, 198, 255}, 110, true, false, false, false, true},
    {PixelMaterial::Glass, "Glass", PixelColor{150, 215, 255, 125}, 95, true, false, false, false, true},
    {PixelMaterial::Oil, "Oil", PixelColor{48, 41, 24, 220}, 28, false, true, false, true, false},
    {PixelMaterial::Plant, "Plant", PixelColor{48, 190, 88, 255}, 45, true, false, false, true, true},
    {PixelMaterial::Mud, "Mud", PixelColor{116, 78, 45, 255}, 72, false, false, false, false, true},
    {PixelMaterial::Ice, "Ice", PixelColor{145, 218, 255, 180}, 92, true, false, false, false, true},
    {PixelMaterial::Electricity, "Electric", PixelColor{105, 210, 255, 240}, -20, false, false, true, false, false},
};

const std::vector<PixelMaterial> kPalette = {
    PixelMaterial::Sand, PixelMaterial::Water, PixelMaterial::Mud, PixelMaterial::Stone,
    PixelMaterial::Wood, PixelMaterial::Fire, PixelMaterial::Smoke, PixelMaterial::Steam,
    PixelMaterial::Lava, PixelMaterial::Acid, PixelMaterial::Metal, PixelMaterial::Glass,
    PixelMaterial::Oil, PixelMaterial::Plant, PixelMaterial::Ice, PixelMaterial::Electricity
};
}

const PixelMaterialInfo& materialInfo(PixelMaterial material) {
    for (const auto& info : kInfos) {
        if (info.material == material) return info;
    }
    return kInfos[0];
}

const std::vector<PixelMaterial>& paletteMaterials() {
    return kPalette;
}

PixelMaterial materialFromIndex(int index) {
    if (index < 0 || index >= static_cast<int>(kPalette.size())) return PixelMaterial::Sand;
    return kPalette[static_cast<std::size_t>(index)];
}

int materialIndex(PixelMaterial material) {
    for (std::size_t i = 0; i < kPalette.size(); ++i) {
        if (kPalette[i] == material) return static_cast<int>(i);
    }
    return 0;
}

bool isValidMaterial(std::uint8_t value) {
    return value < static_cast<std::uint8_t>(PixelMaterial::Count);
}

std::uint8_t materialToSaveId(PixelMaterial material) {
    switch (material) {
        case PixelMaterial::Empty: return 0;
        case PixelMaterial::Sand: return 1;
        case PixelMaterial::Water: return 2;
        case PixelMaterial::Stone: return 3;
        case PixelMaterial::Wood: return 4;
        case PixelMaterial::Fire: return 5;
        case PixelMaterial::Smoke: return 6;
        case PixelMaterial::Steam: return 7;
        case PixelMaterial::Lava: return 8;
        case PixelMaterial::Acid: return 9;
        case PixelMaterial::Metal: return 10;
        case PixelMaterial::Glass: return 11;
        case PixelMaterial::Oil: return 12;
        case PixelMaterial::Plant: return 13;
        case PixelMaterial::Mud: return 14;
        case PixelMaterial::Ice: return 15;
        case PixelMaterial::Electricity: return 16;
        case PixelMaterial::Count: return 0;
    }
    return 0;
}

bool tryMaterialFromSaveId(std::uint32_t version, std::uint8_t raw, PixelMaterial& out) {
    if (version == 0 || version > 2) return false;
    switch (raw) {
        case 0: out = PixelMaterial::Empty; return true;
        case 1: out = PixelMaterial::Sand; return true;
        case 2: out = PixelMaterial::Water; return true;
        case 3: out = PixelMaterial::Stone; return true;
        case 4: out = PixelMaterial::Wood; return true;
        case 5: out = PixelMaterial::Fire; return true;
        case 6: out = PixelMaterial::Smoke; return true;
        case 7: out = PixelMaterial::Steam; return true;
        case 8: out = PixelMaterial::Lava; return true;
        case 9: out = PixelMaterial::Acid; return true;
        case 10: out = PixelMaterial::Metal; return true;
        case 11: out = PixelMaterial::Glass; return true;
        case 12: out = PixelMaterial::Oil; return true;
        case 13: out = PixelMaterial::Plant; return true;
        case 14: out = PixelMaterial::Mud; return true;
        case 15: out = PixelMaterial::Ice; return true;
        case 16:
            if (version < 2) return false;
            out = PixelMaterial::Electricity;
            return true;
        default: return false;
    }
}
