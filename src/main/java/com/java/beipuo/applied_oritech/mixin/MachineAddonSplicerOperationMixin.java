package com.java.beipuo.applied_oritech.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.java.beipuo.applied_oritech.blockentity.MEAddonBlockEntity;

@Mixin(targets = "rearth.oritech.block.behavior.MachineAddonSplicerBehavior$AddonOperation")
public abstract class MachineAddonSplicerOperationMixin {
    @Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean appliedOritech$dropMEAddon(ServerLevel level, BlockPos pos, BlockState state, int flags) {
        var addon = level.getBlockEntity(pos);
        if (addon instanceof MEAddonBlockEntity) {
            Block.dropResources(level.getBlockState(pos), level, pos, addon);
        }
        return level.setBlock(pos, state, flags);
    }
}
