#include "simulation/World.h"
#include <algorithm>
#include <array>
#include <cmath>
#include <stdexcept>

World::World(int width_, int height_) : width(width_), height(height_), cells(static_cast<std::size_t>(width_ * height_)) {
    std::random_device rd;
    rng.seed(rd());
}

void World::resize(int newWidth, int newHeight) {
    width = newWidth;
    height = newHeight;
    cells.assign(static_cast<std::size_t>(width * height), Cell{});
}

void World::update() { step(); }

void World::step() {
    resetUpdatedFlags();
    Vec2i g = down();
    if (g.y > 0) {
        for (int y = height - 1; y >= 0; --y) {
            bool ltr = randomChance(50);
            if (ltr) for (int x = 0; x < width; ++x) updateCell(x, y);
            else for (int x = width - 1; x >= 0; --x) updateCell(x, y);
        }
    } else if (g.y < 0) {
        for (int y = 0; y < height; ++y) {
            bool ltr = randomChance(50);
            if (ltr) for (int x = 0; x < width; ++x) updateCell(x, y);
            else for (int x = width - 1; x >= 0; --x) updateCell(x, y);
        }
    } else if (g.x > 0) {
        for (int x = width - 1; x >= 0; --x) for (int y = 0; y < height; ++y) updateCell(x, y);
    } else {
        for (int x = 0; x < width; ++x) for (int y = 0; y < height; ++y) updateCell(x, y);
    }
    ++generation;
}

void World::clear() {
    cells.assign(static_cast<std::size_t>(width * height), Cell{});
}

void World::setCell(int x, int y, PixelMaterial material) {
    if (!inBounds(x, y)) return;
    Cell& c = getCell(x, y);
    c.material = material;
    c.updated = true;
    c.variant = static_cast<unsigned char>(randomInt(0, 30));
    c.temperature = 20.0f;
    switch (material) {
        case PixelMaterial::Fire: c.lifetime = randomInt(12, 45); c.temperature = 650.0f; break;
        case PixelMaterial::Smoke: c.lifetime = randomInt(80, 170); break;
        case PixelMaterial::Steam: c.lifetime = randomInt(50, 140); c.temperature = 120.0f; break;
        case PixelMaterial::Lava: c.lifetime = randomInt(500, 1200); c.temperature = 900.0f; break;
        case PixelMaterial::Plant: c.lifetime = randomInt(200, 900); break;
        case PixelMaterial::Mud: c.lifetime = randomInt(80, 220); break;
        case PixelMaterial::Ice: c.temperature = -8.0f; c.lifetime = randomInt(250, 900); break;
        case PixelMaterial::Electricity: c.lifetime = randomInt(2, 6); c.temperature = 1200.0f; break;
        default: c.lifetime = 0; break;
    }
}

void World::setCellRaw(int x, int y, const Cell& cell) {
    if (!inBounds(x, y)) return;
    getCell(x, y) = cell;
}

void World::paintCircle(int cx, int cy, int radius, PixelMaterial material) {
    int r2 = radius * radius;
    const PixelMaterialInfo& info = materialInfo(material);
    const int fillChance = (!info.liquid && !info.gas) ? 100 : 88;
    for (int y = cy - radius; y <= cy + radius; ++y) {
        for (int x = cx - radius; x <= cx + radius; ++x) {
            int dx = x - cx;
            int dy = y - cy;
            if (dx * dx + dy * dy <= r2 && randomChance(fillChance)) setCell(x, y, material);
        }
    }
}

void World::eraseCircle(int cx, int cy, int radius) {
    int r2 = radius * radius;
    for (int y = cy - radius; y <= cy + radius; ++y) {
        for (int x = cx - radius; x <= cx + radius; ++x) {
            int dx = x - cx;
            int dy = y - cy;
            if (dx * dx + dy * dy <= r2 && inBounds(x, y)) getCell(x, y) = Cell{};
        }
    }
}

const Cell& World::getCell(int x, int y) const { return cells[static_cast<std::size_t>(index(x, y))]; }
Cell& World::getCell(int x, int y) { return cells[static_cast<std::size_t>(index(x, y))]; }

bool World::inBounds(int x, int y) const { return x >= 0 && x < width && y >= 0 && y < height; }
int World::index(int x, int y) const { return y * width + x; }

void World::cycleGravity() {
    gravity = static_cast<GravityMode>((static_cast<int>(gravity) + 1) % 4);
}

