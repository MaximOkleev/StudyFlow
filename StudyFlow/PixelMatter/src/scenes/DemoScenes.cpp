#include "scenes/DemoScenes.h"

namespace {
void rect(World& w, int x0, int y0, int x1, int y1, PixelMaterial m) {
    for (int y = y0; y <= y1; ++y) for (int x = x0; x <= x1; ++x) w.setCell(x, y, m);
}

void circle(World& w, int cx, int cy, int r, PixelMaterial m) {
    for (int y = cy-r; y <= cy+r; ++y) for (int x = cx-r; x <= cx+r; ++x) {
        int dx = x-cx, dy = y-cy;
        if (dx*dx + dy*dy <= r*r) w.setCell(x,y,m);
    }
}
}

const char* demoSceneName(DemoScene scene) {
    switch(scene) {
        case DemoScene::Volcano: return "Volcano";
        case DemoScene::Aquarium: return "Aquarium";
        case DemoScene::FireLab: return "Fire Lab";
        case DemoScene::AcidLab: return "Acid Lab";
        case DemoScene::Garden: return "Garden";
    }
    return "Demo";
}

void loadDemoScene(World& world, DemoScene scene) {
    world.clear();
    int w = world.getWidth();
    int h = world.getHeight();
    switch (scene) {
        case DemoScene::Volcano: {
            for (int y = h/2; y < h-5; ++y) {
                int half = (y - h/2) / 2 + 8;
                rect(world, w/2-half, y, w/2+half, y, PixelMaterial::Stone);
            }
            rect(world, w/2-8, h/2+10, w/2+8, h-12, PixelMaterial::Lava);
            rect(world, 10, h-18, w-10, h-8, PixelMaterial::Stone);
            rect(world, 20, 10, 80, 25, PixelMaterial::Water);
            break;
        }
        case DemoScene::Aquarium: {
            rect(world, 25, h-20, w-25, h-12, PixelMaterial::Glass);
            rect(world, 25, 45, 30, h-12, PixelMaterial::Glass);
            rect(world, w-30, 45, w-25, h-12, PixelMaterial::Glass);
            rect(world, 31, h-40, w-31, h-21, PixelMaterial::Sand);
            rect(world, 50, h-48, w-50, h-41, PixelMaterial::Mud);
            rect(world, 35, 60, w-35, h-42, PixelMaterial::Water);
            circle(world, w/2, h-55, 12, PixelMaterial::Stone);
            rect(world, 58, 52, 95, 55, PixelMaterial::Ice);
            break;
        }
        case DemoScene::FireLab: {
            rect(world, 40, h-30, w-40, h-20, PixelMaterial::Stone);
            rect(world, w/2-40, h-80, w/2+40, h-31, PixelMaterial::Wood);
            rect(world, w/2-8, h-95, w/2+8, h-81, PixelMaterial::Wood);
            rect(world, 60, h-50, 90, h-31, PixelMaterial::Fire);
            rect(world, w-90, h-70, w-50, h-31, PixelMaterial::Water);
            rect(world, w-145, h-70, w-105, h-31, PixelMaterial::Ice);
            break;
        }
        case DemoScene::AcidLab: {
            rect(world, 20, h-18, w-20, h-10, PixelMaterial::Stone);
            rect(world, 50, h-65, 100, h-19, PixelMaterial::Metal);
            rect(world, 140, h-65, 200, h-19, PixelMaterial::Wood);
            rect(world, 240, h-65, 300, h-19, PixelMaterial::Stone);
            rect(world, w-90, 20, w-30, 40, PixelMaterial::Acid);
            break;
        }
        case DemoScene::Garden: {
            rect(world, 10, h-25, w-10, h-10, PixelMaterial::Sand);
            rect(world, 30, h-40, w-30, h-26, PixelMaterial::Mud);
            rect(world, 30, h-55, w-30, h-41, PixelMaterial::Water);
            for (int x = 60; x < w-60; x += 30) rect(world, x, h-70, x+5, h-26, PixelMaterial::Plant);
            rect(world, w/2-8, h-100, w/2+8, h-80, PixelMaterial::Fire);
            break;
        }
    }
}
