package com.java.beipuo.applied_oritech.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class MEInterfaceUpgradeBlockEntity extends MEInterfaceAddonBlockEntity {
    public MEInterfaceUpgradeBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    protected MEInterfaceUpgradeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int slots) {
        super(type, pos, state, slots);
    }
}
