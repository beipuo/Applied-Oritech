package com.java.beipuo.applied_oritech.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.client.gui.AEBaseScreen;
import appeng.menu.AEBaseMenu;
import appeng.menu.implementations.InterfaceMenu;
import appeng.menu.implementations.PatternProviderMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import com.java.beipuo.applied_oritech.client.AutoReturnButton;
import com.java.beipuo.applied_oritech.menu.AutoReturnMenu;

@Mixin(AEBaseScreen.class)
public abstract class AutoReturnScreenMixin extends AbstractContainerScreen<AEBaseMenu> {
    protected AutoReturnScreenMixin(AEBaseMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void appliedOritech$addAutoReturnButton(CallbackInfo ci) {
        if (!(menu instanceof InterfaceMenu || menu instanceof PatternProviderMenu)) return;
        var autoReturn = (AutoReturnMenu) menu;
        if (!autoReturn.appliedOritech$hasAutoReturn()) return;
        var screen = (AEBaseScreen<?>) (Object) this;
        var priority = screen.getStyle().getWidget("openPriority");
        var position = priority.resolve(new net.minecraft.client.renderer.Rect2i(
                leftPos, topPos, imageWidth, imageHeight));
        addRenderableWidget(new AutoReturnButton(position.getX() - priority.getWidth() - 2,
                position.getY(), priority.getWidth(), priority.getHeight(), autoReturn));
    }
}
