# v1.1.10 smart-restock / direction-mode note

`direction-mode` is restored as an enum setting. `Motion` remains the default and keeps the existing dominant-horizontal-velocity logic with the `< 0.01` horizontal-motion fallback to look direction. `Look` bypasses velocity and always resolves the current horizontal facing direction.

The restock planner is now source-filtered before capacity planning: only Soul Sand, Soul Soil, and Wither Skeleton Skulls are eligible shulker source slots. No Name Tag or miscellaneous source slot can enter `restockTransferSlots`, so the later one-tick QUICK_MOVE burst and retry loop are structurally unable to transfer those items.

The full-fit test is now performed against only eligible Wither-material source stacks. If all eligible material fits, all eligible material is taken regardless of its ratio; non-material shulker contents remain untouched by design.

When all eligible material does not fit, the partial planner starts from the actual 36-slot player inventory and also includes a usable soul/skull offhand stack in the logical material totals. For each source stack that can fully merge/fit into a simulated inventory, it compares the resulting number of complete Withers (`min(soul / 4, skull / 3)`) and the ratio error (`abs(3*soul - 4*skull)`). It repeatedly selects the fitting source stack that supports the most complete Withers and, on ties, leaves the carried totals closest to the real 4:3 requirement. This compensates for pre-existing inventory surplus instead of assuming the player starts empty.

For ordinary full 64-item source stacks, this produces the expected integer slot allocations: 7 available slots -> 4 soul + 3 skull; 10 -> 6 + 4; 14 -> 8 + 6; and an empty 27-slot target -> 15 + 12. The last split is optimal because 15 soul stacks support 240 Withers while 12 skull stacks support 256; the neighboring 16 + 11 split is skull-limited to 234 complete Withers.

During partial-content capacity expansion, only Firework Rockets are considered expendable. Current soul/skull stacks are preserved because they are part of the ratio state and directly contribute to future Withers. The older initial step that may need one receive slot for taking the supply shulker from the Ender Chest remains unchanged.

---

# v1.1.9 fixed-staging GUI cleanup note

The fixed staging-slot architecture is intentionally retained. Only the GUI organization changed: `restock-staging-slot` is registered in the Inventory SettingGroup, while `name-tag-staging-slot` remains in Wither Naming. Soul and Skull staging behavior is unchanged.

The former Direction Mode setting is removed from user settings. `getTravelDirection()` now directly implements the old Motion branch: dominant horizontal velocity chooses the cardinal travel direction, and horizontal speed below the existing threshold falls back to current look direction. This preserves stationary startup behavior while eliminating the GUI choice.

Soul Soil support is now unconditional. Both world-state validation and inventory-material matching accept Soul Sand or Soul Soil without consulting a setting.

Normal `behind-distance` now defaults to `2.5`. Existing Meteor saved configs may preserve a previously saved value until the setting/module is reset.

---

# v1.1.8 Auto Walk/default-profile note

The integrated `auto-walk` intentionally controls only Minecraft's forward key. It does not replace Elytra Fly physics. During ordinary printing it holds forward continuously; the existing restock state machine temporarily owns all movement keys, and the naming phase releases forward while managed Elytra speed is held at 0.0. The module captures the pre-Auto-Walk forward-key state and restores it when Auto Walk is disabled or the Wither Printer deactivates.

The requested screenshot values are now code defaults. Wither Naming remains disabled by default, so the normal General/Elytra profile is used until the naming toggle is explicitly enabled. The naming-specific geometry/speed values remain separate and become active only while naming is enabled.

---

# v1.1.7 naming-specific geometry/speed note

The printer now resolves three settings through active-value helpers at runtime. If `name-withers` is enabled, `naming-behind-distance`, `naming-vertical-offset`, and `naming-placing-speed` override the normal General/Elytra values. If naming is disabled, the original settings remain the source of truth. The build plan freezes the selected anchor coordinates when it is created, while the managed Elytra speed is resolved continuously. The dedicated post-spawn naming hold remains `0.0` for entity-interaction reliability and is intentionally not replaced by `naming-placing-speed`.

