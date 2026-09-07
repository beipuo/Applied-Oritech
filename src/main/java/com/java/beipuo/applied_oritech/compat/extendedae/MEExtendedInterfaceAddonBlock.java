package com.java.beipuo.applied_oritech.compat.extendedae;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.java.beipuo.applied_oritech.block.MEInterfaceUpgradeBlock;

public class MEExtendedInterfaceAddonBlock extends MEInterfaceUpgradeBlock {
    public MEExtendedInterfaceAddonBlock(Properties settings) {
        super(settings);
    }

    @Override
    public Class<? extends BlockEntity> getBlockEntityType() {
        return MEExtendedInterfaceAddonBlockEntity.class;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MEExtendedInterfaceAddonBlockEntity(pos, state);
    }
}
