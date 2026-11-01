package com.java.beipuo.applied_oritech.block;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import appeng.menu.locator.MenuLocators;

import com.java.beipuo.applied_oritech.blockentity.MEInterfaceAddonBlockEntity;
import com.java.beipuo.applied_oritech.blockentity.MEAddonBlockEntity;

/**
 * The ME Interface Upgrade block. Right-click opens AE2's own interface menu, where the stock list
 * and upgrade cards are configured.
 */
public class MEInterfaceAddonBlock extends MEAddonBlock {

    public MEInterfaceAddonBlock(Properties settings) {
        super(settings);
    }

    @Override
    public @NotNull Class<? extends BlockEntity> getBlockEntityType() {
        return MEInterfaceAddonBlockEntity.class;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MEInterfaceAddonBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult onUpgradeUsed(MEAddonBlockEntity upgrade, Player player) {
        if (!(upgrade instanceof MEInterfaceAddonBlockEntity iface)) return InteractionResult.PASS;

        if (player.isShiftKeyDown()) {
            var enabled = iface.toggleAutoReturn();
            var label = net.minecraft.network.chat.Component.translatable(
                    enabled ? "message.applied_oritech.toggle_on" : "message.applied_oritech.toggle_off");
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    "message.applied_oritech.auto_return", label), true);
            return InteractionResult.CONSUME;
        }
        iface.openMenu(player, MenuLocators.forBlockEntity(iface));
        return InteractionResult.CONSUME;
    }
}
