package com.java.beipuo.applied_oritech.compat.extendedae;

import appeng.helpers.externalstorage.GenericStackInv;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;

import com.glodblock.github.extendedae.api.caps.IGenericInvHost;
import com.glodblock.github.extendedae.container.ContainerExPatternProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import com.java.beipuo.applied_oritech.blockentity.MEPatternProviderUpgradeBlockEntity;

public class MEExtendedPatternProviderAddonBlockEntity extends MEPatternProviderUpgradeBlockEntity
        implements IGenericInvHost {
    public MEExtendedPatternProviderAddonBlockEntity(BlockPos pos, BlockState state) {
        super(ExtendedAEContent.PATTERN_PROVIDER_ENTITY.get(), pos, state, 36);
    }

    @Override
    public void openMenu(Player player, MenuHostLocator locator) {
        MenuOpener.open(ContainerExPatternProvider.TYPE, player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(ContainerExPatternProvider.TYPE, player, subMenu.getLocator());
    }

    @Override
    public GenericStackInv getGenericInv() {
        return getLogic().getReturnInv();
    }
}
