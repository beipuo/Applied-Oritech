package com.java.beipuo.applied_oritech.machine;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import rearth.oritech.api.item.ItemApi;
import rearth.oritech.api.fluid.FluidApi;
import rearth.oritech.block.entity.MachineCoreEntity;
import rearth.oritech.block.entity.addons.AddonBlockEntity;
import rearth.oritech.util.MachineAddonController;
import rearth.oritech.util.ScreenProvider;

/**
 * A resolved view of the Oritech machine an addon is attached to.
 *
 * <p>Oritech exposes a machine's whole inventory as one flat {@link ItemApi.InventoryStorage},
 * with no notion of which slots are recipe inputs and which hold results. That distinction
 * only lives in the machine's {@link ScreenProvider}: {@link ScreenProvider#getGuiSlots()}
 * returns one {@link ScreenProvider.GuiSlot} per slot, each carrying an {@code output} flag
 * and the slot's real index into that same storage.
 *
 * <p>Splitting the slots this way is what lets us insert only into inputs and pull only from
 * outputs. Handing AE2 the raw storage instead is exactly what makes a stock pattern provider
 * shove ingredients into a machine's result slot.
 */
public record OritechMachineLink(MachineAddonController machine,
                                 ItemApi.InventoryStorage inventory,
                                 FluidApi.FluidStorage fluidStorage,
                                 int[] inputSlots,
                                 int[] outputSlots) {

    /**
     * Resolves the machine at {@code controllerPos}, or returns null when it is missing,
     * not an addon controller, or does not expose an inventory.
     */
    @Nullable
    public static OritechMachineLink resolve(@Nullable Level level, @Nullable BlockPos controllerPos) {
        if (level == null || controllerPos == null || !level.hasChunkAt(controllerPos)) return null;

        var controller = level.getBlockEntity(controllerPos);
        if (!(controller instanceof MachineAddonController machine)) return null;

        var inventory = inventoryOf(machine);
        if (inventory == null) return null;

        var slots = slotsOf(machine);
        if (slots == null) return null;

        var inputs = new ArrayList<Integer>();
        var outputs = new ArrayList<Integer>();
        var slotCount = inventory.getSlotCount();
        for (var slot : slots) {
            // Guard against a GUI describing slots the storage does not actually have.
            if (slot.index() < 0 || slot.index() >= slotCount) continue;
            (slot.output() ? outputs : inputs).add(slot.index());
        }

        var fluids = machine instanceof FluidApi.BlockProvider provider
                ? provider.getFluidStorage(null) : null;
        return new OritechMachineLink(machine, inventory, fluids, toIntArray(inputs), toIntArray(outputs));
    }

    /**
     * Prefers the same path Oritech's own inventory proxy addon uses
     * ({@code ItemApi.BlockProvider#getInventoryStorage}) so we see exactly the storage the
     * GUI slot indices refer to, and falls back to the addon API for controllers that only
     * implement that.
     */
    @Nullable
    private static ItemApi.InventoryStorage inventoryOf(MachineAddonController machine) {
        if (machine instanceof ItemApi.BlockProvider provider) {
            var storage = provider.getInventoryStorage(null);
            if (storage != null) return storage;
        }
        return machine.getInventoryForAddon();
    }

    @Nullable
    private static List<ScreenProvider.GuiSlot> slotsOf(MachineAddonController machine) {
        var screen = machine.getScreenProvider();
        if (screen == null) return null;
        try {
            return screen.getGuiSlots();
        } catch (RuntimeException e) {
            // Some machines build their slot list from state that is only valid once the
            // multiblock is assembled. Treat that as "not ready" rather than crashing the tick.
            return null;
        }
    }

    private static int[] toIntArray(List<Integer> values) {
        var result = new int[values.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = values.get(i);
        }
        return result;
    }

    public boolean hasInputs() {
        return inputSlots.length > 0;
    }

    public boolean hasOutputs() {
        return outputSlots.length > 0;
    }

    public boolean hasFluids() {
        return fluidStorage != null;
    }

    /**
     * The sides of {@code self} that touch the machine this link points at — its controller
     * block, one of its multiblock cores, or another addon claimed by the same controller.
     *
     * <p>AE2's pattern provider only ever looks one block away
     * ({@code PatternProviderLogic#findAdapter} builds its target cache at
     * {@code getBlockPos().relative(side)}), so an upgrade has to be told which of its six
     * neighbours belongs to the machine. Accepting neighbouring addons as well as the machine
     * itself is what makes upgrades work when they sit at the end of an extender chain.
     */
    public static EnumSet<Direction> sidesFacingMachine(@Nullable Level level, BlockPos self,
            @Nullable BlockPos controllerPos) {
        var sides = EnumSet.noneOf(Direction.class);
        if (level == null || controllerPos == null) return sides;

        var controller = level.getBlockEntity(controllerPos);
        if (controller == null) return sides;

        for (var direction : Direction.values()) {
            var neighbourPos = self.relative(direction);
            if (!level.hasChunkAt(neighbourPos)) continue;
            if (neighbourPos.equals(controllerPos)) {
                sides.add(direction);
                continue;
            }

            var neighbour = level.getBlockEntity(neighbourPos);
            if (neighbour == null) continue;

            if (neighbour instanceof MachineCoreEntity core) {
                if (core.getCachedController() == controller) sides.add(direction);
            } else if (neighbour instanceof AddonBlockEntity addon) {
                if (addon.getBlockState().getValue(rearth.oritech.block.blocks.addons.MachineAddonBlock.ADDON_USED)
                        && addon.getControllerPos().equals(controllerPos)) sides.add(direction);
            }
        }

        return sides;
    }
}
