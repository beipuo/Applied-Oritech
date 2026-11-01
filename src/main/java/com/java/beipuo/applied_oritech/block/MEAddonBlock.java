package com.java.beipuo.applied_oritech.block;

import java.util.EnumMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;


import com.java.beipuo.applied_oritech.blockentity.MEAddonBlockEntity;

/**
 * Shared block behaviour for the two upgrade modules: a server-side ticker, dropping the AE2 logic
 * contents on break, and refusing interaction until the module is actually usable.
 */
public abstract class MEAddonBlock extends AOGridAddonBlock {

    public static final EnumProperty<Direction> ORIENTATION = BlockStateProperties.FACING;
    private static final Map<Direction, VoxelShape> SHAPES = createShapes();

    private static Map<Direction, VoxelShape> createShapes() {
        var shapes = new EnumMap<Direction, VoxelShape>(Direction.class);
        for (var direction : Direction.values()) {
            shapes.put(direction, Shapes.or(
                    orientedBox(direction, 4, 0, 4, 12, 1, 12),
                    orientedBox(direction, 4, 1, 6, 6, 4, 10),
                    orientedBox(direction, 10, 1, 6, 12, 3, 10),
                    orientedBox(direction, 2, 3, 2, 14, 5, 14),
                    orientedBox(direction, 3, 5, 3, 13, 6, 13),
                    orientedBox(direction, 1, 3, 4, 3, 8, 12),
                    orientedBox(direction, 13, 3, 4, 15, 8, 12),
                    orientedBox(direction, 5, 3, 1, 11, 6, 3),
                    orientedBox(direction, 5, 3, 13, 11, 6, 15)).optimize());
        }
        return Map.copyOf(shapes);
    }

    private static VoxelShape orientedBox(Direction direction, double x1, double y1, double z1,
            double x2, double y2, double z2) {
        return switch (direction) {
            case UP -> Block.box(x1, y1, z1, x2, y2, z2);
            case DOWN -> Block.box(x1, 16 - y2, 16 - z2, x2, 16 - y1, 16 - z1);
            case NORTH -> Block.box(x1, z1, 16 - y2, x2, z2, 16 - y1);
            case SOUTH -> Block.box(16 - x2, z1, y1, 16 - x1, z2, y2);
            case EAST -> Block.box(y1, z1, x1, y2, z2, x2);
            case WEST -> Block.box(16 - y2, z1, 16 - x2, 16 - y1, z2, 16 - x1);
        };
    }

    protected MEAddonBlock(Properties settings) {
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
        var state = defaultBlockState().setValue(ORIENTATION, context.getClickedFace());
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    public boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) {
        return canAttach(level, pos, state.getValue(ORIENTATION).getOpposite());
    }

    @Override
    protected BlockState updateShape(BlockState state, net.minecraft.world.level.LevelReader level,
            net.minecraft.world.level.ScheduledTickAccess ticks, BlockPos pos, Direction direction,
            BlockPos neighborPos, BlockState neighborState, net.minecraft.util.RandomSource random) {
        if (direction == state.getValue(ORIENTATION).getOpposite() && !state.canSurvive(level, pos)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return state;
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
            if (blockEntity instanceof MEAddonBlockEntity upgrade) upgrade.tickServer();
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof MEAddonBlockEntity upgrade)) return InteractionResult.PASS;

        if (!upgrade.isAttachedToMachine()) {
            player.sendOverlayMessage(
                    Component.translatable("message.applied_oritech.not_attached"));
            return InteractionResult.CONSUME;
        }
        if (upgrade.getDock() == null) {
            player.sendOverlayMessage(
                    Component.translatable("message.applied_oritech.no_dock"));
            return InteractionResult.CONSUME;
        }

        return onUpgradeUsed(upgrade, player);
    }

    /** Called only once the module is attached to a machine and has found a dock. */
    protected abstract InteractionResult onUpgradeUsed(MEAddonBlockEntity upgrade, Player player);
}
