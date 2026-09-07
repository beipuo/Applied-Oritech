package com.java.beipuo.applied_oritech.block;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;


import com.java.beipuo.applied_oritech.blockentity.MEUpgradeBlockEntity;

/**
 * Shared block behaviour for the two upgrade modules: a server-side ticker, dropping the AE2 logic
 * contents on break, and refusing interaction until the module is actually usable.
 */
public abstract class MEUpgradeBlock extends AOGridAddonBlock {

    public static final DirectionProperty ORIENTATION = BlockStateProperties.FACING;
    private static final Map<Direction, VoxelShape> SHAPES = createShapes();

    private static Map<Direction, VoxelShape> createShapes() {
        var shapes = new EnumMap<Direction, VoxelShape>(Direction.class);
        for (var direction : Direction.values()) {
            // The four square layers match me_interface_addon, inherited by the provider model.
            shapes.put(direction, Shapes.or(
                    layer(direction, 2, 2, 4),
                    layer(direction, 3, 0, 1),
                    layer(direction, 5, 1, 2),
                    layer(direction, 5, 4, 6)).optimize());
        }
        return Map.copyOf(shapes);
    }

    private static VoxelShape layer(Direction direction, double inset, double bottom, double top) {
        double edge = 16 - inset;
        return switch (direction) {
            case UP -> Block.box(inset, bottom, inset, edge, top, edge);
            case DOWN -> Block.box(inset, 16 - top, inset, edge, 16 - bottom, edge);
            case NORTH -> Block.box(inset, inset, 16 - top, edge, edge, 16 - bottom);
            case SOUTH -> Block.box(inset, inset, bottom, edge, edge, top);
            case EAST -> Block.box(bottom, inset, inset, top, edge, edge);
            case WEST -> Block.box(16 - top, inset, inset, 16 - bottom, edge, edge);
        };
    }

    protected MEUpgradeBlock(Properties settings) {
        super(settings.noOcclusion());
        registerDefaultState(defaultBlockState().setValue(ORIENTATION, Direction.UP));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(ORIENTATION));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return SHAPES.get(state.getValue(ORIENTATION));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ORIENTATION);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // The model's base is at y=0; its top points away from the clicked surface.
        return defaultBlockState().setValue(ORIENTATION, context.getClickedFace());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(ORIENTATION, rotation.rotate(state.getValue(ORIENTATION)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(ORIENTATION)));
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
