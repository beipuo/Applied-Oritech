package com.java.beipuo.applied_oritech.block;

import java.util.ArrayList;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;


import com.java.beipuo.applied_oritech.blockentity.MEUpgradeBlockEntity;

/**
 * Shared block behaviour for the two upgrade modules: a server-side ticker, dropping the AE2 logic
 * contents on break, and refusing interaction until the module is actually usable.
 */
public abstract class MEUpgradeBlock extends AOGridAddonBlock {

    protected MEUpgradeBlock(Properties settings) {
        super(settings);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return (tickLevel, pos, tickState, blockEntity) -> {
            if (blockEntity instanceof MEUpgradeBlockEntity upgrade) upgrade.tickServer();
        };
    }

    /**
     * Encoded patterns, stocked items and upgrade cards live inside AE2 logic objects, not a vanilla
     * container, so a loot table cannot reach them.
     *
     * <p>Done here rather than in {@code playerWillDestroy} so that explosions and any other
     * non-player removal drop the contents too. The block entity is still present at this point —
     * vanilla containers rely on the same ordering.
     */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState,
            boolean movedByPiston) {
        if (!level.isClientSide() && !state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof MEUpgradeBlockEntity upgrade) {
            var drops = new ArrayList<ItemStack>();
            upgrade.addAdditionalDrops(drops);
            for (var drop : drops) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
            }
            upgrade.clearLogicContent();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof MEUpgradeBlockEntity upgrade)) return InteractionResult.PASS;

        if (!upgrade.isAttachedToMachine()) {
            player.displayClientMessage(
                    Component.translatable("message.applied_oritech.not_attached"), true);
            return InteractionResult.CONSUME;
        }
        if (upgrade.getDock() == null) {
            player.displayClientMessage(
                    Component.translatable("message.applied_oritech.no_dock"), true);
            return InteractionResult.CONSUME;
        }

        return onUpgradeUsed(upgrade, player);
    }

    /** Called only once the module is attached to a machine and has found a dock. */
    protected abstract InteractionResult onUpgradeUsed(MEUpgradeBlockEntity upgrade, Player player);
}
