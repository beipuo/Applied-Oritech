package com.java.beipuo.applied_oritech.compat.jade;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import com.java.beipuo.applied_oritech.blockentity.MEAddonBlockEntity;
import com.java.beipuo.applied_oritech.blockentity.MEInterfaceAddonBlockEntity;
import com.java.beipuo.applied_oritech.blockentity.MEPatternProviderAddonBlockEntity;

public final class AutoReturnDataProvider
        implements BodyProvider<MEAddonBlockEntity>, ServerDataProvider<MEAddonBlockEntity> {
    @Override
    public void provideServerData(Player player, MEAddonBlockEntity host, CompoundTag data) {
        if (host instanceof MEInterfaceAddonBlockEntity iface) {
            data.putBoolean("auto_return", iface.isAutoReturn());
        } else if (host instanceof MEPatternProviderAddonBlockEntity provider) {
            data.putBoolean("auto_return", provider.isAutoReturn());
        }
    }

    @Override
    public void buildTooltip(MEAddonBlockEntity host, TooltipContext context, TooltipBuilder tooltip) {
        context.serverData().getBoolean("auto_return").ifPresent(enabled -> tooltip.addLine(
                Component.translatable("message.applied_oritech.auto_return", Component.translatable(
                        enabled ? "message.applied_oritech.toggle_on" : "message.applied_oritech.toggle_off"))));
    }
}
