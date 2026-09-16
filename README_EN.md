# Applied Oritech

[简体中文](README.md) | English

Applied Oritech integrates **Applied Energistics 2 (AE2)** with **Oritech**. It adds the **ME Dock, ME Pattern Provider Addon, and ME Interface Addon**, connecting Oritech machines to ME networks for autocrafting and item stocking.

## Requirements

- Minecraft 26.1.2 / NeoForge 26.1.2.77 or newer for 26.1.2 / Java 25
- Applied Energistics 2 (AE2) 26.1.10-beta
- Oritech 2.0.0-exp6
- GuideME 26.1.12-beta, Architectury 20.1.14, Athena 4.7.3, GeckoLib 5.5.2
- The dependencies required by those mods

Use mod files intended for the matching Minecraft version and loader.

## Components

| Component | Function |
| --- | --- |
| ME Dock | Installs in an Oritech machine's addon slot and connects its ME addons to the network. Each dock and its assigned addons share one channel. |
| ME Pattern Provider Addon | Reuses the AE2 pattern provider GUI, sends processing-pattern ingredients into machine input slots, and supports automatic output return. |
| ME Interface Addon | Reuses the AE2 interface GUI to supply configured items to machine input slots. Supports crafting and fuzzy cards. |

Both addons support placement in all six directions, including the top, bottom, and four sides of a dock.

## Getting Started

1. Install an ME Dock in a machine's addon slot, then right-click the machine to recognize its addons.
2. Connect the dock with ME cables and provide sufficient network power and channels.
3. Place the desired addons directly against the dock. They connect to the machine through it.
4. Right-click the pattern provider addon to insert processing patterns, or the interface addon to configure the items to supply.

Sneak-right-click a pattern provider addon with an empty hand to toggle automatic output return. When disabled, another route must return the products to the ME network for the corresponding autocrafting job to complete.

Right-click the dock to view its connection status. The machine must still meet Oritech's structure, recipe, and power requirements.

## ExtendedAE Integration

ExtendedAE has no 26.1.2 release yet. This branch does not register or package its two addons. Their legacy sources are retained for a future port.

## Transfers and Configuration

Item ingredients go only into input slots, and automatic return extracts only from output slots. The limited fluid bridge from the original branch is retained; interface stocking and automatic output return still handle items only.

Configuration options control ME network idle power for docks and addons, machine RF consumption per transfer, and whether automatic output return is enabled by default.

## Development and Verification

Build with Java 25 using `./gradlew build`. Start the development client with `./gradlew runClient`. Its `run-26.1.2` directory is separate from the old `run` directory. Put additional development mods such as JEI in `run-26.1.2/mods`.

## Credits

Thanks to the authors of Applied Energistics 2, Oritech, and ExtendedAE. The addon models are modified from AE2 models; the relevant textures and GUIs come from the corresponding dependency mods.
