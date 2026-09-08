package com.java.beipuo.applied_oritech.machine;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;

import dev.architectury.fluid.FluidStack;
import rearth.oritech.api.fluid.FluidApi;

import net.minecraft.network.chat.Component;

public final class OritechFluidStorage implements MEStorage {
    private final FluidApi.FluidStorage storage;
    private final Component description;

    public OritechFluidStorage(FluidApi.FluidStorage storage, Component description) {
        this.storage = storage;
        this.description = description;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!(what instanceof AEFluidKey fluid) || amount <= 0) return 0;
        var moved = storage.insert(FluidStack.create(fluid.getFluid(), amount), mode == Actionable.SIMULATE);
        if (moved > 0 && mode == Actionable.MODULATE) storage.update();
        return moved;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!(what instanceof AEFluidKey fluid) || amount <= 0) return 0;
        var moved = storage.extract(FluidStack.create(fluid.getFluid(), amount), mode == Actionable.SIMULATE);
        if (moved > 0 && mode == Actionable.MODULATE) storage.update();
        return moved;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        for (var stack : storage.getContent()) {
            if (!stack.isEmpty()) out.add(AEFluidKey.of(stack.getFluid()), stack.getAmount());
        }
    }

    @Override
    public Component getDescription() {
        return description;
    }
}
