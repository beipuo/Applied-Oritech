# Applied Oritech

[简体中文](README.md) | English

Applied Oritech integrates **Applied Energistics 2 (AE2)** with **Oritech**. It adds the **ME Dock, ME Pattern Provider Addon, and ME Interface Addon**, connecting Oritech machines to ME networks for autocrafting and item stocking.

## Requirements

- Minecraft 1.21.1 / NeoForge
- Applied Energistics 2 (AE2) 19.2.0 or newer
- Oritech 1.2.12 or newer
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

When ExtendedAE is installed, two additional components are registered:

| Component | Function |
| --- | --- |
| ME Extended Pattern Provider Addon | Provides 36 pattern slots and reuses ExtendedAE's extended pattern provider GUI. |
| ME Extended Interface Addon | Provides 36 stock configuration slots and reuses ExtendedAE's paged interface GUI. |

These addons use Applied Oritech's models, directional placement, and collision shapes, with the texture combinations used by the corresponding ExtendedAE parts.

The integration targets ExtendedAE `1.21-2.2.36-neoforge`. ExtendedAE is optional; these two addons are not registered when it is absent.

## Transfers and Configuration

The current machine adapter handles items: ingredients go only into input slots, and automatic return extracts only from output slots. Fluid transfer is not currently included.

Configuration options control ME network idle power for docks and addons, machine RF consumption per transfer, and whether automatic output return is enabled by default.

## Credits

Thanks to the authors of Applied Energistics 2, Oritech, and ExtendedAE. The addon models are modified from AE2 models; the relevant textures and GUIs come from the corresponding dependency mods.
