package com.java.beipuo.applied_oritech.block;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import appeng.menu.locator.MenuLocators;

import com.java.beipuo.applied_oritech.blockentity.MEPatternProviderAddonBlockEntity;
import com.java.beipuo.applied_oritech.blockentity.MEAddonBlockEntity;

/**
 * The ME Pattern Provider Upgrade block.
 *
 * <p>Right-click opens AE2's own pattern provider menu. Sneak-right-click with an empty hand toggles
 * the output sweep — the switch that decides whether this module pulls the machine's results back
 * into the network, which autocrafting needs because Oritech machines never push results out.
 */
public class MEPatternProviderAddonBlock extends MEAddonBlock {

    public MEPatternProviderAddonBlock(Properties settings) {
        super(settings);
    }

    @Override
    public @NotNull Class<? extends BlockEntity> getBlockEntityType() {
        return MEPatternProviderAddonBlockEntity.class;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MEPatternProviderAddonBlockEntity(pos, state);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
            BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MEPatternProviderAddonBlockEntity provider) {
            provider.getLogic().updateRedstoneState();
        }
    }

    @Override
    protected InteractionResult onUpgradeUsed(MEAddonBlockEntity upgrade, Player player) {
        if (!(upgrade instanceof MEPatternProviderAddonBlockEntity provider)) {
            return InteractionResult.PASS;
        }

        if (player.isSecondaryUseActive()) {
            var enabled = provider.toggleAutoReturn();
            var stateLabel = Component.translatable(
                    enabled ? "message.applied_oritech.toggle_on" : "message.applied_oritech.toggle_off");
            player.displayClientMessage(
                    Component.translatable("message.applied_oritech.auto_return", stateLabel)
                            .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.YELLOW),
                    true);
            return InteractionResult.CONSUME;
        }

        provider.openMenu(player, MenuLocators.forBlockEntity(provider));
        return InteractionResult.CONSUME;
    }
}
