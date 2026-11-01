package com.java.beipuo.applied_oritech.compat.extendedae;

import appeng.items.tools.quartz.QuartzCuttingKnifeItem;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.util.InteractionUtil;
import com.glodblock.github.extendedae.container.ContainerRenamer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import com.java.beipuo.applied_oritech.blockentity.MEPatternProviderAddonBlockEntity;

public final class ExtendedAERenaming {
    private ExtendedAERenaming() {
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        var player = event.getEntity();
        if (event.isCanceled() || player.isSpectator() || event.getHand() != InteractionHand.MAIN_HAND
                || InteractionUtil.isInAlternateUseMode(player)
                || !(event.getItemStack().getItem() instanceof QuartzCuttingKnifeItem)
                || !(event.getLevel().getBlockEntity(event.getPos())
                        instanceof MEPatternProviderAddonBlockEntity provider)) return;
        if (!event.getLevel().isClientSide()) {
            MenuOpener.open(ContainerRenamer.TYPE, player, MenuLocators.forBlockEntity(provider));
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
