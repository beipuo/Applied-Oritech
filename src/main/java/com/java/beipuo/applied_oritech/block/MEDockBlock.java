package com.java.beipuo.applied_oritech.block;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.chat.Component;

import com.java.beipuo.applied_oritech.blockentity.MEDockBlockEntity;

/**
 * The ME Dock block.
 *
 * <p>Registered as a full cube ({@code needsSupport = false}) rather than a face-attached panel
 * like most Oritech addons: it is the block ME cables attach to, and a full cube gives them all six
 * faces to connect from.
 */
public class MEDockBlock extends AOGridAddonBlock {

    public MEDockBlock(Properties settings) {
        super(settings);
    }

    /** Oritech consults this reflectively; overriding {@link #newBlockEntity} makes it unused. */
    @Override
    public @NotNull Class<? extends BlockEntity> getBlockEntityType() {
        return MEDockBlockEntity.class;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MEDockBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MEDockBlockEntity dock) {
            var message = dock.getMachine() == null ? "not_attached"
                    : dock.isOnline() ? "dock_online" : "dock_offline";
            player.displayClientMessage(Component.translatable("message.applied_oritech." + message,
                    dock.findGroupUpgrades().size()), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
