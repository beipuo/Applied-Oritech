package com.java.beipuo.applied_oritech.machine;

import java.util.List;
import java.util.function.BooleanSupplier;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;

public final class OritechOutputTransfer {
    private OritechOutputTransfer() {
    }

    @FunctionalInterface
    public interface Insert {
        long insert(AEKey key, long amount, Actionable mode);
    }

    public static void sweep(OritechMachineLink link, Insert target, BooleanSupplier hasEnergy, Runnable spendEnergy) {
        var inventory = link.inventory();
        for (var slot : link.outputSlots()) {
            var stack = inventory.getStackInSlot(slot).copy();
            if (stack.isEmpty()) continue;
            var key = AEItemKey.of(stack);
            var accepted = target.insert(key, stack.getCount(), Actionable.SIMULATE);
            if (accepted <= 0) continue;
            if (!hasEnergy.getAsBoolean()) return;
            var taken = inventory.extractFromSlot(key.toStack((int) accepted), slot, false);
            if (taken <= 0) continue;
            var stored = target.insert(key, taken, Actionable.MODULATE);
            if (stored < taken) {
                inventory.setStackInSlot(slot, stack.copyWithCount(stack.getCount() - (int) stored));
            }
            inventory.update();
            if (stored > 0) spendEnergy.run();
        }
        var fluids = link.fluidStorage();
        if (fluids == null || !fluids.supportsExtraction()) return;
        for (var stack : List.copyOf(fluids.getContent())) {
            if (stack.isEmpty()) continue;
            var key = AEFluidKey.of(stack.getFluid());
            var accepted = target.insert(key, stack.getAmount(), Actionable.SIMULATE);
            if (accepted <= 0) continue;
            if (!hasEnergy.getAsBoolean()) return;
            var request = dev.architectury.fluid.FluidStack.create(stack.getFluid(), accepted);
            var taken = fluids.extract(request, false);
            if (taken <= 0) continue;
            var stored = target.insert(key, taken, Actionable.MODULATE);
            if (stored < taken) {
                fluids.insert(dev.architectury.fluid.FluidStack.create(stack.getFluid(), taken - stored), false);
            }
            fluids.update();
            if (stored > 0) spendEnergy.run();
        }
    }
}