# v1.1.6 multiplayer naming protocol note

Minecraft 1.21.11 vanilla `Minecraft.startUseItem()` does not use a bare generic entity interaction when the crosshair hits an entity. It calls `MultiPlayerGameMode.interactAt(player, entity, EntityHitResult, hand)` first and only calls `interact(player, entity, hand)` when the first result does not consume the action. `MultiPlayerGameMode.interactAt` serializes the hit point relative to the entity into `ServerboundInteractPacket`; `interact` sends the generic form.

The previous generic-only implementation was accepted by the integrated singleplayer server but user testing showed it was rejected on multiplayer even at short range while stationary. v1.1.6 therefore mirrors the exact vanilla packet ordering and constructs the hit point by clipping the eye-to-hitbox-center ray against the Wither's current AABB.

# v1.1.5 partial-restock implementation notes

The previous capacity model had an all-or-nothing endpoint: after dropping only allowed Wither materials, the full 27-slot source still had to fit or the restock aborted. v1.1.5 separates **full-fit** and **partial-fit** planning. Full fit remains the preferred fast path. Partial fit is used only when protected inventory makes the complete shulker impossible.

Partial mode models a virtual 36-slot player inventory and selects complete source stacks that can be QUICK_MOVE transferred. If both soul material and wither skeleton skulls are present, source slots are admitted strictly in soul+skull pairs, guaranteeing equal source-stack counts. The pair search prefers the pair containing the most actual items. This is an equal-stack policy, not a 4:3 recipe-ratio policy, because the requested behavior was equal amounts of source stacks.

A single freed inventory slot may not improve a balanced plan because the next admissible unit is a two-stack soul+skull pair. The drop optimizer therefore evaluates both one-stack and two-stack cleanup batches; this avoids incorrectly concluding that no useful partial refill is possible when two slots must be freed together.

Existing renamed Name Tags are never cleanup candidates. Firework Rockets are cleanup candidates with highest discard priority, followed by soul material and then skulls. This reflects the managed Elytra Fly workflow, which does not need rockets for propulsion.

Once a partial source-slot set is selected, transfer/retry logic operates only on that set. Remaining shulker slots are intentional leftovers, not an error condition. The supply shulker is left placed in the world with those leftovers.

# v1.1.3 naming implementation notes

Minecraft 1.21.11 exposes `ItemStack.getCustomName()` for detecting the real server-synchronized custom name on a renamed Name Tag, and the client game mode exposes normal entity interaction for applying the tag. The implementation intentionally does not fabricate renamed item data client-side.

Inventory Cycle uses distinct custom-name strings and a persistent cycle-order list rather than rescanning slot order as the authoritative order every Wither. This is necessary because moving a selected tag from main inventory into a hotbar staging slot changes physical slot order.

The existing final-skull UUID baseline is reused to resolve the exact newly spawned `WitherBoss`. Old nearby Withers are excluded by UUID. The build plan remains alive until naming is finished so the anchor and baseline are not discarded too early and managed Elytra Fly stays at placing speed.

Naming interaction success is accepted when either (a) the target Wither's custom name equals the requested name or (b) the total count of matching renamed Name Tags in slots 0-35 decreases. The second signal allows the printer to move on when server item synchronization arrives before entity metadata and avoids blindly sending another tag interaction.

# Research notes — Wither Printer v1.1.2

## Goal

Extend the existing continuous flying Wither Printer so material depletion does not immediately end the run. The requested sequence is:

1. switch Elytra Fly to non-placing speed;
2. fly about 50 blocks;
3. stop in air;
4. air-place an Ender Chest one block above the player/head area;
5. take one supply shulker from the Ender Chest;
6. place it next to the Ender Chest;
7. empty all 27 shulker slots into player inventory as quickly as practical;
8. if space is insufficient, drop only Wither Printer materials and only enough stacks to make room;
9. leave Ender Chest + shulker placed;
10. restore placing speed and continue Wither printing.

## Supplied mod review

The attached reference project is a Meteor addon called Shulker Tweaks / Shulker Planner and targets Minecraft 26.2. Its notable real-world execution path is:

