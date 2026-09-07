package com.java.beipuo.applied_oritech.compat.extendedae;

import appeng.api.storage.MEStorage;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;

import com.glodblock.github.extendedae.api.IPage;
import com.glodblock.github.extendedae.api.caps.IGenericInvHost;
import com.glodblock.github.extendedae.api.caps.IMEStorageAccess;
import com.glodblock.github.extendedae.container.ContainerExInterface;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import com.java.beipuo.applied_oritech.blockentity.MEInterfaceUpgradeBlockEntity;

public class MEExtendedInterfaceAddonBlockEntity extends MEInterfaceUpgradeBlockEntity
        implements IPage, IGenericInvHost, IMEStorageAccess {
    private int page;

    public MEExtendedInterfaceAddonBlockEntity(BlockPos pos, BlockState state) {
        super(ExtendedAEContent.INTERFACE_ENTITY.get(), pos, state, 36);
    }

    @Override
    public void openMenu(Player player, MenuHostLocator locator) {
        MenuOpener.open(ContainerExInterface.TYPE, player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(ContainerExInterface.TYPE, player, subMenu.getLocator());
    }

    @Override
    public void setPage(int page) {
        this.page = Math.clamp(page, 0, 1);
    }

    @Override
    public int getPage() {
        return page;
    }

    @Override
    public GenericStackInv getGenericInv() {
        return getInterfaceLogic().getStorage();
    }

    @Override
    public MEStorage getMEStorage() {
        return getInterfaceLogic().getInventory();
    }
}