const char* World::gravityName() const {
    switch (gravity) {
        case GravityMode::Down: return "Down";
        case GravityMode::Up: return "Up";
        case GravityMode::Left: return "Left";
        case GravityMode::Right: return "Right";
    }
    return "Down";
}

int World::countMaterial(PixelMaterial material) const {
    return static_cast<int>(std::count_if(cells.begin(), cells.end(), [material](const Cell& c) { return c.material == material; }));
}

void World::resetUpdatedFlags() {
    for (auto& c : cells) c.updated = false;
}

void World::updateCell(int x, int y) {
    if (!inBounds(x, y)) return;
    Cell& c = getCell(x, y);
    if (c.updated || c.material == PixelMaterial::Empty) return;

    switch (c.material) {
        case PixelMaterial::Sand: if (nearMaterial(x, y, PixelMaterial::Water) && randomChance(2)) { setCell(x, y, PixelMaterial::Mud); break; } updatePowder(x, y); break;
        case PixelMaterial::Mud: updateMud(x, y); break;
        case PixelMaterial::Water: updateLiquid(x, y, 5, 1); break;
        case PixelMaterial::Acid: dissolveAround(x, y, 18); updateLiquid(x, y, 4, 1); break;
        case PixelMaterial::Oil: if (nearMaterial(x, y, PixelMaterial::Fire) || nearMaterial(x, y, PixelMaterial::Lava)) ignite(x, y); else updateLiquid(x, y, 6, 2); break;
        case PixelMaterial::Lava: heatReactions(x, y); updateLiquid(x, y, 2, 4); break;
        case PixelMaterial::Smoke: updateGas(x, y, 75); break;
        case PixelMaterial::Steam: updateGas(x, y, 90); break;
        case PixelMaterial::Fire: updateFire(x, y); break;
        case PixelMaterial::Plant: updatePlant(x, y); break;
        case PixelMaterial::Wood:
        case PixelMaterial::Stone:
        case PixelMaterial::Metal:
        case PixelMaterial::Glass: updateSolid(x, y); break;
        case PixelMaterial::Ice: updateIce(x, y); break;
        case PixelMaterial::Electricity: updateElectricity(x, y); break;
        default: c.updated = true; break;
    }
}

void World::updatePowder(int x, int y) {
    Vec2i d = down();
    Vec2i l = left();
    Vec2i r = right();
    bool firstLeft = randomChance(50);
    if (tryMove(x, y, x + d.x, y + d.y)) return;
    Vec2i a = firstLeft ? l : r;
    Vec2i b = firstLeft ? r : l;
    if (tryMove(x, y, x + d.x + a.x, y + d.y + a.y)) return;
    if (tryMove(x, y, x + d.x + b.x, y + d.y + b.y)) return;
    getCell(x, y).updated = true;
}

void World::updateLiquid(int x, int y, int spread, int viscosity) {
    if (viscosity > 1 && !randomChance(100 / viscosity)) { getCell(x, y).updated = true; return; }
    Vec2i d = down();
    Vec2i l = left();
    Vec2i r = right();
    bool firstLeft = randomChance(50);
    if (tryMove(x, y, x + d.x, y + d.y)) return;
    Vec2i a = firstLeft ? l : r;
    Vec2i b = firstLeft ? r : l;
    if (tryMove(x, y, x + d.x + a.x, y + d.y + a.y)) return;
    if (tryMove(x, y, x + d.x + b.x, y + d.y + b.y)) return;
    for (int i = 1; i <= spread; ++i) {
        if (tryMove(x, y, x + a.x * i, y + a.y * i, false)) return;
        if (tryMove(x, y, x + b.x * i, y + b.y * i, false)) return;
    }
    getCell(x, y).updated = true;
}

void World::updateGas(int x, int y, int lateralChance) {
    Cell& c = getCell(x, y);
    c.lifetime--;
    if (c.lifetime <= 0) { c = Cell{}; return; }
    Vec2i u = up();
    Vec2i l = left();
    Vec2i r = right();
    bool firstLeft = randomChance(50);
    if (tryMove(x, y, x + u.x, y + u.y, false)) return;
    Vec2i a = firstLeft ? l : r;
    Vec2i b = firstLeft ? r : l;
    if (randomChance(lateralChance) && tryMove(x, y, x + u.x + a.x, y + u.y + a.y, false)) return;
    if (randomChance(lateralChance) && tryMove(x, y, x + u.x + b.x, y + u.y + b.y, false)) return;
    if (randomChance(35) && tryMove(x, y, x + a.x, y + a.y, false)) return;
    c.updated = true;
}