- user plans shulker changes client-side;
- the mod stages a real shulker;
- places and opens the real block;
- replays the planned real container clicks;
- optional cleanup can mine the placed shulker.

The important performance idea for this project is the replay mode: the fast path sends multiple valid container operations in one client tick rather than artificially inserting a tick between every slot transfer.

The reference's above-head air-placement target is based on floor(player X/Z) and ceil(player bounding-box maxY), then interacts with a hit result centered on the target block using `Direction.UP`. v1.1.0 uses the same geometric/interaction idea for the restock Ender Chest.

The reference mod's preview UI and optional shulker mining are not needed for this workflow and were not imported. The requested restocker intentionally leaves both blocks behind.

## Minecraft 26.2 -> 1.21.11 API difference

The uploaded source is not directly source-compatible with this project. In particular, its newer container-input code uses API names from Minecraft 26.2.

For Minecraft 1.21.11 official Mojang mappings, real inventory interaction is implemented with:

- `net.minecraft.world.inventory.ClickType`
- `MultiPlayerGameMode.handleInventoryMouseClick(...)`

Relevant operations used by v1.1.0:

- `ClickType.SWAP` — stage an inventory item in a hotbar slot
- `ClickType.PICKUP` — move the selected supply shulker into one reserved exact player slot using two same-tick clicks
- `ClickType.QUICK_MOVE` — shift-click all non-empty shulker-content slots during the transfer burst
- `ClickType.THROW` with button `1` — throw a complete selected stack

This translation is deliberate; copying 26.2 `ContainerInput` / `handleContainerInput` calls into a 1.21.11 official-mapping addon would be the wrong API target.

## Restock state machine

The state machine separates world/network operations so each server-visible action can be acknowledged without slowing down the container replay itself:

- Travel
- PrepareEnderChest
- WaitEnderChestStage
- PlaceEnderChest
- WaitEnderChestBlock
- OpenEnderChest
- WaitEnderChestScreen
- WaitEnderChestSpace
- WaitShulkerPickup
- WaitEnderChestClose
- PrepareShulker
- WaitShulkerStage
- PlaceShulker
- WaitShulkerBlock
- OpenShulker
- WaitShulkerScreen
- PrepareCapacity
- WaitCapacitySync
- Transfer
- Settle
- WaitShulkerClose

Fatal stage failures disable the module after restoring owned input instead of automatically repeating against uncertain inventory state.

## Automatic 50-block flight

When restocking begins:

- travel start position and horizontal travel direction are captured;
- the user's W/A/S/D plus jump/sneak pressed states are captured;
- Elytra Fly is switched to `not-placing-speed`;
- W is held by the module while S/A/D are released so the relocation is not bent by stale movement input;
- horizontal displacement from the restock start is measured;
- after `restock-travel-distance`, all horizontal movement keys are released;
- managed Elytra speed becomes 0.0 and local delta movement is zeroed while container interactions run;
- jump/sneak are also released while stopped so vertical Elytra input cannot move the player away from the container setup;
- all captured movement-key states are restored at the end or on abort.

The module does not enable Elytra Fly itself. Automatic restocking requires Elytra Fly already active, the player actually fall-flying, and `auto-refill-hotbar` enabled so transferred main-inventory materials can feed the normal printer.

## Ender Chest and supply shulker placement

The restocker requires `air-place` because it must work in open air.

Ender Chest:

- target = floor player X/Z, ceil player bounding-box maxY;
- block must be replaceable/placeable and within configured `max-place-range`;
- supportless interaction is sent directly at the target block using an UP-face block hit;
- waits for the Ender Chest block before trying to open it.

Supply shulker:

- first shulker in the Ender Chest's 27 slots is selected;
- a known empty player slot is reserved, then two `PICKUP` clicks are sent in the same client tick to move that exact shulker into that exact slot; this avoids confusing it with a pre-existing identical shulker while preserving the fast replay style;
- after taking it, a horizontal block next to the Ender Chest is selected from left/right/forward/back candidates;
- the shulker is air-placed there and confirmed before opening.

