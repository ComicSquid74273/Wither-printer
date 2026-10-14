## 1.2.0

- Added `pause-while-eating` (General, default ON): all placement, restock, and naming actions pause while Meteor Auto Eat, Auto Gap, or manual eating is active, then resume automatically where they stopped. Eating can no longer be cancelled by the printer's hotbar swaps and placements.
- Added lifetime per-server wither statistics: every accepted skull placement is folded into a tamper-evident ledger (`wither-printer-stats.bin` in the Minecraft directory) protected by a per-server chain hash and a whole-file SHA-256 digest; a hand-edited file is reported as corrupted instead of trusted.
- Added the `.witherstats` (`.ws`) command: prints withers placed, skulls placed, server, player name and UUID, plus the SHA-256 of the jar currently running so it can be matched against the digest GitHub publishes for the official release jar.
- The module HUD info string now shows `eating` while the eating pause is active.

## Minecraft 26.2 port (mod version 1.1.10)

- Retargeted the project from Minecraft 1.21.11 / Java 21 / Meteor 1.21.11 to Minecraft 26.2 / Java 25 / Meteor 26.2.
- Switched inventory interaction from `ClickType` + `handleInventoryMouseClick(...)` to `ContainerInput` + `handleContainerInput(...)`.
- Updated active-screen access from `Minecraft.screen` to `Minecraft.gui.screen()`.
- Updated automatic Wither naming to 26.2's consolidated `interact(player, entity, EntityHitResult, hand)` call while retaining the exact hit point, range guard, UUID lock, rotation, and confirmation logic.
- Updated Fabric compatibility metadata and artifact/root names to 26.2.
- Verified dependency-backed Java compilation and a clean release build against the Minecraft 26.2 and Meteor 26.2 classpaths.

## 1.1.10

- Restored the `direction-mode` GUI setting with `Motion` as the default and `Look` as the alternative.
- Replaced equal-stack/full-shulker restocking with smart recipe-ratio restocking based on the real `4 soul : 3 skull` Wither requirement.
- Supply-shulker QUICK_MOVE selection is now strictly limited to Soul Sand, Soul Soil, and Wither Skeleton Skulls.
- Name Tags and every other non-Wither item in the supply shulker are always left untouched, even when free player inventory space exists.
- If all eligible soul/skull source stacks fit, all eligible material stacks are taken.
- If they do not all fit, the planner includes the player's current carried material counts (plus a usable offhand stack), simulates real inventory merges, and chooses source stacks that maximize complete Withers while keeping the resulting carried load closest to the 4:3 item ratio.
- The 27-full-stack empty-loadout optimum resolves to 15 soul stacks + 12 skull stacks.
- Firework Rockets in the player inventory may still be discarded when that strictly improves a partial smart refill, but existing carried soul/skull stacks are no longer discarded merely to enlarge the partial transfer.
- Existing fixed Soul/Skull/Restock/Name Tag staging-slot behavior is unchanged.

## 1.1.9

- Kept the existing fixed Soul, Skull, Restock, and Name Tag staging-slot design.
- Moved `restock-staging-slot` from Auto Restock into the Inventory settings group; default remains `7`.
- Left `name-tag-staging-slot` in Wither Naming.
- Changed normal `behind-distance` default to `2.5`.
- Removed the `direction-mode` GUI setting and hard-coded the previous Motion behavior, with look-direction fallback while nearly stationary.
- Removed the `use-soul-soil` GUI setting and now always accept Soul Soil alongside Soul Sand.

## 1.1.8

- Added built-in forward-only `auto-walk` under General; enabled by default.
- Auto Walk yields to automatic restock movement and pauses during the final Name Tag interaction, then resumes immediately.
- Applied the requested screenshot defaults while keeping `name-withers` disabled by default.
- Changed normal `vertical-offset` default to `-1`, `wither-spacing` default to `10`, `swing` default to OFF, restock travel to `10.0`, restock action delay to `0`, restock settle delay to `1`, and spawn confirmation wait to `4`.
- Changed naming-profile defaults to behind distance `1.0`, vertical offset `-1`, and naming placing speed `0.00`.
- Increased the maximum/slider maximum for `wither-spacing` to `50`.
- Moved the Wither Naming settings group below Elytra Fly.
- Changed the Fabric/Meteor display name from `Bonggerz Wither Printer` to `Wither Printer` while retaining Bonggerz as author metadata.

# Changelog

## 1.1.7

### Added — naming-specific build geometry and speed

- Added `naming-behind-distance` to the Wither Naming group. When `name-withers` is enabled, new Wither anchors use this value instead of the normal General `behind-distance`.
- Added `naming-vertical-offset` to the Wither Naming group. When `name-withers` is enabled, new Wither anchors use this value instead of the normal General `vertical-offset`.
- Added `naming-placing-speed` to the Wither Naming group. When `name-withers` is enabled, active Wither construction uses this Elytra Fly speed instead of the normal Elytra Fly `placing-speed`.
- When `name-withers` is disabled, the original `behind-distance`, `vertical-offset`, and `placing-speed` settings are used exactly as before.
- The actual post-spawn Name Tag interaction still temporarily holds managed Elytra Fly at `0.0` speed for the multiplayer reliability behavior introduced in v1.1.5/v1.1.6.
- Naming-specific settings are visible only while `name-withers` is enabled.

