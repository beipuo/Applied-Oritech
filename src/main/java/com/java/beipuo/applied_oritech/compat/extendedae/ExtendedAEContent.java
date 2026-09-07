package com.java.beipuo.applied_oritech.compat.extendedae;

import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import com.java.beipuo.applied_oritech.AOContent;

/** Loaded only after the caller has confirmed that ExtendedAE is installed. */
public final class ExtendedAEContent {
    public static final DeferredBlock<MEExtendedPatternProviderAddonBlock> PATTERN_PROVIDER_BLOCK =
            AOContent.BLOCKS.register("me_extended_pattern_provider_addon",
                    () -> new MEExtendedPatternProviderAddonBlock(AOContent.machineAddonProperties()));
    public static final DeferredBlock<MEExtendedInterfaceAddonBlock> INTERFACE_BLOCK =
            AOContent.BLOCKS.register("me_extended_interface_addon",
                    () -> new MEExtendedInterfaceAddonBlock(AOContent.machineAddonProperties()));

    public static final DeferredItem<BlockItem> PATTERN_PROVIDER =
            AOContent.ITEMS.registerSimpleBlockItem("me_extended_pattern_provider_addon", PATTERN_PROVIDER_BLOCK);
    public static final DeferredItem<BlockItem> INTERFACE =
            AOContent.ITEMS.registerSimpleBlockItem("me_extended_interface_addon", INTERFACE_BLOCK);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MEExtendedPatternProviderAddonBlockEntity>>
            PATTERN_PROVIDER_ENTITY = AOContent.blockEntity("me_extended_pattern_provider_addon",
                    MEExtendedPatternProviderAddonBlockEntity::new, PATTERN_PROVIDER_BLOCK);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MEExtendedInterfaceAddonBlockEntity>>
            INTERFACE_ENTITY = AOContent.blockEntity("me_extended_interface_addon",
                    MEExtendedInterfaceAddonBlockEntity::new, INTERFACE_BLOCK);

    private ExtendedAEContent() {
    }

    public static void register(IEventBus bus) {
        bus.addListener(ExtendedAEContent::commonSetup);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Upgrades.add(AEItems.CRAFTING_CARD, INTERFACE.get(), 1);
            Upgrades.add(AEItems.FUZZY_CARD, INTERFACE.get(), 1);
        });
    }

    public static void addCreativeItems(CreativeModeTab.Output output) {
        output.accept(PATTERN_PROVIDER.get());
        output.accept(INTERFACE.get());
    }
}
