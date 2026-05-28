#include "storage/SaveLoad.h"
#include <cstdint>
#include <fstream>

namespace {
constexpr char kMagic[4] = {'P','M','A','T'};
constexpr std::uint32_t kVersion = 2;
}

bool saveWorld(const World& world, const std::string& path) {
    std::ofstream out(path, std::ios::binary);
    if (!out) return false;
    std::uint32_t version = kVersion;
    std::uint32_t width = static_cast<std::uint32_t>(world.getWidth());
    std::uint32_t height = static_cast<std::uint32_t>(world.getHeight());
    out.write(kMagic, 4);
    out.write(reinterpret_cast<const char*>(&version), sizeof(version));
    out.write(reinterpret_cast<const char*>(&width), sizeof(width));
    out.write(reinterpret_cast<const char*>(&height), sizeof(height));
    for (const Cell& c : world.data()) {
        auto mat = materialToSaveId(c.material);
        out.write(reinterpret_cast<const char*>(&mat), sizeof(mat));
        out.write(reinterpret_cast<const char*>(&c.lifetime), sizeof(c.lifetime));
        out.write(reinterpret_cast<const char*>(&c.temperature), sizeof(c.temperature));
        out.write(reinterpret_cast<const char*>(&c.variant), sizeof(c.variant));
    }
    return true;
}

bool loadWorld(World& world, const std::string& path) {
    std::ifstream in(path, std::ios::binary);
    if (!in) return false;
    char magic[4]{};
    std::uint32_t version = 0, width = 0, height = 0;
    in.read(magic, 4);
    if (magic[0] != kMagic[0] || magic[1] != kMagic[1] || magic[2] != kMagic[2] || magic[3] != kMagic[3]) return false;
    in.read(reinterpret_cast<char*>(&version), sizeof(version));
    if (version == 0 || version > kVersion) return false;
    in.read(reinterpret_cast<char*>(&width), sizeof(width));
    in.read(reinterpret_cast<char*>(&height), sizeof(height));
    if (width == 0 || height == 0 || width > 2000 || height > 2000) return false;
    world.resize(static_cast<int>(width), static_cast<int>(height));
    for (int y = 0; y < world.getHeight(); ++y) {
        for (int x = 0; x < world.getWidth(); ++x) {
            Cell c;
            std::uint8_t mat = 0;
            in.read(reinterpret_cast<char*>(&mat), sizeof(mat));
            in.read(reinterpret_cast<char*>(&c.lifetime), sizeof(c.lifetime));
            in.read(reinterpret_cast<char*>(&c.temperature), sizeof(c.temperature));
            in.read(reinterpret_cast<char*>(&c.variant), sizeof(c.variant));
            if (!in) return false;
            if (!tryMaterialFromSaveId(version, mat, c.material)) return false;
            c.updated = false;
            world.setCellRaw(x, y, c);
        }
    }
    return true;
}