## 1.1.6

### Fixed — multiplayer Wither naming

- Replaced the v1.1.5 generic-only Name Tag interaction with Minecraft 1.21.11's real vanilla entity right-click sequence.
- Naming now sends `interactAt(...)` with a geometrically valid `EntityHitResult` first, then sends generic `interact(...)` only when the specific interaction does not consume the action.
- The `INTERACT_AT` hit position is the entry point of the eye-to-Wither-center ray through the target's current AABB, matching the shape of an actual vanilla crosshair hit instead of inventing an arbitrary relative vector.
- Existing one-tick held-slot synchronization, zero-speed naming hold, entity-range validation, server-side rotation, UUID locking, and confirmation logic are retained.
- This specifically targets the observed behavior where automated naming works in singleplayer but is rejected on a multiplayer server even while stationary at `behind-distance = 1`.

## 1.1.5

### Fixed — Wither Name Tag interactions

- Reworked the post-spawn naming interaction after v1.1.3/v1.1.4 could repeatedly report `Name Tag interaction ... was not confirmed` without applying the tag.
- Naming now forces managed Elytra Fly horizontal speed to `0.0` for the whole naming state instead of continuing to drift at `placing-speed`.
- A Name Tag hotbar selection is now given a full client tick to synchronize before the entity interaction is sent; the main-hand stack is re-verified by exact custom name first.
- Added Minecraft 1.21.11's real entity-interaction-range check. The printer no longer burns retries when the exact spawned Wither is outside entity reach.
- Added an explicit range timeout diagnostic showing closest hitbox distance and the player's synchronized entity-interaction range.
- Naming now rotates server-side toward the center of the exact UUID-tracked Wither before interaction, independent of the normal block-placement `rotate` setting.
- Uses the normal generic `MultiPlayerGameMode.interact(...)` right-click-entity path used for Name Tags; no unnecessary INTERACT_AT packet is sent first.
- Increased the post-interaction confirmation grace slightly to reduce duplicate retries on delayed server inventory/entity metadata.
- Exact UUID target locking, InventoryCycle/Specific selection, Name Tag protection, balanced partial restocking, and 2+2+3 burst placement are retained.

## 1.1.4

- Changed automatic restocking so a supply shulker no longer has to fit completely. Capacity shortfalls now produce a partial refill instead of a fatal abort.
- Added a balanced partial-transfer planner. When both soul material and skulls are present, partial mode selects equal numbers of soul stacks and skull stacks.
- Larger stacks are preferred within balanced pairs.
- After balanced soul/skull pairs, renamed Name Tags are prioritized for any remaining safe capacity, followed by other non-soul/non-skull shulker contents.
- If only one Wither material type exists in the supply shulker, the restocker takes as much of that type as fits.
- Only selected shulker source slots are QUICK_MOVE clicked/retried; deliberately-left-behind slots no longer cause a timeout or abort.
- Restock transfer timeout now keeps everything already accepted, closes the shulker, and resumes instead of disabling solely because leftovers remain.
- Renamed Name Tags remain protected from inventory cleanup.
- Firework Rockets are now expendable restock cleanup candidates and are preferred for dropping before soul/skull stacks because managed Elytra Fly does not require rockets.
- The initial empty-slot step for taking a supply shulker can also discard a Firework Rocket stack before useful Wither materials.
- Restock completion messages now distinguish full and partial refills and report remaining source-stack count.

## 1.1.3

- Added optional automatic Wither naming with real pre-renamed Name Tags.
- Added `Wither Naming` setting group.
- Added `name-withers`.
- Added `name-mode` with `InventoryCycle` and `Specific`.
- Added `specific-name` for exact-name selection.
- Added automatic `name-tag-staging-slot` (default hotbar slot 6).
- Inventory Cycle discovers distinct renamed Name Tag names automatically; no GUI list is required.
- If only one distinct renamed name exists, Inventory Cycle repeatedly uses that one.
- Cycle order is stable across hotbar staging/inventory reordering and newly discovered names are appended.
- Naming is locked to the exact newly spawned Wither UUID using the existing pre-final-skull Wither baseline.
- Elytra Fly remains at placing speed until naming completes or safely times out.
- Naming confirmation accepts either the Wither's synchronized custom name or the expected Name Tag count decreasing, avoiding accidental multi-tag consumption on delayed entity metadata.
- Missing requested tags produce a warning and the printer continues rather than stopping the full run.
- Retains all v1.1.2 restock synchronization fixes and v1.1.1 2+2+3 burst placement behavior.

## 1.1.2

