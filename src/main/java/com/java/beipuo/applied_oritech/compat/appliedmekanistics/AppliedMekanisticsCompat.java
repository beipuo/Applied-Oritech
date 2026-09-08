package com.java.beipuo.applied_oritech.compat.appliedmekanistics;

import me.ramidzkh.mekae2.MekCapabilities;
import me.ramidzkh.mekae2.ae2.stack.ChemicalHandlerFacade;

import appeng.api.AECapabilities;
import appeng.api.storage.MEStorage;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import com.java.beipuo.applied_oritech.AOCapabilities;

public final class AppliedMekanisticsCompat {
    private AppliedMekanisticsCompat() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        for (var entry : AOCapabilities.oritechBlockEntityTypes()) {
            registerType(event, entry);
        }
    }

    private static <BE extends BlockEntity> void registerType(RegisterCapabilitiesEvent event,
            net.minecraft.world.level.block.entity.BlockEntityType<BE> type) {
        event.registerBlockEntity(AECapabilities.ME_STORAGE, type,
                (BE blockEntity, Direction side) -> {
                    var level = blockEntity.getLevel();
                    if (level == null) return null;
                    var handler = level.getCapability(MekCapabilities.CHEMICAL.block(),
                            blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity, side);
                    return handler == null ? null : new ChemicalHandlerFacade(handler, false, blockEntity::setChanged);
                });
    }
}