void World::updateFire(int x, int y) {
    Cell& c = getCell(x, y);
    if (nearMaterial(x, y, PixelMaterial::Water) || nearMaterial(x, y, PixelMaterial::Acid)) {
        setCell(x, y, PixelMaterial::Smoke);
        return;
    }
    for (auto n : neighbors4(x, y)) {
        if (!inBounds(n.x, n.y)) continue;
        PixelMaterial m = getCell(n.x, n.y).material;
        if (materialInfo(m).flammable && randomChance(m == PixelMaterial::Oil ? 75 : 20)) ignite(n.x, n.y);
    }
    c.lifetime--;
    if (c.lifetime <= 0) {
        setCell(x, y, randomChance(70) ? PixelMaterial::Smoke : PixelMaterial::Empty);
        return;
    }
    updateGas(x, y, 30);
}

void World::updateSolid(int x, int y) {
    if (nearMaterial(x, y, PixelMaterial::Lava)) {
        Cell& c = getCell(x, y);
        c.temperature += 5.0f;
        if (c.material == PixelMaterial::Wood && randomChance(18)) ignite(x, y);
        if (c.material == PixelMaterial::Glass && c.temperature > 800.0f && randomChance(3)) setCell(x, y, PixelMaterial::Lava);
    }
    getCell(x, y).updated = true;
}

void World::updateMud(int x, int y) {
    Cell& c = getCell(x, y);
    if (nearMaterial(x, y, PixelMaterial::Fire) || nearMaterial(x, y, PixelMaterial::Lava) || nearMaterial(x, y, PixelMaterial::Electricity)) {
        setCell(x, y, PixelMaterial::Sand);
        return;
    }
    if (!nearMaterial(x, y, PixelMaterial::Water)) {
        c.lifetime--;
        if (c.lifetime <= 0 && randomChance(6)) {
            setCell(x, y, PixelMaterial::Sand);
            return;
        }
    } else if (randomChance(8)) {
        c.lifetime = std::min(c.lifetime + 8, 320);
    }
    if (randomChance(42)) { c.updated = true; return; }
    updatePowder(x, y);
}

void World::updateIce(int x, int y) {
    Cell& c = getCell(x, y);
    if (nearMaterial(x, y, PixelMaterial::Fire) || nearMaterial(x, y, PixelMaterial::Lava) || nearMaterial(x, y, PixelMaterial::Electricity)) {
        setCell(x, y, PixelMaterial::Water);
        return;
    }
    if (nearMaterial(x, y, PixelMaterial::Steam) && randomChance(8)) {
        setCell(x, y, PixelMaterial::Water);
        return;
    }
    if (nearMaterial(x, y, PixelMaterial::Water) && randomChance(2)) {
        for (auto n : neighbors4(x, y)) {
            if (inBounds(n.x, n.y) && getCell(n.x, n.y).material == PixelMaterial::Water) {
                setCell(n.x, n.y, PixelMaterial::Ice);
                break;
            }
        }
    }
    c.updated = true;
}

void World::updateElectricity(int x, int y) {
    Cell& c = getCell(x, y);
    for (auto n : neighbors4(x, y)) {
        if (!inBounds(n.x, n.y)) continue;
        PixelMaterial m = getCell(n.x, n.y).material;
        if (m == PixelMaterial::Metal && randomChance(65)) setCell(n.x, n.y, PixelMaterial::Electricity);
        else if (m == PixelMaterial::Water && randomChance(25)) setCell(n.x, n.y, PixelMaterial::Electricity);
        else if ((m == PixelMaterial::Wood || m == PixelMaterial::Plant || m == PixelMaterial::Oil) && randomChance(30)) ignite(n.x, n.y);
        else if (m == PixelMaterial::Ice && randomChance(45)) setCell(n.x, n.y, PixelMaterial::Water);
        else if (m == PixelMaterial::Mud && randomChance(20)) setCell(n.x, n.y, PixelMaterial::Sand);
    }
    c.lifetime--;
    if (c.lifetime <= 0) {
        setCell(x, y, randomChance(55) ? PixelMaterial::Smoke : PixelMaterial::Empty);
        return;
    }
    updateGas(x, y, 85);
}

