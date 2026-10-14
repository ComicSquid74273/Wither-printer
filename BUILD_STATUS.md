# Build status — v1.1.10

## Changes validated

- Restored the General `direction-mode` setting.
- `Motion` remains the default; `Look` is available again.
- Fixed staging-slot architecture is unchanged.
- Smart restocking now filters shulker source slots to Soul Sand, Soul Soil, and Wither Skeleton Skulls only.
- Name Tags and all other non-material shulker contents are intentionally left untouched.
- Full-fit mode transfers every eligible soul/skull source stack.
- Partial-fit mode includes the player's existing soul/skull counts and balances around the real 4:3 Wither recipe requirement.
- The common full-64-stack partial path exhaustively enumerates Soul Sand/Soul Soil/Skull stack-count combinations; abstract exhaustive checks confirm `15+12` for 27 slots, `6+4` for 10 slots, and compensation for pre-existing material surplus.
- Existing carried soul/skull stacks are not discarded merely to enlarge a partial content transfer; Firework Rockets may still be discarded when that strictly improves the smart refill.
- Project metadata updated to `1.1.10`.

## Build note

The project is source-structure validated in this environment. I attempted a full Gradle build, but the wrapper could not resolve `services.gradle.org` (`curl: (6) Could not resolve host`), so dependency-backed compilation could not run here. Local Java parsing reached type-resolution errors only (no syntax-form errors were reported), and the final archive is integrity-tested separately.

Build locally on Windows with:

```powershell
.\gradlew.bat build
```

Expected jar:

```text
build\libs\Bonggerz-Wither-Printer-1.21.11-1.1.10.jar
```
