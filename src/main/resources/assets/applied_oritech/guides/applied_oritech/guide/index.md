---
navigation:
  title: Applied Oritech
  position: 0
item_ids:
  - applied_oritech:me_dock
  - applied_oritech:me_pattern_provider_addon
  - applied_oritech:me_interface_addon
  - applied_oritech:me_extended_pattern_provider_addon
  - applied_oritech:me_extended_interface_addon
---

# Applied Oritech

Applied Oritech connects Oritech machines to an Applied Energistics 2 ME network. The ME Dock connects the machine to the network; addons provide crafting and stocking functions.

## Installation

<ItemImage id="applied_oritech:me_dock" scale="4" />

1. Assemble a working Oritech machine and supply it with energy.
2. Install an ME Dock at a machine addon position and let the machine detect it.
3. Connect an ME cable to the dock. The network must be powered and have an available channel.
4. Attach an ME addon to the machine's addon structure, or place it against the dock connected to that machine. Let the machine detect its addons again if necessary.
5. Right-click the addon to open its configuration menu. An addon needs both a machine connection and a dock before it can be used.

Each dock and its associated addons share one channel. The addons join the network through their dock rather than accepting cable connections themselves. Multiple docks may be used; an addon selects a compatible dock for its machine.

## ME Pattern Provider Addon

<ItemImage id="applied_oritech:me_pattern_provider_addon" scale="4" />

The standard addon provides nine pattern slots. Encode a processing pattern matching the machine's recipe and insert it into the addon. The network can then use the machine for autocrafting. Ingredients are routed to machine input slots rather than output slots; supported fluids use the machine's fluid storage.

The addon uses AE2's pattern provider logic, including blocking mode, priority and pattern access. Enable automatic output return when the machine has no other way to send crafting results back. Returned results first enter the provider's return inventory and are then handled by AE2.

## ME Interface Addon

<ItemImage id="applied_oritech:me_interface_addon" scale="4" />

Configure the resources and quantities to stock in the interface menu. The interface obtains them from the network and transfers its stocked buffer to the attached machine. Resources that the machine cannot accept stay in the buffer. Supported machines can receive fluids as well as items.

A crafting card allows the interface to request crafting for configured resources that are unavailable in storage. A fuzzy card enables AE2's supported fuzzy matching. Automatic output return sends machine outputs back into ME storage independently of stocking.

## ExtendedAE Integration

<ItemImage id="applied_oritech:me_extended_pattern_provider_addon" scale="4" />

<ItemImage id="applied_oritech:me_extended_interface_addon" scale="4" />

When ExtendedAE is installed, the extended pattern provider addon offers 36 pattern slots. The extended interface addon offers 36 configuration slots through ExtendedAE's paged menu. Both remain Applied Oritech addons and use the same machine, dock and automatic output return behavior. ExtendedAE itself is optional.

## Automatic Output Return

Use the button immediately to the left of the priority button in an addon menu. The icon is shown in its original color when enabled and is darkened when disabled. The button background does not change color. Hover over it to read the current state.

The setting is saved per addon. Its initial value comes from the common configuration. Sneak-right-click is not a toggle. This button is only added to Applied Oritech addon menus, not to native AE2 or ExtendedAE devices.

Automatic return collects the machine's output resources, not arbitrary input contents. Transfers require an online ME network, available storage space and any machine energy required by the configuration. Disable it when another system should handle the outputs.

## Troubleshooting

- **Not attached to a machine:** check the machine's addon positions and detection, or place the addon against the machine's connected dock.
- **No dock:** install and detect an ME Dock belonging to the same machine.
- **Dock offline:** check the ME cable, network energy and channel availability.
- **Crafting does not finish:** check the processing pattern, machine inputs, machine power, output space and automatic return setting.
- **Resources do not transfer:** verify that the machine accepts the resource, that it supports the required item or fluid storage, and that both the network and machine have enough energy.
- **Outputs stay in the machine:** check the return toggle, network storage capacity and, for pattern providers, the return inventory.

Breaking an addon drops its stored contents, encoded patterns and installed cards. Remove or cancel active crafting jobs before changing a working setup.
