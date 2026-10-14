# Wither Printer — Minecraft 1.21.11 / Meteor Client

Made by [Bonggerz](https://github.com/Bonggerz)

A continuous flying Wither printer for Meteor Client. v1.1.10 restores the Motion/Look direction setting (Motion remains default) and replaces equal-stack/full-shulker restocking with a smart 4:3 material planner that only takes Soul Sand/Soul Soil and Wither Skeleton Skulls from supply shulkers.

## How to use

1. Use default settings
2. Fill the top half of your ender chest with griefing kits
3. Put 1 stack of ender chests somewhere in your inventory/hotbar
4. Put soul sand and wither skulls in your inv/hotbar
5. Activate the module

## v1.1.10 smart material-only restocking

- Restored `direction-mode` under **General** with `Motion` as the default and `Look` as the alternative.
- `Motion` follows actual horizontal velocity and falls back to look direction only when movement is nearly zero. `Look` always uses the direction the player faces.
- Supply-shulker transfer is now **material-only**: the restocker never QUICK_MOVE clicks Name Tags, tools, blocks, food, rockets, or any other non-Wither item in the shulker.
- Eligible source items are only Soul Sand, Soul Soil, and Wither Skeleton Skulls.
- If every eligible soul/skull stack fits in the player inventory/hotbar, the printer takes **all** eligible material stacks and leaves every non-material source stack untouched.
- If every eligible material stack does not fit, the planner counts the soul/skull items the player already carries (including a usable offhand stack), then chooses complete source stacks to maximize complete Withers and keep the resulting carried inventory as close as possible to the real `4 soul : 3 skull` recipe ratio.
- For an otherwise empty 27-slot/full-stack loadout, the integer optimum is `15 soul stacks + 12 skull stacks`: 15 soul stacks support 240 Withers while 12 skull stacks support 256, which is better than the neighboring 16+11 split.
- Existing excess is compensated. Example: if the player already carries many skull stacks and little/no soul material, a tight partial refill can select mostly or entirely soul stacks until the carried totals are balanced again.
- Firework Rockets in the **player inventory** remain expendable and may be dropped only when dropping them strictly improves the smart material refill. Existing carried soul/skull stacks are no longer discarded merely to enlarge a partial shulker transfer.
- The selected material source slots are still sent as one QUICK_MOVE burst and only those selected slots are retried.

## v1.1.9 GUI cleanup without changing staging behavior

- Kept the existing manual staging-slot system exactly as requested.
- Moved `restock-staging-slot` from **Auto Restock** to **Inventory**. The default remains hotbar slot `7`.
- `name-tag-staging-slot` remains in **Wither Naming**.
- Changed the normal `behind-distance` default from `2.0` to `2.5`.
- Removed `direction-mode` from the GUI. The printer now always uses the previous **Motion** behavior internally, including its look-direction fallback while nearly stationary.
- Removed `use-soul-soil` from the GUI. Soul Soil is now always accepted alongside Soul Sand.

## v1.1.8 integrated Auto Walk and requested defaults

- Added `auto-walk` to General, default `ON`. It holds forward during normal travel and Wither construction, yields to the automatic-restock movement state machine, and releases forward during the final Name Tag interaction where naming intentionally holds position.
- `name-withers` remains default `OFF`.
- Requested defaults: General `behind-distance 2.0`, `vertical-offset -1`, `wither-spacing 10`; Placement `air-place ON`, `rotate OFF`, `swing OFF`, `max-place-range 4.5`, `burst-placement ON`, `placement-delay 0`; Inventory `auto-refill-hotbar ON`, soul slot `8`, skull slot `9`, refill delay `1`; Auto Restock `ON`, travel `10.0`, staging slot `7`, action delay `0`, settle delay `1`, timeout `80`; Elytra Fly management `ON`, not-placing `2.0`, placing `0.2`, acceleration disabled; Wither Naming profile `InventoryCycle`, tag slot `6`, behind `1.0`, vertical `-1`, placing speed `0.00`; Reliability confirmation `1`, retries `4`, spawn confirmation `4`.
- `wither-spacing` now allows values from `3` through `50`, with the GUI slider also reaching `50`.
- Settings group order is now `Auto Restock -> Elytra Fly -> Wither Naming -> Reliability`.
- The Fabric/Meteor addon display name is now `Wither Printer`; author metadata remains `Bonggerz`.


## v1.1.7 naming-specific geometry and placing speed

When `name-withers` is enabled, the Wither Naming group now has its own build-position and construction-speed settings:

- `naming-behind-distance` — replaces General `behind-distance` while naming is enabled. Default `1.0`.
- `naming-vertical-offset` — replaces General `vertical-offset` while naming is enabled. Default `-1`.
- `naming-placing-speed` — replaces Elytra Fly `placing-speed` while naming is enabled. Default `0.0`.

This lets you keep one normal printer profile and a separate geometry/speed profile for named Withers. Disabling `name-withers` immediately returns the printer to the normal General/Elytra values. The final Name Tag interaction itself still holds managed Elytra Fly at `0.0` horizontal speed for reliability; `naming-placing-speed` controls the actual Wither construction phase.

Example:

```text
Name Withers:              ON
Naming Behind Distance:    1.0
Naming Vertical Offset:   -1
Naming Placing Speed:      0.10

Normal Behind Distance:    2.5
Normal Vertical Offset:   -2
Normal Placing Speed:      0.30
```

With naming ON, the first three naming values are used. With naming OFF, the normal values are used.

## v1.1.6 multiplayer Name Tag packet fix

v1.1.5 still used only `MultiPlayerGameMode.interact(...)` for the Name Tag. Testing showed that path works in singleplayer but can be rejected on a multiplayer server even at very short distance while stationary. Minecraft 1.21.11's real `startUseItem()` flow does something more specific for an entity hit: it first calls `interactAt(...)` with the exact hit point, then calls generic `interact(...)` only when the first result does not consume the action.

v1.1.6 now mirrors that vanilla sequence:

- the printer remains stopped and UUID-locked to the exact newly spawned Wither;
- it waits for the expected renamed Name Tag to be synchronized in the main hand;
- it rotates toward the current Wither hitbox;
- it computes the view-ray entry point on the Wither's current AABB;
- it constructs a real `EntityHitResult` for that point;
- it sends `interactAt(player, wither, hitResult, MAIN_HAND)` first;
- if that result does not consume the action, it immediately follows with `interact(player, wither, MAIN_HAND)`, exactly like vanilla 1.21.11;
- confirmation/retry logic is unchanged, so a successful tag consumption or synchronized custom-name update completes naming without duplicate tags.

This removes the client/server protocol mismatch that was invisible in the integrated singleplayer server but can matter to multiplayer validation or anticheat.

## v1.1.5 Name Tag interaction reliability

v1.1.3 added automatic naming, but its first interaction path could select a tag and interact in the same client tick while the player was still moving at `placing-speed`. On multiplayer servers that can leave the server seeing the previous held item, a stale player position, or an entity outside the smaller entity-interaction range. v1.1.5 changes the naming phase to behave much closer to a deliberate vanilla right click:

- managed Elytra Fly is forced to `0.0` horizontal speed for the entire naming phase;
- when a renamed Name Tag is selected/staged, the module waits a full tick and re-verifies the exact expected tag in the main hand before interacting;
- the exact UUID-tracked new Wither must pass Minecraft's entity-interaction-range check before an interaction is attempted;
- the module rotates server-side toward the Wither's hitbox center before the interaction;
- v1.1.5 used the generic `MultiPlayerGameMode.interact(...)` path; v1.1.6 supersedes this with the full vanilla `interactAt(...)` → conditional `interact(...)` sequence;
- confirmation still accepts either the Wither's custom name update or a decrease in the matching Name Tag count;
- the confirmation grace is slightly longer before another attempt is allowed.

If the Wither stays outside entity range, the module does not spam interactions. It waits while stopped and eventually reports the closest hitbox distance together with the player's synchronized interaction range. That makes an overly-large placement/spacing geometry distinguishable from an actual server-side refusal to name the entity.

## Smart restocking behavior

The current restocker is intentionally **not** a general shulker emptier. It uses the supply shulker only as Wither-material storage.

Rules:

- Only Soul Sand, Soul Soil, and Wither Skeleton Skulls are eligible for transfer.
- Name Tags and every other non-material item remain in the placed supply shulker even when the player has plenty of free inventory space.
- If all eligible material stacks fit, all eligible material stacks are transferred immediately.
- If they do not all fit, the planner uses the player's current carried soul/skull item counts plus the source-shulker material counts to choose a ratio-aware partial load.
- The target is the actual Wither recipe requirement: `4 soul : 3 skull`. The planner maximizes the number of complete Withers supported by the resulting inventory, then favors the material choice that leaves the carried totals closest to that ratio.
- Full source stacks are selected as whole shulker slots because the real transfer path is QUICK_MOVE; normal stack merging into partial player stacks is simulated before a source slot is accepted.
- Soul Sand and Soul Soil both count as the same logical "soul material" for the 4:3 calculation, while still respecting their separate vanilla stack-merging behavior.
- Only selected source slots are QUICK_MOVE replayed/retried. Intentionally ignored Name Tags/other items are never clicked and never treated as a transfer failure.
- The Ender Chest and supply shulker are still left placed in the world.

Examples with full 64-item stacks and no existing material surplus:

```text
Receiving capacity 7 stacks  -> 4 soul + 3 skull
Receiving capacity 10 stacks -> 6 soul + 4 skull
Receiving capacity 14 stacks -> 8 soul + 6 skull
27-slot loadout              -> 15 soul + 12 skull
```

The 27-slot result is not an exact 4:3 stack count because 27 cannot be divided in an exact 4:3 whole-slot ratio. `15 + 12` maximizes the number of complete Withers possible from 27 full stacks.

## Core Wither printing

With `burst-placement` enabled (the v1.1.2 default), every clean Wither build uses three placement ticks:

1. **placement tick 1:** bottom soul block + center/stem soul block
2. **placement tick 2:** left arm soul block + right arm soul block
3. **placement tick 3:** left skull + middle skull + right skull, in that order inside the same tick

The right skull is the last interaction on the clean path because it completes the structure and triggers Wither creation. Confirmation/retry ticks can be inserted when the client/server has not confirmed the previous burst yet. If a burst is only partially accepted, retries target only the missing positions instead of blindly replaying already-present blocks.

Turning `burst-placement` off restores the older one-block-per-placement-tick state machine for troubleshooting or unusually strict servers.

Important behavior:

- Continuous printing; not limited to one Wither.
- Designed for slow flight while the Withers are constructed behind the player.
- Decimal `behind-distance` values such as `1.5`, `2.0`, and `2.5`.
- Deterministic `wither-spacing` based on the previous accepted anchor during straight flight.
- Air-place support for the first soul block.
- Soul-first state machine: no skull is attempted until all 4 soul blocks are confirmed.
- Per-block confirmation/retry handling.
- Final-skull tracking ignores older nearby Withers and treats structure consumption as spawn evidence.
- Full survival inventory support through configurable hotbar staging slots.
- Optional soul soil support.
- Peaceful-mode guard.
- Meteor Elytra Fly speed management with separate placing and non-placing speeds.

## v1.1.1 three-tick burst placement

The original printer intentionally sent at most one world-placement interaction per game tick. v1.1.1 added a burst scheduler so the normal path sends **2 + 2 + 3 placements across three consecutive placement ticks**.

Before a new structure starts, burst mode attempts to stage enough soul material and skulls into the hotbar so a main-inventory refill does not interrupt the three placement bursts. The staging slots are still automatic; you do not need to fill them manually.

Reliability rules remain active:

- the second soul pair is not sent until the first pair is confirmed;
- skulls are not sent until all four soul blocks are confirmed;
- a partial 2-block burst retries only the missing soul position(s);
- a partial 3-skull burst keeps accepted skulls and retries only missing skull positions;
- burst spawn confirmation uses disappearance of the already-confirmed soul structure or a newly observed Wither, avoiding the false-positive skull-disappearance assumption that is only valid in legacy sequential mode;
- `placement-delay = 0` is required for the fastest three consecutive placement-tick path.

With the default `confirmation-wait = 1`, a normal local/sufficiently responsive server update is checked at the start of the next tick. If it is already visible, the next burst is sent immediately during that same tick.


## v1.1.3 automatic Wither naming

Enable `name-withers` to use real, already-renamed Name Tags from the player's normal 36-slot inventory. Plain/unrenamed Name Tags are ignored because the server requires the item itself to already carry a custom name.

Two modes are available:

- **InventoryCycle**: discovers every **distinct renamed Name Tag name** currently present in hotbar/main inventory and loops through those names. Example: stacks named `Alpha`, `Bravo`, and `Charlie` produce `Alpha -> Bravo -> Charlie -> Alpha ...`. If only one distinct renamed name is available, every Wither receives that name. Duplicate stacks of the same name do not create duplicate entries in the cycle.
- **Specific**: uses only a renamed Name Tag whose custom name exactly matches `specific-name`.

The cycle order is remembered independently of physical inventory slot order, so automatically staging a tag into the hotbar cannot scramble the name sequence. Newly discovered names (for example after an automatic supply-shulker refill) are appended to the cycle. Names that are temporarily unavailable are skipped until they become available again.

Naming is bound to the exact Wither created by the current structure: immediately before the final skull burst the printer already snapshots nearby Wither UUIDs. The naming stage reuses that baseline and only targets the newly appearing UUID, so an older Wither standing nearby cannot accidentally receive the tag.

After spawn confirmation, the build plan remains active and v1.1.5 temporarily forces managed Elytra Fly horizontal speed to `0.0` while naming is attempted. The selected renamed tag is automatically staged into `name-tag-staging-slot` if it is only in the main inventory, then the module waits one full tick for held-slot synchronization, verifies entity interaction range, rotates toward the exact spawned Wither, and sends the normal generic entity interaction. Either the Wither's synchronized custom name or a one-item decrease in the matching Name Tag count confirms success. This prevents repeated interactions from consuming multiple tags just because the entity-name update arrived late.

If the requested tag is unavailable, the printer warns and continues with that Wither unnamed rather than stopping the entire printing run. **v1.1.10 smart restocking deliberately does not take Name Tags from supply shulkers**, so tags needed for automatic naming must already be carried in the player inventory/hotbar (or be supplied by some separate workflow).

### Naming settings

- `name-withers`: enable/disable automatic naming. Default `OFF`.
- `name-mode`: `InventoryCycle` or `Specific`.
- `specific-name`: exact custom Name Tag text used only in Specific mode.
- `name-tag-staging-slot`: hotbar slot used automatically when the required tag is in main inventory. Default `6`.
- `naming-behind-distance`: build-anchor behind distance used only while `name-withers` is enabled. Default `1.0`.
- `naming-vertical-offset`: build-anchor vertical offset used only while `name-withers` is enabled. Default `-1`.
- `naming-placing-speed`: active Wither construction speed used only while `name-withers` is enabled. Default `0.0`; the final naming interaction still holds at `0.0`.

## v1.1.2 Ender Chest synchronization fix

On some servers, the Ender Chest screen can become active before its 27 slots have finished synchronizing to the client. v1.1.1 inspected the slots immediately and treated a temporarily empty snapshot as proof that no supply shulker existed. v1.1.2 keeps the Ender Chest open and polls the real slot state until a shulker appears or `restock-timeout` is reached. This adds no delay to the normal fast path when the shulker is already visible.

The same retry-until-timeout behavior is used when re-resolving the selected supply shulker after the module drops one printer-material stack to make room.

## v1.1.0 automatic mid-air restock

When there are no longer enough materials for another complete Wither and `auto-restock` is enabled, the module performs this sequence:

1. Stops the current Wither plan cleanly before beginning any partial next structure.
2. Changes Meteor Elytra Fly to `not-placing-speed`.
3. Temporarily owns the horizontal movement keys and flies forward for `restock-travel-distance` (default `10.0` blocks).
4. Stops/holds position in the air by setting Elytra Fly horizontal speed to `0.0`, clearing horizontal + jump/sneak movement keys, and zeroing local motion while the restock state machine is active.
5. Finds an Ender Chest in player inventory/hotbar and stages it in the configured restock hotbar slot if needed.
6. Air-places the Ender Chest directly above the player's head using the same supportless placement concept used by the supplied shulker mod.
7. Opens the real Ender Chest and selects the first shulker box in its 27 Ender Chest slots.
8. If the player inventory has no free slot to receive that shulker, drops exactly one Wither Printer material stack to make the required shulker slot. It never drops unrelated items.
9. Reserves one exact empty player-inventory slot and moves the supply shulker into it with two `PICKUP` clicks sent in the same client tick, then closes the Ender Chest GUI. This keeps the transfer fast without confusing it with an identical shulker already carried.
10. Stages and air-places that shulker horizontally next to the Ender Chest.
11. Opens the real shulker.
12. Reads the 27 shulker source slots but marks only Soul Sand, Soul Soil, and Wither Skeleton Skulls as eligible.
13. If every eligible soul/skull stack fits in the current 36-slot inventory, selects all eligible material slots. Non-material shulker slots are ignored even when free inventory space exists.
14. If not all eligible material fits, counts the soul/skull items already carried and computes a smart partial source-slot set around the real 4:3 Wither recipe ratio.
15. Sends QUICK_MOVE only for the selected soul/skull source slots in one client tick. A busy server is retried only for those selected slots; Name Tags and every other non-material source slot are never clicked.
16. Closes the shulker once the selected material subset is synchronized, even though intentionally ignored or excess stacks may remain.
17. Leaves both the Ender Chest and the supply shulker placed in the world. They are intentionally **not mined**.
18. Sets Elytra Fly to `placing-speed` and immediately returns control to the Wither printer.

The design intentionally uses real placements and real container clicks. It does not fabricate shulker contents client-side.

### Restock requirements

Automatic restocking requires:

- `auto-restock = ON`
- `air-place = ON`
- `auto-refill-hotbar = ON`
- `manage-elytra-fly-speed = ON`
- Meteor **Elytra Fly** active
- the player actually Elytra-flying when materials run out
- at least one Ender Chest in the 36-slot player inventory/hotbar
- at least one shulker box in the Ender Chest

Each refill consumes one Ender Chest from inventory and one supply shulker from the Ender Chest as placed world blocks, because the requested workflow deliberately leaves both behind.

For the normal use case, fill each supply shulker with Soul Sand/Soul Soil and Wither Skeleton Skulls. v1.1.10 transfers only those Wither materials. If all eligible material fits it takes all of it; otherwise it takes the smartest ratio-aware partial material load and leaves the rest in the placed shulker.

## Fast-transfer design copied from the supplied mod's behavior

The supplied reference mod targets Minecraft 26.2 and uses a client-planning / real-container replay workflow. Its fast mode sends the planned container clicks as a burst after the real shulker is opened.

v1.1.0 reuses that **behavioral idea** for restocking, but the implementation is written for this project's Minecraft 1.21.11 target rather than copying the 26.2 API literally:

- if necessary, the selected supply shulker is moved from Ender Chest to a reserved exact inventory slot with two same-tick `PICKUP` clicks;
- required printer-stack DROP clicks are sent together;
- the module waits for server inventory synchronization;
- all non-empty shulker QUICK_MOVE clicks are then sent in one client tick;
- only late/rejected source slots are retried.

This is important because the supplied 26.2 source uses newer container-input APIs that are not the same names in Minecraft 1.21.11 official Mojang mappings. The 1.21.11 implementation uses `ClickType` with `MultiPlayerGameMode.handleInventoryMouseClick(...)`.

## Recommended first test settings

Start in a singleplayer survival world or another controlled test environment:

```text
air-place:                       ON
manage-elytra-fly-speed:         ON
auto-refill-hotbar:              ON
not-placing-speed:               2.0
placing-speed:                   0.2

auto-restock:                    ON
restock-travel-distance:         10.0
restock-staging-slot:            7
restock-action-delay:            0
restock-settle-delay:            1
restock-timeout:                 80

burst-placement:                 ON
placement-delay:                 0
confirmation-wait:               1
spawn-confirmation-wait:         4
max-place-range:                 4.5
behind-distance:                 2.5
vertical-offset:                -1
wither-spacing:                  10
```

A useful first restock test is to begin with only enough loose material for one or two Withers, carry one Ender Chest in inventory, and put one supply shulker containing soul sand/soul soil + skulls in the Ender Chest. This makes the depletion-to-restock transition easy to observe.

## Settings

### General

- `direction-mode`: `Motion` (default) or `Look`. Motion follows actual horizontal movement and falls back to look direction when nearly stationary; Look always follows where the player faces.
- `auto-walk`: automatically holds forward during normal printing/travel; default `ON`.
- `behind-distance`: horizontal distance behind the player when Wither Naming is disabled. Decimal values are supported. Default `2.5`.
- `vertical-offset`: Wither base Y relative to player feet when Wither Naming is disabled.
- `wither-spacing`: requested anchor-to-anchor spacing; range `3-50`, default `10`.
- Soul Soil is always accepted alongside Soul Sand; there is no Soul Soil toggle in v1.1.9+.
- `stop-when-empty`: disables when there is not enough material and an automatic restock cannot start. When auto-restock starts successfully, it takes precedence over this setting.

### Placement

- `air-place`: supportless placement path for the bottom soul block and required by automatic restocking.
- `rotate`: Meteor rotation before world interaction.
- `swing`: hand swing on placement/interaction.
- `max-place-range`: eye-to-target placement guard.
- `burst-placement`: default `ON`; sends 2 lower soul blocks, then 2 soul arms, then all 3 skulls across three placement ticks.
- `placement-delay`: extra ticks between placement bursts. Keep `0` for consecutive-tick 2 + 2 + 3 building.

### Inventory

- `auto-refill-hotbar`: permits material use from the main inventory.
- `soul-staging-slot`: hotbar slot used to stage soul material.
- `skull-staging-slot`: hotbar slot used to stage skulls.
- `refill-delay`: wait after a normal inventory-to-hotbar move.

Soul and skull staging slots must be different. Swaps preserve displaced items.

### Auto Restock

- `auto-restock`: enables the full mid-air Ender Chest/shulker refill state machine. Default `ON`.
- `restock-travel-distance`: horizontal displacement before stopping to restock. Default `10.0`, decimal setting.
- `restock-staging-slot`: shown under **Inventory**; hotbar slot used to stage the Ender Chest and supply shulker. Default `7` (GUI numbering 1-9).
- `restock-action-delay`: delay between world interactions. Default `0`; increase it only if a server rejects interactions.
- `restock-settle-delay`: normal server-sync wait after the one-tick transfer burst. Default `1`.
- `restock-timeout`: maximum wait for each placement/open/sync stage before a safe abort. Default `80` ticks.

If a restock stage fails, the module closes an open container if possible, restores owned input state, and disables itself rather than blindly placing more blocks or dropping more items in an uncertain state.

### Elytra Fly

- `manage-elytra-fly-speed`: lets Wither Printer control Meteor Elytra Fly's horizontal speed.
- `not-placing-speed`: used while travelling between builds and during the configured restock relocation. Default `2.0`.
- `placing-speed`: used while a Wither is actively being placed when Wither Naming is disabled. Default `0.2`; naming-enabled builds use `naming-placing-speed`.
- During Ender Chest/shulker interaction, the managed speed is temporarily `0.0` so the player stays near the blocks.
- `disable-acceleration-while-active`: temporarily disables Elytra Fly acceleration so requested speeds apply directly.
- The original Elytra Fly horizontal-speed and acceleration values are restored when Wither Printer stops controlling them.

The printer does not automatically enable Elytra Fly.

### Reliability

- `confirmation-wait`: block-update wait before retrying a normal Wither placement.
- `max-retries`: attempt budget for one position.
- `spawn-confirmation-wait`: final-skull/Wither spawn grace window.
- `restock-timeout`: equivalent safety budget for each restock state.

## Build on Windows

Open PowerShell in the extracted project folder and run:

```powershell
.\gradlew.bat build
```

Expected jar:

```text
build\libs\Bonggerz-Wither-Printer-1.21.11-1.1.10.jar
```

Copy that jar into the same Minecraft `mods` folder as Meteor Client.

## Target/build notes

The project targets:

- Minecraft `1.21.11`
- Java `21`
- Fabric Loader `0.19.3`
- Fabric Loom `1.17-SNAPSHOT`
- Meteor Client `1.21.11-SNAPSHOT`
- official Mojang mappings

Official mappings are intentional. In particular, do not copy the supplied Minecraft 26.2 shulker mod's container-click API names into this project: v1.1.2 retains the v1.1.0 translation of the fast replay behavior to the 1.21.11 official-mapping API.

## Previous fixes retained

### v1.0.1

Fixed alternating missing-final-skull behavior by snapshotting nearby Wither UUIDs immediately before the third skull and refusing to let an older nearby Wither complete the current cycle.

### v1.0.2

Fixed false `Wither did not spawn` warnings, spawn-consumption handling, `wither-spacing`, decimal `behind-distance`, and separate placing/non-placing Meteor Elytra Fly speed control.
