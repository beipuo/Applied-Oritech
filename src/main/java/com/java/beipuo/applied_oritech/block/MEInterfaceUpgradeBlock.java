package com.java.beipuo.applied_oritech.block;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import appeng.menu.locator.MenuLocators;

import com.java.beipuo.applied_oritech.blockentity.MEInterfaceUpgradeBlockEntity;
import com.java.beipuo.applied_oritech.blockentity.MEUpgradeBlockEntity;

/**
 * The ME Interface Upgrade block. Right-click opens AE2's own interface menu, where the stock list
 * and upgrade cards are configured.
 */
public class MEInterfaceUpgradeBlock extends MEUpgradeBlock {

    public MEInterfaceUpgradeBlock(Properties settings) {
        super(settings);
    }

    @Override
    public @NotNull Class<? extends BlockEntity> getBlockEntityType() {
        return MEInterfaceUpgradeBlockEntity.class;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MEInterfaceUpgradeBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult onUpgradeUsed(MEUpgradeBlockEntity upgrade, Player player) {
        if (!(upgrade instanceof MEInterfaceUpgradeBlockEntity iface)) return InteractionResult.PASS;

        iface.openMenu(player, MenuLocators.forBlockEntity(iface));
        return InteractionResult.CONSUME;
    }
}
