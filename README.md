# Wither Printer — Minecraft 26.2 / Meteor Client

Made by [Bonggerz](https://github.com/Bonggerz)

A continuous flying Wither printer addon for [Meteor Client](https://meteorclient.com). It builds Withers behind you while you fly, can name them automatically, and restocks itself mid-air from shulkers in your ender chest.

## How to use

1. Use default settings
2. Fill the top half of your ender chest with griefing kits
3. Put 1 stack of ender chests somewhere in your inventory/hotbar
4. Put soul sand and wither skulls in your inv/hotbar
5. Activate the module

## Demo

<video src="https://raw.githubusercontent.com/ComicSquid74273/Wither-printer/main/wither.mp4" controls muted></video>

[Open the demo video on GitHub](https://github.com/ComicSquid74273/Wither-printer/blob/main/wither.mp4) if the player does not render.

## Features

- Continuous Wither printing while elytra flying — no one-Wither limit
- Fast 2+2+3 burst placement (three ticks per Wither) with per-block confirmation and retries
- Air-place support and deterministic `wither-spacing` along your flight path
- Automatic hotbar staging from the main inventory
- Automatic mid-air restock: places an ender chest + supply shulker, takes only soul sand/soul soil and skulls in the real 4:3 recipe ratio
- Optional automatic naming with renamed name tags (InventoryCycle or a specific name)
- Meteor Elytra Fly speed management with separate placing and travelling speeds
- Pauses automatically while Auto Eat / Auto Gap / manual eating is active and resumes afterwards
- Tamper-evident lifetime wither counter per server; `.witherstats` prints the counts plus the running jar's SHA-256 to match against the GitHub release digest
- Peaceful-mode guard and safe abort on failed restock stages

## Requirements

- Minecraft 26.2 with Meteor Client (Java 25)

## Build

Run from the project folder (the Gradle JVM must be Java 25):

```powershell
.\gradlew.bat build
```

Copy the jar from `build\libs\` into the same Minecraft `mods` folder as Meteor Client.

Version-by-version details live in [CHANGELOG.md](CHANGELOG.md). For Minecraft 1.21.11, use the [`1.21.11` branch](https://github.com/ComicSquid74273/Wither-printer/tree/1.21.11).
