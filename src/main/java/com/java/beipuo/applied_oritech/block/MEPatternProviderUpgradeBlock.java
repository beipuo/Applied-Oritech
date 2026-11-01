package com.java.beipuo.applied_oritech.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.java.beipuo.applied_oritech.blockentity.MEPatternProviderUpgradeBlockEntity;

public class MEPatternProviderUpgradeBlock extends MEPatternProviderAddonBlock {
    public MEPatternProviderUpgradeBlock(Properties settings) {
        super(settings);
    }

    @Override
    public Class<? extends BlockEntity> getBlockEntityType() {
        return MEPatternProviderUpgradeBlockEntity.class;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MEPatternProviderUpgradeBlockEntity(pos, state);
    }
}
