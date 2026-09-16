package com.java.beipuo.applied_oritech.compat.extendedae;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.java.beipuo.applied_oritech.block.MEPatternProviderAddonBlock;

public class MEExtendedPatternProviderAddonBlock extends MEPatternProviderAddonBlock {
    public MEExtendedPatternProviderAddonBlock(Properties settings) {
        super(settings);
    }

    @Override
    public Class<? extends BlockEntity> getBlockEntityType() {
        return MEExtendedPatternProviderAddonBlockEntity.class;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MEExtendedPatternProviderAddonBlockEntity(pos, state);
    }
}
