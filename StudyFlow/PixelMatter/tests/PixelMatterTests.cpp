#include "simulation/World.h"
#include "storage/SaveLoad.h"
#include <cassert>
#include <filesystem>

static void sand_falls_down_after_step() {
    World world(8, 8);
    world.setCell(4, 1, PixelMaterial::Sand);
    world.step();
    assert(world.getCell(4, 2).material == PixelMaterial::Sand);
}

static void stable_save_ids_cover_new_materials() {
    assert(materialToSaveId(PixelMaterial::Mud) == 14);
    assert(materialToSaveId(PixelMaterial::Ice) == 15);
    assert(materialToSaveId(PixelMaterial::Electricity) == 16);
    PixelMaterial out = PixelMaterial::Empty;
    assert(tryMaterialFromSaveId(2, 16, out));
    assert(out == PixelMaterial::Electricity);
}

static void save_load_roundtrip_preserves_world() {
    World world(10, 10);
    world.setCell(1, 1, PixelMaterial::Mud);
    world.setCell(2, 1, PixelMaterial::Ice);
    world.setCell(3, 1, PixelMaterial::Electricity);

    const auto path = std::filesystem::temp_directory_path() / "pixelmatter_test_roundtrip.pmat";
    assert(saveWorld(world, path.string()));

    World loaded(1, 1);
    assert(loadWorld(loaded, path.string()));
    assert(loaded.getWidth() == 10);
    assert(loaded.getHeight() == 10);
    assert(loaded.getCell(1, 1).material == PixelMaterial::Mud);
    assert(loaded.getCell(2, 1).material == PixelMaterial::Ice);
    assert(loaded.getCell(3, 1).material == PixelMaterial::Electricity);
    std::filesystem::remove(path);
}

int main() {
    sand_falls_down_after_step();
    stable_save_ids_cover_new_materials();
    save_load_roundtrip_preserves_world();
    return 0;
}
