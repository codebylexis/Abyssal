# Abyssal

A 2D game engine and level editor built from scratch in Java. The engine handles
sprite batching, a JBox2D physics simulation, OGG audio, and a full Dear ImGui
editor with GPU-based object picking. Levels are saved and loaded as JSON.

## Features

- **Batch renderer** - sprites are grouped by z-index and texture set into
  single draw calls, with per-vertex entity IDs written to a separate FBO for
  mouse picking
- **Component system** - every game object is a bag of components; the base
  class reflects over fields to generate an inspector panel automatically
- **Animation state machine** - trigger-based FSM drives sprite frame sequences;
  the player uses idle, run, and jump states
- **JBox2D physics** - box, circle, and capsule (pillbox) colliders; contact
  callbacks are routed back to component hooks so any component can respond to
  collisions
- **Level editor** - pan/zoom camera, translate and scale gizmos, tile palette
  with snap-to-grid placement, rubber-band box selection, and Ctrl+D / Delete
  shortcuts

## How it works

**Rendering** - The scene is drawn twice per frame. The first pass renders
each sprite's entity ID as a color into a dedicated FBO (PickingTexture). The
second pass renders normally into a game FBO that is displayed as an ImGui
image. RenderBatch packs position, color, UV, texture slot, and entity ID into
a 10-float interleaved vertex layout and issues one glDrawElements call per
batch.

**Physics** - Physics2D wraps a single JBox2D World stepped at a fixed 60 Hz.
Bodies and fixtures are created from Rigidbody2D, Box2DCollider, CircleCollider,
and PillboxCollider components on scene start. AbyssContactListener bridges
Box2D callbacks to the component beginCollision/endCollision hooks.

**Player movement** - Velocity is integrated manually rather than relying on
Box2D forces alone so jump feel can be tuned precisely. Two short raycasts
beneath the player's feet detect ground contact. A short debounce window keeps
the "on ground" flag true for a few frames after stepping off a ledge so jumps
still register at the edge.

**Serialization** - Scenes are saved to level.txt as a JSON array of game
objects using GSON with custom type adapters for the Component and GameObject
hierarchies.

## Requirements

- Java 17 or later (tested on Java 21)
- Maven 3.6 or later
- macOS (Apple Silicon or Intel)

All library dependencies are downloaded automatically by Maven on first build.

## Build and run

```bash
git clone https://github.com/codebylexis/Abyssal.git
cd Abyssal
mvn compile exec:exec
```

The first build downloads roughly 150 MB of dependencies (LWJGL, imgui-java,
JBox2D, JOML, GSON). Subsequent runs compile and launch in a few seconds.

## Editor controls

| Input | Action |
|---|---|
| Middle-mouse drag | Pan the camera |
| Scroll wheel | Zoom in and out |
| 0 | Reset camera to origin |
| Left-click | Select object / place held tile |
| Left-click drag | Box select multiple objects |
| E | Translate gizmo |
| R | Scale gizmo |
| Ctrl+D | Duplicate selected object |
| Delete | Remove selected object(s) |
| Escape | Drop held tile without placing |

Use File > Save (or the menu bar) to write the level to disk. Press Play in the
game viewport to enter runtime mode; press Stop to return to the editor.

## Project structure

```
src/
  core/           - engine kernel: window, game loop, scene, input, assets
  components/     - behaviors: player, sprites, animation, physics colliders
  graphics/       - rendering: batch renderer, framebuffers, debug drawing
  physics2d/      - JBox2D wrapper and collider components
  editor/         - Dear ImGui panels and gizmo tools
  scenes/         - scene initializers for editor and gameplay modes
  observers/      - lightweight event bus
  util/           - asset cache, math helpers, settings
res/
  shaders/        - GLSL vertex and fragment programs
  textures/       - sprite atlases and tilesets
  sounds/         - OGG audio files
  fonts/          - TTF fonts used by the editor
pom.xml           - Maven build; all dependencies declared here
```

## Dependencies

| Library | Version | Purpose |
|---|---|---|
| LWJGL | 3.3.4 | OpenGL, GLFW, OpenAL, STB |
| imgui-java | 1.89.0 | Dear ImGui bindings |
| JBox2D | 2.2.1.1 | 2D physics simulation |
| JOML | 1.9.25 | Vector and matrix math |
| GSON | 2.8.7 | JSON save/load |