Neither block is mined afterward.

## Inventory capacity and drop safety

The main safety requirement is that the restocker may never discard unrelated inventory contents just to fit a supply shulker.

The code therefore:

1. copies all 36 player inventory/hotbar stacks;
2. copies all 27 supply-shulker source stacks;
3. simulates normal same-item + same-components merging into existing partial stacks;
4. calculates how many physical inventory slots the complete incoming contents still require;
5. considers only the configured Wither Printer material classes as possible whole-stack drops:
   - soul sand;
   - soul soil when `use-soul-soil` is enabled;
   - wither skeleton skulls;
6. virtually removes candidate printer-material stacks and stops as soon as all incoming contents fit;
7. aborts without dropping unrelated items if a complete fit cannot be achieved using only those candidates.

Before taking the supply shulker itself from the Ender Chest, a full player inventory is handled similarly: one printer-material stack is dropped to create the one required receive slot.

## Fast shulker emptying

The transfer path is intentionally burst-oriented:

- all required whole-stack drops are issued in one tick;
- the module waits until capacity reflects those drops;
- all non-empty source slots 0..26 are QUICK_MOVE clicked in one client tick;
- after a short settle window, if the server corrected any items back, only remaining non-empty slots are replayed;
- once all 27 source slots are empty, the shulker screen closes.

This keeps the normal successful case as fast as the supplied mod's replay concept while still adding a server-sync fallback.

## Elytra speed transitions

With `manage-elytra-fly-speed` enabled:

- normal idle/travel between Wither anchors -> `not-placing-speed`
- active Wither placement -> `placing-speed`
- restock 50-block travel -> `not-placing-speed`
- Ender Chest / shulker interaction -> `0.0`
- successful restock -> immediately `placing-speed`

The previous Meteor Elytra Fly horizontal-speed and acceleration settings are captured and restored when this module stops managing Elytra Fly.

## Recommended defaults

```text
auto-restock = true
restock-travel-distance = 50.0
restock-staging-slot = 7
restock-action-delay = 1
restock-settle-delay = 2
restock-timeout = 80

manage-elytra-fly-speed = true
not-placing-speed = 2.0
placing-speed = 0.2
disable-acceleration-while-active = true
```

The action delay may be tested at 0 in a controlled environment, but the fastest reliable value on a multiplayer server depends on interaction acceptance and block/container update latency.


## v1.1.1 burst-placement notes

The prior builder stopped a tick immediately after each successful `BlockUtils.place` call, limiting the normal path to one block interaction per game tick. v1.1.1 groups independent placements whose supports are already valid:

- base + stem are sent together; the stem placement is sent after the base interaction in the same tick;
- both arms are sent together only after base + stem are confirmed;
- all three skull interactions are sent in left/middle/right order only after all four soul blocks are confirmed.

The reliability model needed a separate burst spawn-confirmation path. Sequential mode can assume the first two skulls were visibly present before sending the third. Burst mode cannot make that assumption because all three interactions are transmitted in one tick. Therefore burst mode uses the confirmed soul structure as the disappearance baseline and a newly observed Wither UUID as the entity baseline; it does not treat an unseen first/second skull as proof of structure consumption.


## v1.1.2 Ender Chest slot-sync race

Observed behavior on a live server: the restock occasionally reported that the Ender Chest contained no shulker even though a valid supply shulker was present. The failure was intermittent, which points to ordering rather than item recognition. The 1.1.1 state machine accepted `ChestMenu` creation as evidence that all 27 Ender Chest slots were already current, then immediately called `firstShulkerSlot`. Network/menu synchronization does not guarantee that ordering on a busy server.

The fix keeps `WaitEnderChestScreen` active while the menu is open but its container slots do not yet expose a shulker. It polls each tick and proceeds on the first valid shulker snapshot. Only `restock-timeout` turns that into a failure. Thus the normal zero-extra-delay path is retained, while delayed slot packets no longer create a false absence. The exact-match re-resolution in `WaitEnderChestSpace` gets the same treatment.
