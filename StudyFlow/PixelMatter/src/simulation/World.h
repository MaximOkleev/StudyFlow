#pragma once

#include <array>
#include <random>
#include <string>
#include <vector>
#include "simulation/Cell.h"

struct Vec2i {
    int x = 0;
    int y = 0;
};

enum class GravityMode {
    Down,
    Up,
    Left,
    Right
};

class World {
public:
    World(int width, int height);

    void update();
    void step();
    void clear();
    void resize(int newWidth, int newHeight);

    void setCell(int x, int y, PixelMaterial material);
    void setCellRaw(int x, int y, const Cell& cell);
    void paintCircle(int cx, int cy, int radius, PixelMaterial material);
    void eraseCircle(int cx, int cy, int radius);

    const Cell& getCell(int x, int y) const;
    Cell& getCell(int x, int y);
    const std::vector<Cell>& data() const { return cells; }
    std::vector<Cell>& data() { return cells; }

    bool inBounds(int x, int y) const;
    int getWidth() const { return width; }
    int getHeight() const { return height; }
    int getGeneration() const { return generation; }
    GravityMode getGravity() const { return gravity; }
    void setGravity(GravityMode mode) { gravity = mode; }
    void cycleGravity();
    const char* gravityName() const;

    int countMaterial(PixelMaterial material) const;

private:
    int width;
    int height;
    std::vector<Cell> cells;
    std::mt19937 rng;
    int generation = 0;
    GravityMode gravity = GravityMode::Down;

    int index(int x, int y) const;
    void resetUpdatedFlags();
    void updateCell(int x, int y);

    void updatePowder(int x, int y);
    void updateLiquid(int x, int y, int spread, int viscosity);
    void updateGas(int x, int y, int lateralChance);
    void updateFire(int x, int y);
    void updateSolid(int x, int y);
    void updatePlant(int x, int y);
    void updateMud(int x, int y);
    void updateIce(int x, int y);
    void updateElectricity(int x, int y);

    bool tryMove(int x, int y, int nx, int ny, bool swapWithLowerDensity = true);
    bool canDisplace(PixelMaterial moving, PixelMaterial target) const;
    bool isEmpty(int x, int y) const;
    bool randomChance(int percent);
    int randomInt(int minInclusive, int maxInclusive);

    Vec2i down() const;
    Vec2i up() const;
    Vec2i left() const;
    Vec2i right() const;
    std::array<Vec2i, 4> neighbors4(int x, int y) const;
    bool nearMaterial(int x, int y, PixelMaterial material) const;
    void ignite(int x, int y);
    void dissolveAround(int x, int y, int chancePercent);
    void heatReactions(int x, int y);
};
