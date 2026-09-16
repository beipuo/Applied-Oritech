package com.java.beipuo.applied_oritech.item;

import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.block.Block;

/** Forwards the addon's existing tooltip through the 26.1.2 item API. */
public final class AOAddonBlockItem extends BlockItem {
    public AOAddonBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
            Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        if (getBlock() instanceof TooltipProvider provider) {
            provider.addToTooltip(context, tooltip, flag, stack);
        }
    }
}
