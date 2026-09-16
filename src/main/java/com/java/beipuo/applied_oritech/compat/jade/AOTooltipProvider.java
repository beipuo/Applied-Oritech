package com.java.beipuo.applied_oritech.compat.jade;

import net.minecraft.resources.Identifier;
import appeng.api.integrations.igtooltip.ClientRegistration;
import appeng.api.integrations.igtooltip.CommonRegistration;
import appeng.api.integrations.igtooltip.TooltipProvider;
import appeng.integration.modules.igtooltip.blocks.GridNodeStateDataProvider;
import appeng.integration.modules.igtooltip.blocks.PatternProviderDataProvider;

import com.java.beipuo.applied_oritech.block.AOGridAddonBlock;
import com.java.beipuo.applied_oritech.Applied_oritech;
import com.java.beipuo.applied_oritech.block.MEPatternProviderAddonBlock;
import com.java.beipuo.applied_oritech.blockentity.AOGridAddonBlockEntity;
import com.java.beipuo.applied_oritech.blockentity.MEPatternProviderAddonBlockEntity;

/** Registers our Oritech-based entities with AE2's existing Jade tooltip providers. */
public final class AOTooltipProvider implements TooltipProvider {
    // Jade keys providers by ID, so AE2's IDs cannot be reused for a different host class.
    private static final Identifier GRID_NODE_STATE =
            Identifier.fromNamespaceAndPath(Applied_oritech.MODID, "grid_node_state");
    private static final Identifier PATTERN_PROVIDER =
            Identifier.fromNamespaceAndPath(Applied_oritech.MODID, "pattern_provider");

    @Override
    public void registerCommon(CommonRegistration registration) {
        registration.addBlockEntityData(GRID_NODE_STATE,
                AOGridAddonBlockEntity.class, new GridNodeStateDataProvider());
        registration.addBlockEntityData(PATTERN_PROVIDER,
                MEPatternProviderAddonBlockEntity.class, new PatternProviderDataProvider());
    }

    @Override
    public void registerClient(ClientRegistration registration) {
        registration.addBlockEntityBody(AOGridAddonBlockEntity.class,
                AOGridAddonBlock.class, GRID_NODE_STATE, new GridNodeStateDataProvider());
        registration.addBlockEntityBody(MEPatternProviderAddonBlockEntity.class,
                MEPatternProviderAddonBlock.class, PATTERN_PROVIDER,
                new PatternProviderDataProvider());
    }
}
