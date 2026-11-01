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
import net.minecraft.world.level.redstone.Orientation;
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
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            @Nullable net.minecraft.world.entity.LivingEntity placer, net.minecraft.world.item.ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()
                && level.getBlockEntity(pos) instanceof MEPatternProviderAddonBlockEntity provider) {
            provider.setCustomName(stack.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME));
        }
    }

    @Override
    protected java.util.List<net.minecraft.world.item.ItemStack> getDrops(BlockState state,
            net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        var drops = super.getDrops(state, builder);
        if (builder.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY)
                instanceof MEPatternProviderAddonBlockEntity provider && provider.hasCustomName()) {
            for (var stack : drops) {
                if (stack.is(asItem())) {
                    stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, provider.getCustomName());
                }
            }
        }
        return drops;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
            @Nullable Orientation orientation, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, orientation, isMoving);
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
            player.sendOverlayMessage(
                    Component.translatable("message.applied_oritech.auto_return", stateLabel)
                            .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
            return InteractionResult.CONSUME;
        }

        provider.openMenu(player, MenuLocators.forBlockEntity(provider));
        return InteractionResult.CONSUME;
    }
}
