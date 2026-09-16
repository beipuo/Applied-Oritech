package com.java.beipuo.applied_oritech.machine;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import net.minecraft.network.chat.Component;

public final class OritechFluidStorage implements MEStorage {
    private final ResourceHandler<FluidResource> storage;
    private final Component description;

    public OritechFluidStorage(ResourceHandler<FluidResource> storage, Component description) {
        this.storage = storage;
        this.description = description;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!(what instanceof AEFluidKey fluid) || amount <= 0) return 0;
        try (var transaction = Transaction.openRoot()) {
            var moved = storage.insert(FluidResource.of(fluid.toStack(1)),
                    (int) Math.min(amount, Integer.MAX_VALUE), transaction);
            if (mode == Actionable.MODULATE) transaction.commit();
            return moved;
        }
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!(what instanceof AEFluidKey fluid) || amount <= 0) return 0;
        try (var transaction = Transaction.openRoot()) {
            var moved = storage.extract(FluidResource.of(fluid.toStack(1)),
                    (int) Math.min(amount, Integer.MAX_VALUE), transaction);
            if (mode == Actionable.MODULATE) transaction.commit();
            return moved;
        }
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        for (int slot = 0; slot < storage.size(); slot++) {
            var resource = storage.getResource(slot);
            if (!resource.isEmpty()) out.add(AEFluidKey.of(resource.toStack(1)), storage.getAmountAsLong(slot));
        }
    }

    @Override
    public Component getDescription() {
        return description;
    }
}