- Fixed an intermittent automatic-restock race where an Ender Chest menu could open before its 27 slots had synchronized, causing a false `does not contain a shulker box` abort even when a supply shulker was present.
- The restocker now polls the already-open Ender Chest until a shulker appears or `restock-timeout` is genuinely reached. The normal fast path is unchanged: if the shulker is visible immediately, it is taken immediately.
- Applied the same synchronization tolerance after dropping a printer-material stack to make an empty slot, so a temporarily missing exact shulker match no longer aborts the refill on the first stale slot snapshot.
- Timeout messages now distinguish container/slot synchronization failures from a truly immediate absence check.


## 1.1.1

### Added — three-tick 2 + 2 + 3 Wither burst

- Added `burst-placement`, enabled by default.
- Clean-path build schedule is now exactly three placement ticks: base+stem, then both arms, then all three skulls.
- Multiple world placement interactions are sent inside one client tick for each burst; `placement-delay = 0` allows the next confirmed burst on the next game tick.
- Before starting a burst build, the printer automatically stages soul material and skulls from the main inventory when the hotbar does not contain enough for the corresponding burst.
- Partial soul bursts retry only missing soul positions.
- Partial skull bursts retain accepted skulls and retry only missing skull positions.
- Added burst-specific spawn confirmation. Because the first two skulls are no longer guaranteed to have been visible before the completion interaction is sent, burst mode uses disappearance of the already-confirmed soul structure and/or a newly observed Wither as its immediate completion signal instead of incorrectly treating an unseen skull as a consumed skull.
- Added burst phase text to the module info string.
- Legacy one-block-per-placement-tick behavior remains available by disabling `burst-placement`.

### Retained

- Automatic 50-block Ender Chest / supply-shulker restocking from v1.1.0.
- Decimal behind distance, deterministic Wither spacing, and separate Elytra Fly placing/non-placing speeds.

## 1.1.0

### Added — automatic Ender Chest / shulker restocking

- Added `auto-restock` state machine that starts when fewer than 4 usable soul blocks or 3 wither skeleton skulls remain for the next complete Wither.
- Switches Meteor Elytra Fly to `not-placing-speed`, flies the configured horizontal distance (default 50 blocks), then stops in mid-air for the refill.
- Temporarily owns W/A/S/D during the restock relocation; while stopped it also suppresses jump/sneak so Elytra Fly cannot drift vertically during container interaction. All captured movement-key states are restored afterward.
- Air-places an Ender Chest directly above the player.
- Opens the real Ender Chest and takes the first shulker from its first 27 slots. The receive slot is reserved explicitly and the shulker is moved with two same-tick PICKUP clicks, avoiding ambiguity if an identical shulker is already carried.
- If no inventory slot is free for the shulker, drops one Wither Printer material stack only; unrelated stacks are never selected.
- Air-places the supply shulker next to the Ender Chest.
- Opens the real shulker and capacity-simulates all 27 source slots against the 36 player inventory slots, including stack merging/components.
- If capacity is insufficient, drops only soul sand / enabled soul soil / wither skeleton skull stacks, and stops selecting drops as soon as the complete shulker can fit.
- Sends the required full-stack DROP operations as one fast click burst, waits for synchronization, then sends QUICK_MOVE for every non-empty shulker source slot in one client tick.
- Retries only source slots that remain after server correction instead of slowing the normal successful fast path.
- Leaves both Ender Chest and emptied shulker in the world; no mining/breaking routine was added.
- Restores the original selected hotbar slot and movement-key state after restock.
- Returns Elytra Fly to `placing-speed` and resumes normal Wither printing.
- Added `restock-travel-distance`, `restock-staging-slot`, `restock-action-delay`, `restock-settle-delay`, and `restock-timeout` settings.
- Added restock state text to the module info string.

### Porting/reliability

- Reviewed the supplied Minecraft 26.2 shulker automation source and reproduced its high-speed real-container replay approach without importing its unrelated preview UI/mining behavior.
- Translated the newer 26.2 container-click API to Minecraft 1.21.11 official Mojang mappings: `ClickType` + `handleInventoryMouseClick(...)`.
- Restock requires `air-place`, `auto-refill-hotbar`, managed Elytra speed, an active Elytra Fly module, actual fall-flying, and an Ender Chest in player inventory.
- Any fatal restock uncertainty closes the open container when possible, restores player input, and disables the printer instead of continuing blindly.

## 1.0.2

- Fixed `wither-spacing` not affecting placement distance after false final-skull aborts.
- Added decimal `behind-distance` values.
- Added separate Elytra Fly placing / not-placing speeds and temporary acceleration management.
- Fixed false `final skull was placed but the Wither did not spawn` messages by treating structure consumption as successful spawn evidence.

## 1.0.1

- Fixed the alternating missing-skull bug caused by an older nearby Wither satisfying the next structure's final-skull confirmation.
- Added final-skull Wither UUID baseline tracking.

## 1.0.0

- Initial continuous flying Wither printer for Minecraft 1.21.11 / Meteor Client.
