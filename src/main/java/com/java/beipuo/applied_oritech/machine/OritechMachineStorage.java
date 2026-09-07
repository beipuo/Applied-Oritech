package com.java.beipuo.applied_oritech.machine;

import net.minecraft.network.chat.Component;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;

/**
 * Presents an Oritech machine to AE2 as an {@link MEStorage} that inserts only into recipe
 * input slots and extracts only from result slots.
 *
 * <p>This is the whole reason the addon exists. Oritech already exposes machines through
 * NeoForge's item handler capability, but as one flat inventory — so a stock AE2 pattern
 * provider happily pushes ingredients into a machine's output slot, where they jam. AE2 checks
 * {@code AECapabilities.ME_STORAGE} before falling back to the platform item handler
 * ({@code PatternProviderTarget#get}), so registering this storage for the sides where one of
 * our upgrades is attached redirects AE2 onto the correct slots without touching how any other
 * mod sees the machine.
 *
 * <p>Only items are handled. Oritech's fluid slots are a separate API and are left alone.
 */
public class OritechMachineStorage implements MEStorage {

    private final OritechMachineLink link;
    private final Component description;

    public OritechMachineStorage(OritechMachineLink link, Component description) {
        this.link = link;
        this.description = description;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!(what instanceof AEItemKey itemKey) || amount <= 0 || !link.hasInputs()) return 0;

        var inventory = link.inventory();
        long inserted = 0;

        for (var slot : link.inputSlots()) {
            if (inserted >= amount) break;

            var remaining = amount - inserted;
            // A slot limit is the ceiling for one slot; ItemStack counts are ints, so clamp
            // before building the probe stack.
            var slotLimit = Math.max(1, inventory.getSlotLimit(slot));
            var want = (int) Math.min(remaining, Math.min(slotLimit, Integer.MAX_VALUE));

            var moved = inventory.insertToSlot(itemKey.toStack(want), slot, mode == Actionable.SIMULATE);
            inserted += moved;
        }

        if (inserted > 0 && mode == Actionable.MODULATE) {
            inventory.update();
        }
        return inserted;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!(what instanceof AEItemKey itemKey) || amount <= 0 || !link.hasOutputs()) return 0;

        var inventory = link.inventory();
        long extracted = 0;

        for (var slot : link.outputSlots()) {
            if (extracted >= amount) break;

            var present = inventory.getStackInSlot(slot);
            if (present.isEmpty() || !itemKey.equals(AEItemKey.of(present))) continue;

            var remaining = amount - extracted;
            var want = (int) Math.min(remaining, present.getCount());
            if (want <= 0) continue;

            var moved = inventory.extractFromSlot(itemKey.toStack(want), slot, mode == Actionable.SIMULATE);
            extracted += moved;
        }

        if (extracted > 0 && mode == Actionable.MODULATE) {
            inventory.update();
        }
        return extracted;
    }

    /**
     * Reports only the result slots. Inputs are deliberately invisible: listing ingredients the
     * machine is about to consume would let the network yank them back out mid-recipe.
     */
    @Override
    public void getAvailableStacks(KeyCounter out) {
        if (!link.hasOutputs()) return;

        var inventory = link.inventory();
        for (var slot : link.outputSlots()) {
            var stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            var key = AEItemKey.of(stack);
            if (key != null) out.add(key, stack.getCount());
        }
    }

    @Override
    public Component getDescription() {
        return description;
    }

    public OritechMachineLink link() {
        return link;
    }
}
