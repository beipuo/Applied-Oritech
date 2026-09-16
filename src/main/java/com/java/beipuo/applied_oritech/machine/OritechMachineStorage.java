package com.java.beipuo.applied_oritech.machine;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

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
        var resource = itemKey.toResource();
        long inserted = 0;

        try (var transaction = Transaction.openRoot()) {
            for (var slot : link.inputSlots()) {
                if (inserted >= amount) break;
                var remaining = amount - inserted;
                var slotLimit = inventory.getCapacityAsLong(slot, resource);
                var want = (int) Math.min(remaining, Math.min(slotLimit, Integer.MAX_VALUE));
                inserted += inventory.insert(slot, resource, want, transaction);
            }
            if (mode == Actionable.MODULATE) transaction.commit();
        }
        return inserted;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!(what instanceof AEItemKey itemKey) || amount <= 0 || !link.hasOutputs()) return 0;

        var inventory = link.inventory();
        long extracted = 0;

        try (var transaction = Transaction.openRoot()) {
            for (var slot : link.outputSlots()) {
                if (extracted >= amount) break;
                var present = link.stackInSlot(slot);
                if (present.isEmpty() || !itemKey.equals(AEItemKey.of(present))) continue;
                var remaining = amount - extracted;
                var want = (int) Math.min(remaining, present.getCount());
                if (want <= 0) continue;
                extracted += inventory.extract(slot, ItemResource.of(present), want, transaction);
            }
            if (mode == Actionable.MODULATE) transaction.commit();
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

        for (var slot : link.outputSlots()) {
            var stack = link.stackInSlot(slot);
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
