# PixelMatter

PixelMatter is a desktop falling-sand sandbox written in C++20 with raylib. It simulates powders, liquids, gases, heat, fire, acids, electricity and save/loadable worlds.

## Current features

- Real-time cellular simulation with gravity modes.
- Materials: sand, water, mud, stone, wood, fire, smoke, steam, lava, acid, metal, glass, oil, plant, ice and electricity.
- Stable 2-column material palette: drawing and hit-testing use the same shared UI geometry.
- Continuous brush strokes without gaps when drawing quickly.
- Material-aware brush fill: solids paint cleanly, liquids/gases remain looser.
- Improved Mud/Ice behavior: drying, freezing, melting and heat interactions.
- Electricity: short-lived conductive material that spreads through metal/water and ignites flammables.
- Resizable window and `F11` fullscreen toggle.
- Demo scenes: volcano, aquarium, fire lab, acid lab, garden.
- Quick save/load in `saves/quick_save.pmat`.
- Save format with stable material IDs and validation.
- CTest-based smoke/unit tests for core world and save/load logic.

## Controls

| Action | Control |
|---|---|
| Draw | Left mouse button |
| Erase | Right mouse button |
| Change brush size | Mouse wheel, `[` and `]` |
| Select material | Click material in panel, or `1-9`, `0`, `Q`, `W`, `E`, `R`, `T`, `Y` |
| Pause / resume | `Space` |
| Single step | `N` |
| Clear world | `C` |
| Change gravity | `G` |
| Quick save / load | `F5` / `F9` |
| Fullscreen | `F11` |
| Screenshot | `F12` |
| Toggle help | `H` |
| Demo scenes | `V`, `B`, `M`, `P`, `O` |

## Build, run and test

Requirements:

- Git
- CMake 3.20+
- C++20 compiler
- On Windows: Visual Studio Build Tools / Visual Studio with **Desktop development with C++**

```bash
cmake -S . -B build -DBUILD_TESTING=ON
cmake --build build --config Release
ctest --test-dir build --output-on-failure
```

With CLion, open the folder that contains `CMakeLists.txt`, select the Visual Studio toolchain or MinGW toolchain, reload CMake, then run the `PixelMatter` CMake target.

## Project structure

```text
src/app          Application loop, layout updates and shortcuts
src/input        Mouse, keyboard and brush handling
src/rendering    raylib rendering and UI panel
src/scenes       Demo scene generation
src/simulation   Materials, cells and world physics
src/storage      Binary save/load format
tests            CTest smoke/unit tests
```

## Release hygiene

Do not commit or ship generated files:

```text
.idea/
.vs/
build/
cmake-build-*/
out/
_deps/
CMakeFiles/
*.obj
*.lib
*.pdb
*.ilk
saves/*.pmat
saves/*.png
```

The repository is meant to contain source code only. Build from a clean copy before making release archives.