void World::updatePlant(int x, int y) {
    if (nearMaterial(x, y, PixelMaterial::Fire) || nearMaterial(x, y, PixelMaterial::Lava)) { ignite(x, y); return; }
    if (nearMaterial(x, y, PixelMaterial::Water) && randomChance(3)) {
        auto ns = neighbors4(x, y);
        std::shuffle(ns.begin(), ns.end(), rng);
        for (auto n : ns) if (inBounds(n.x, n.y) && getCell(n.x, n.y).material == PixelMaterial::Empty) { setCell(n.x, n.y, PixelMaterial::Plant); break; }
    }
    getCell(x, y).updated = true;
}

bool World::tryMove(int x, int y, int nx, int ny, bool swapWithLowerDensity) {
    if (!inBounds(nx, ny) || !inBounds(x, y)) return false;
    Cell& src = getCell(x, y);
    Cell& dst = getCell(nx, ny);
    if (dst.updated) return false;
    if (dst.material == PixelMaterial::Empty || (swapWithLowerDensity && canDisplace(src.material, dst.material))) {
        std::swap(src, dst);
        dst.updated = true;
        return true;
    }
    return false;
}

bool World::canDisplace(PixelMaterial moving, PixelMaterial target) const {
    if (target == PixelMaterial::Empty) return true;
    const auto& a = materialInfo(moving);
    const auto& b = materialInfo(target);
    if (b.solid) return false;
    return a.density > b.density;
}

bool World::isEmpty(int x, int y) const { return inBounds(x, y) && getCell(x, y).material == PixelMaterial::Empty; }

bool World::randomChance(int percent) {
    if (percent <= 0) return false;
    if (percent >= 100) return true;
    return randomInt(1, 100) <= percent;
}

int World::randomInt(int minInclusive, int maxInclusive) {
    std::uniform_int_distribution<int> dist(minInclusive, maxInclusive);
    return dist(rng);
}

Vec2i World::down() const {
    switch (gravity) {
        case GravityMode::Down: return {0, 1};
        case GravityMode::Up: return {0, -1};
        case GravityMode::Left: return {-1, 0};
        case GravityMode::Right: return {1, 0};
    }
    return {0, 1};
}

Vec2i World::up() const { Vec2i d = down(); return {-d.x, -d.y}; }
Vec2i World::left() const { Vec2i d = down(); return {d.y, -d.x}; }
Vec2i World::right() const { Vec2i d = down(); return {-d.y, d.x}; }

std::array<Vec2i, 4> World::neighbors4(int x, int y) const { return {{{x+1,y},{x-1,y},{x,y+1},{x,y-1}}}; }

bool World::nearMaterial(int x, int y, PixelMaterial material) const {
    for (auto n : neighbors4(x, y)) if (inBounds(n.x, n.y) && getCell(n.x, n.y).material == material) return true;
    return false;
}

void World::ignite(int x, int y) { setCell(x, y, PixelMaterial::Fire); }

void World::dissolveAround(int x, int y, int chancePercent) {
    for (auto n : neighbors4(x, y)) {
        if (!inBounds(n.x, n.y)) continue;
        PixelMaterial m = getCell(n.x, n.y).material;
        if (materialInfo(m).dissolvable && randomChance(chancePercent)) {
            getCell(n.x, n.y) = Cell{};
            if (randomChance(20)) { getCell(x, y) = Cell{}; return; }
        }
    }
}

void World::heatReactions(int x, int y) {
    for (auto n : neighbors4(x, y)) {
        if (!inBounds(n.x, n.y)) continue;
        PixelMaterial m = getCell(n.x, n.y).material;
        if (m == PixelMaterial::Water && randomChance(70)) {
            setCell(n.x, n.y, PixelMaterial::Steam);
            if (randomChance(25)) setCell(x, y, PixelMaterial::Stone);
        } else if (m == PixelMaterial::Wood || m == PixelMaterial::Plant || m == PixelMaterial::Oil) {
            if (randomChance(35)) ignite(n.x, n.y);
        } else if (m == PixelMaterial::Sand && randomChance(2)) {
            setCell(n.x, n.y, PixelMaterial::Glass);
        } else if (m == PixelMaterial::Mud && randomChance(10)) {
            setCell(n.x, n.y, PixelMaterial::Sand);
        } else if (m == PixelMaterial::Ice && randomChance(45)) {
            setCell(n.x, n.y, PixelMaterial::Water);
        } else if (m == PixelMaterial::Metal && randomChance(4)) {
            setCell(n.x, n.y, PixelMaterial::Electricity);
        }
    }
    Cell& c = getCell(x, y);
    c.lifetime--;
    if (c.lifetime <= 0 && randomChance(3)) setCell(x, y, PixelMaterial::Stone);
}
