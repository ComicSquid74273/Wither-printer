# Build status — Minecraft 26.2 port

## Ported target

- Minecraft `26.2`
- Meteor Client `26.2-SNAPSHOT`
- Fabric Loader `0.19.3`
- Fabric Loom `1.17-SNAPSHOT` (resolved implementation `1.17.20`)
- Java `25`
- Output version `1.1.10`

## Validated changes

- The full v1.1.10 module compiles against the Minecraft 26.2 and Meteor 26.2 APIs.
- Fabric metadata expands to `minecraft: ~26.2` and `java: >=25`.
- Container operations now use `ContainerInput` and `handleContainerInput(...)`.
- Screen guards now use `mc.gui.screen()`.
- Name Tag interaction now follows 26.2's hit-result-aware entity-use API.
- Existing smart material-only restocking, 4:3 partial-load planning, UUID-based Wither detection, burst placement, Auto Walk ownership, and Elytra Fly setting restoration are retained.
- The resolved runtime graph contains the expected Minecraft 26.2, Meteor 26.2, and Fabric Loader 0.19.3 artifacts.

## Build

Run Gradle itself with Java 25:

```powershell
java -version
.\gradlew.bat build
```

Expected jar:

```text
build\libs\Bonggerz-Wither-Printer-26.2-1.1.10.jar
```

Compilation and packaging validate API linkage and artifact metadata. Placement timing, multiplayer interaction acceptance, server inventory synchronization, and Elytra flight behavior remain runtime-only checks and should be smoke-tested in a controlled world before live use.
