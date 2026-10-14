# Wither Printer

A continuous flying Wither printer addon for [Meteor Client](https://meteorclient.com). It places Withers behind you while you fly and automatically restocks from shulkers in your ender chest.

Made by [Bonggerz](https://github.com/Bonggerz)

## How to use

1. Use default settings
2. Fill the top half of your ender chest with griefing kits
3. Put 1 stack of ender chests somewhere in your inventory/hotbar
4. Put soul sand and wither skulls in your inv/hotbar
5. Activate the module

## Versions

Each Minecraft version lives on its own branch:

| Branch | Minecraft | Java |
|--------|-----------|------|
| [`1.21.11`](https://github.com/ComicSquid74273/Wither-printer/tree/1.21.11) | 1.21.11 | 21 |
| [`26.2`](https://github.com/ComicSquid74273/Wither-printer/tree/26.2) | 26.2 | 25 |

Each branch is a complete, buildable Fabric addon project. Switch to the branch for your Minecraft version and build with:

```powershell
.\gradlew.bat build
```

Copy the resulting jar from `build\libs\` into the same Minecraft `mods` folder as Meteor Client.
