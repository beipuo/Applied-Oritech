package com.java.beipuo.applied_oritech.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import rearth.oritech.api.splicer.SplicerOperation;
import rearth.oritech.block.entity.interaction.AddonSplicerBlockEntity;

import com.java.beipuo.applied_oritech.machine.AddonConsumption;

@Mixin(AddonSplicerBlockEntity.class)
public abstract class AddonSplicerBlockEntityMixin implements AddonConsumption {
    @Unique
    private boolean appliedOritech$consumingAddons;

    @Override
    public boolean appliedOritech$isConsumingAddons() {
        return appliedOritech$consumingAddons;
    }

    @Redirect(method = "doShrink", at = @At(value = "INVOKE", target = "Lrearth/oritech/api/splicer/SplicerOperation;execute(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack appliedOritech$consumeAddons(SplicerOperation operation, ServerLevel level) {
        var wasConsuming = appliedOritech$consumingAddons;
        appliedOritech$consumingAddons = true;
        try {
            return operation.execute(level);
        } finally {
            appliedOritech$consumingAddons = wasConsuming;
        }
    }
}
