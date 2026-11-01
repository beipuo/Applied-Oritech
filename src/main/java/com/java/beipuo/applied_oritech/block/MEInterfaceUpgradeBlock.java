package com.java.beipuo.applied_oritech.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.java.beipuo.applied_oritech.blockentity.MEInterfaceUpgradeBlockEntity;

public class MEInterfaceUpgradeBlock extends MEInterfaceAddonBlock {
    public MEInterfaceUpgradeBlock(Properties settings) {
        super(settings);
    }

    @Override
    public Class<? extends BlockEntity> getBlockEntityType() {
        return MEInterfaceUpgradeBlockEntity.class;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MEInterfaceUpgradeBlockEntity(pos, state);
    }
}
