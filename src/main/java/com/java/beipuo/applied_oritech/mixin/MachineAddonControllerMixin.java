package com.java.beipuo.applied_oritech.mixin;

import java.util.List;

import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rearth.oritech.block.entity.addons.CombiAddonEntity;
import rearth.oritech.util.MachineAddonController;

import com.java.beipuo.applied_oritech.blockentity.AOGridAddonBlockEntity;
import com.java.beipuo.applied_oritech.machine.OritechAddonScan;

@Mixin(MachineAddonController.class)
public interface MachineAddonControllerMixin {
    @Inject(method = "getAllAddons", at = @At("RETURN"), cancellable = true)
    private void appliedOritech$includeMEAddons(BlockPos brokenAddon,
            CallbackInfoReturnable<List<MachineAddonController.AddonBlock>> callback) {
        var result = OritechAddonScan.withHeart((MachineAddonController) this, brokenAddon);
        if (result != null) callback.setReturnValue(result);
    }

    @Inject(method = "gatherAddonStats", at = @At("HEAD"), cancellable = true)
    private void appliedOritech$preserveHeartStats(List<MachineAddonController.AddonBlock> addons,
            CallbackInfo callback) {
        if (addons.stream().noneMatch(addon -> addon.addonEntity() instanceof CombiAddonEntity)
                || addons.stream().noneMatch(addon -> addon.addonEntity() instanceof AOGridAddonBlockEntity)) return;
        var statsAddons = addons.stream()
                .filter(addon -> addon.addonEntity() instanceof CombiAddonEntity)
                .limit(1).toList();
        ((MachineAddonController) this).gatherAddonStats(statsAddons);
        callback.cancel();
    }
}
