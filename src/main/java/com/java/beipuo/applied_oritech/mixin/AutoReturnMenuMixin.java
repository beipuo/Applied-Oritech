package com.java.beipuo.applied_oritech.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.menu.AEBaseMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;

import com.java.beipuo.applied_oritech.blockentity.MEInterfaceAddonBlockEntity;
import com.java.beipuo.applied_oritech.blockentity.MEPatternProviderAddonBlockEntity;
import com.java.beipuo.applied_oritech.menu.AutoReturnMenu;

@Mixin(AEBaseMenu.class)
public abstract class AutoReturnMenuMixin extends AbstractContainerMenu implements AutoReturnMenu {
    @Unique
    private boolean appliedOritech$autoReturn;

    protected AutoReturnMenuMixin(MenuType<?> type, int id) {
        super(type, id);
    }

    @Shadow
    public abstract Object getTarget();

    @Shadow
    public abstract boolean isClientSide();

    @Shadow
    protected abstract void registerClientAction(String name, Runnable callback);

    @Shadow
    protected abstract void sendClientAction(String name);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void appliedOritech$setup(CallbackInfo ci) {
        if (!appliedOritech$hasAutoReturn()) return;
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                if (isClientSide()) return appliedOritech$autoReturn ? 1 : 0;
                return appliedOritech$readAutoReturn() ? 1 : 0;
            }

            @Override
            public void set(int value) {
                appliedOritech$autoReturn = value != 0;
            }
        });
        registerClientAction("applied_oritech:toggle_auto_return", () -> {
            if (getTarget() instanceof MEInterfaceAddonBlockEntity addon) addon.toggleAutoReturn();
            else if (getTarget() instanceof MEPatternProviderAddonBlockEntity addon) addon.toggleAutoReturn();
        });
    }

    @Override
    public boolean appliedOritech$hasAutoReturn() {
        return getTarget() instanceof MEInterfaceAddonBlockEntity
                || getTarget() instanceof MEPatternProviderAddonBlockEntity;
    }

    @Unique
    private boolean appliedOritech$readAutoReturn() {
        if (getTarget() instanceof MEInterfaceAddonBlockEntity addon) return addon.isAutoReturn();
        return getTarget() instanceof MEPatternProviderAddonBlockEntity addon && addon.isAutoReturn();
    }

    @Override
    public boolean appliedOritech$isAutoReturn() {
        return isClientSide() ? appliedOritech$autoReturn : appliedOritech$readAutoReturn();
    }

    @Override
    public void appliedOritech$toggleAutoReturn() {
        if (appliedOritech$hasAutoReturn() && isClientSide()) {
            sendClientAction("applied_oritech:toggle_auto_return");
        }
    }
}
