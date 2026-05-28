#pragma once

#include <cstdint>
#include <string>
#include <vector>

struct PixelColor {
    unsigned char r = 255;
    unsigned char g = 255;
    unsigned char b = 255;
    unsigned char a = 255;
};

enum class PixelMaterial : std::uint8_t {
    Empty = 0,
    Sand,
    Water,
    Stone,
    Wood,
    Fire,
    Smoke,
    Steam,
    Lava,
    Acid,
    Metal,
    Glass,
    Oil,
    Plant,
    Mud,
    Ice,
    Electricity,
    Count
};

struct PixelMaterialInfo {
    PixelMaterial material;
    const char* name;
    PixelColor baseColor;
    int density;
    bool solid;
    bool liquid;
    bool gas;
    bool flammable;
    bool dissolvable;
};

const PixelMaterialInfo& materialInfo(PixelMaterial material);
const std::vector<PixelMaterial>& paletteMaterials();
PixelMaterial materialFromIndex(int index);
int materialIndex(PixelMaterial material);
bool isValidMaterial(std::uint8_t value);
std::uint8_t materialToSaveId(PixelMaterial material);
bool tryMaterialFromSaveId(std::uint32_t version, std::uint8_t raw, PixelMaterial& out);
