package com.java.beipuo.applied_oritech.mixin;

import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.java.beipuo.applied_oritech.blockentity.MEPatternProviderAddonBlockEntity;

@Pseudo
@Mixin(targets = "com.glodblock.github.extendedae.container.ContainerRenamer", remap = false)
public abstract class ExtendedAERenamerMixin {
    @Inject(method = "setter", at = @At("HEAD"), cancellable = true)
    private static void appliedOritech$setProviderName(Object host,
            CallbackInfoReturnable<Consumer<String>> callback) {
        if (host instanceof MEPatternProviderAddonBlockEntity provider) {
            callback.setReturnValue(name -> provider.setCustomName(
                    name == null || name.isBlank() ? null : Component.literal(name)));
        }
    }
}
