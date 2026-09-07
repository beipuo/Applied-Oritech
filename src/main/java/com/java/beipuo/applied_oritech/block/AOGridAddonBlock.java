package com.java.beipuo.applied_oritech.block;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import rearth.oritech.block.blocks.addons.MachineAddonBlock;

import com.java.beipuo.applied_oritech.blockentity.AOGridAddonBlockEntity;

public abstract class AOGridAddonBlock extends MachineAddonBlock {
    protected AOGridAddonBlock(Properties settings) {
        super(settings, AddonSettings.getDefaultSettings().withNeedsSupport(false));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && placer instanceof Player player
                && level.getBlockEntity(pos) instanceof AOGridAddonBlockEntity addon) {
            addon.getMainNode().setOwningPlayer(player);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState,
            boolean movedByPiston) {
        if (!level.isClientSide() && !state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof AOGridAddonBlockEntity addon) {
            var machine = addon.getMachine();
            if (machine != null) machine.initAddons(pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
            List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(getDescriptionId().replace("block.", "tooltip."))
                .withStyle(ChatFormatting.GRAY));
    }
}
