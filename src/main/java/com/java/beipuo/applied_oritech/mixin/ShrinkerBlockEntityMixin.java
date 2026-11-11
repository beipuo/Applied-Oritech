package com.java.beipuo.applied_oritech.mixin;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import rearth.oritech.block.entity.interaction.ShrinkerBlockEntity;

import com.java.beipuo.applied_oritech.blockentity.MEAddonBlockEntity;
import com.java.beipuo.applied_oritech.machine.AddonConsumption;

@Mixin(ShrinkerBlockEntity.class)
public abstract class ShrinkerBlockEntityMixin implements AddonConsumption {
    @Unique
    private boolean appliedOritech$consumingAddons;

    @Override
    public boolean appliedOritech$isConsumingAddons() {
        return appliedOritech$consumingAddons;
    }

    @Redirect(method = "doShrink", at = @At(value = "INVOKE", target = "Ljava/util/List;reversed()Ljava/util/List;"))
    private List<BlockPos> appliedOritech$snapshotAddons(List<BlockPos> addons) {
        return List.copyOf(addons).reversed();
    }

    @Redirect(method = "doShrink", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean appliedOritech$consumeAddon(Level level, BlockPos pos, BlockState state, int flags) {
        var wasConsuming = appliedOritech$consumingAddons;
        appliedOritech$consumingAddons = true;
        try {
            var addon = level.getBlockEntity(pos);
            if (addon instanceof MEAddonBlockEntity) {
                Block.dropResources(level.getBlockState(pos), level, pos, addon);
            }
            return level.setBlock(pos, state, flags);
        } finally {
            appliedOritech$consumingAddons = wasConsuming;
        }
    }
}
