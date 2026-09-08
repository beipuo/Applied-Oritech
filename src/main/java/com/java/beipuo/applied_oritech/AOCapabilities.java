package com.java.beipuo.applied_oritech;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import appeng.api.AECapabilities;
import appeng.api.storage.MEStorage;

import rearth.oritech.block.entity.MachineCoreEntity;
import rearth.oritech.block.entity.addons.AddonBlockEntity;
import rearth.oritech.util.MachineAddonController;

import com.java.beipuo.applied_oritech.blockentity.MEPatternProviderUpgradeBlockEntity;
import com.java.beipuo.applied_oritech.machine.OritechPatternInputStorage;
import com.java.beipuo.applied_oritech.machine.OritechFluidStorage;
import com.java.beipuo.applied_oritech.machine.OritechMachineMEStorage;

/**
 * Capability wiring. This is where the bridge is actually made.
 *
 * <p>Two registrations:
 *
 * <ol>
 * <li>The dock advertises {@code IN_WORLD_GRID_NODE_HOST}, which is what lets ME cables see and
 * connect to it. Nothing else in this mod exposes a node in-world.
 *
 * <li>Oritech blocks advertise {@code ME_STORAGE} — but only on the side an upgrade of ours occupies,
 * and only when that upgrade belongs to the same machine. AE2 checks this capability before falling
 * back to the platform item handler, so it is the hook that redirects pushes onto recipe input slots
 * instead of the machine's flat inventory. Gating it on our own blocks matters: registering it
 * unconditionally would silently change what every vanilla AE2 storage bus, import bus and pattern
 * provider sees when pointed at an Oritech machine, which is not ours to decide.
 * </ol>
 */
public final class AOCapabilities {

    private static final String ORITECH_NAMESPACE = "oritech";

    private AOCapabilities() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                AOContent.ME_DOCK_ENTITY.get(),
                (dock, context) -> dock);

        // Every Oritech block entity is a candidate. Which of them can actually answer is decided
        // per-lookup by resolveControllerPos, so registering broadly costs nothing but saves us
        // hard-coding a list of machine types that changes with every Oritech release.
        for (var entry : BuiltInRegistries.BLOCK_ENTITY_TYPE.entrySet()) {
            var namespace = entry.getKey().location().getNamespace();
            if (!namespace.equals(ORITECH_NAMESPACE) && !namespace.equals(Applied_oritech.MODID)) continue;
            registerMachineStorage(event, entry.getValue());
        }
    }

    public static java.util.List<BlockEntityType<?>> oritechBlockEntityTypes() {
        return BuiltInRegistries.BLOCK_ENTITY_TYPE.entrySet().stream()
                .filter(entry -> entry.getKey().location().getNamespace().equals(ORITECH_NAMESPACE))
                .map(java.util.Map.Entry::getValue).toList();
    }

    /**
     * The type parameter matters: passing a {@code BlockEntityType<?>} straight into
     * {@code registerBlockEntity} would force raw types onto the provider lambda too, and the
     * compiler then cannot infer its parameters. Naming the wildcard here lets capture conversion
     * line the two up.
     */
    private static <BE extends BlockEntity> void registerMachineStorage(RegisterCapabilitiesEvent event,
            BlockEntityType<BE> type) {
        event.registerBlockEntity(AECapabilities.ME_STORAGE, type,
                (BE blockEntity, Direction side) -> machineStorageFor(blockEntity, side));
    }

    @Nullable
    private static MEStorage machineStorageFor(BlockEntity blockEntity, @Nullable Direction side) {
        if (side == null) return null;
        var level = blockEntity.getLevel();
        var neighbourPos = blockEntity.getBlockPos().relative(side);
        if (level == null || !level.hasChunkAt(neighbourPos)
                || !(level.getBlockEntity(neighbourPos) instanceof MEPatternProviderUpgradeBlockEntity)) return null;
        var itemStorage = new OritechPatternInputStorage(() -> {
            if (blockEntity.isRemoved() || !level.hasChunkAt(neighbourPos)) return null;
            if (!(level.getBlockEntity(neighbourPos) instanceof MEPatternProviderUpgradeBlockEntity upgrade)) return null;
            var controllerPos = resolveControllerPos(blockEntity);
            var machine = upgrade.getMachine();
            return controllerPos != null && machine != null
                    && controllerPos.equals(machine.getPosForAddon()) ? upgrade : null;
        }, blockEntity.getBlockState().getBlock().getName());
        if (blockEntity instanceof com.java.beipuo.applied_oritech.blockentity.MEUpgradeBlockEntity upgrade
                && upgrade.getMachineLink() != null && upgrade.getMachineLink().hasFluids()) {
            return new OritechMachineMEStorage(itemStorage,
                    new OritechFluidStorage(upgrade.getMachineLink().fluidStorage(),
                            blockEntity.getBlockState().getBlock().getName()));
        }
        return itemStorage;
    }

    /**
     * Maps whichever Oritech block an upgrade happens to touch back to the machine controller that
     * owns it. Accepting cores and addons, not just the controller itself, is what makes upgrades
     * work on multiblock machines and at the end of an extender chain — AE2 only ever looks one
     * block away from the upgrade.
     */
    @Nullable
    private static BlockPos resolveControllerPos(BlockEntity blockEntity) {
        if (blockEntity instanceof com.java.beipuo.applied_oritech.blockentity.AOGridAddonBlockEntity addon) {
            var machine = addon.getMachine();
            return machine == null ? null : machine.getPosForAddon();
        }
        if (blockEntity instanceof MachineAddonController) {
            return blockEntity.getBlockPos();
        }
        if (blockEntity instanceof MachineCoreEntity core) {
            var controller = core.getCachedController();
            return controller instanceof BlockEntity controllerEntity ? controllerEntity.getBlockPos() : null;
        }
        if (blockEntity instanceof AddonBlockEntity addon) {
            return addon.getBlockState().getValue(rearth.oritech.block.blocks.addons.MachineAddonBlock.ADDON_USED)
                    ? addon.getControllerPos() : null;
        }
        return null;
    }
}
