package com.java.beipuo.applied_oritech;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.java.beipuo.applied_oritech.block.MEDockBlock;
import com.java.beipuo.applied_oritech.block.MEInterfaceAddonBlock;
import com.java.beipuo.applied_oritech.block.MEPatternProviderAddonBlock;
import com.java.beipuo.applied_oritech.blockentity.MEDockBlockEntity;
import com.java.beipuo.applied_oritech.blockentity.MEInterfaceAddonBlockEntity;
import com.java.beipuo.applied_oritech.blockentity.MEPatternProviderAddonBlockEntity;
import com.java.beipuo.applied_oritech.compat.extendedae.ExtendedAEContent;
import com.java.beipuo.applied_oritech.item.AOAddonBlockItem;

/** Every block, item, block entity type and creative tab this mod registers. */
public final class AOContent {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(Applied_oritech.MODID);
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Applied_oritech.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Applied_oritech.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Applied_oritech.MODID);

    // ---- blocks ---------------------------------------------------------------------------

    public static final DeferredBlock<MEDockBlock> ME_DOCK_BLOCK =
            BLOCKS.registerBlock("me_dock", MEDockBlock::new, AOContent::machineAddonProperties);
    public static final DeferredBlock<MEPatternProviderAddonBlock> ME_PATTERN_PROVIDER_UPGRADE_BLOCK =
            BLOCKS.registerBlock("me_pattern_provider_addon",
                    MEPatternProviderAddonBlock::new, AOContent::machineAddonProperties);
    public static final DeferredBlock<MEInterfaceAddonBlock> ME_INTERFACE_UPGRADE_BLOCK =
            BLOCKS.registerBlock("me_interface_addon",
                    MEInterfaceAddonBlock::new, AOContent::machineAddonProperties);

    // ---- items ----------------------------------------------------------------------------

    public static final DeferredItem<BlockItem> ME_DOCK =
            ITEMS.registerItem("me_dock", properties ->
                    new AOAddonBlockItem(ME_DOCK_BLOCK.get(), properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> ME_PATTERN_PROVIDER_UPGRADE =
            ITEMS.registerItem("me_pattern_provider_addon", properties ->
                    new AOAddonBlockItem(ME_PATTERN_PROVIDER_UPGRADE_BLOCK.get(), properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> ME_INTERFACE_UPGRADE =
            ITEMS.registerItem("me_interface_addon", properties ->
                    new AOAddonBlockItem(ME_INTERFACE_UPGRADE_BLOCK.get(), properties.useBlockDescriptionPrefix()));

    // ---- block entities -------------------------------------------------------------------

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MEDockBlockEntity>> ME_DOCK_ENTITY =
            blockEntity("me_dock", MEDockBlockEntity::new, ME_DOCK_BLOCK);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MEPatternProviderAddonBlockEntity>> ME_PATTERN_PROVIDER_UPGRADE_ENTITY =
            blockEntity("me_pattern_provider_addon", MEPatternProviderAddonBlockEntity::new,
                    ME_PATTERN_PROVIDER_UPGRADE_BLOCK);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MEInterfaceAddonBlockEntity>> ME_INTERFACE_UPGRADE_ENTITY =
            blockEntity("me_interface_addon", MEInterfaceAddonBlockEntity::new,
                    ME_INTERFACE_UPGRADE_BLOCK);

    // ---- creative tab ---------------------------------------------------------------------

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_TABS.register(
            "main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.applied_oritech"))
                    .icon(() -> ME_DOCK.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ME_DOCK.get());
                        output.accept(ME_PATTERN_PROVIDER_UPGRADE.get());
                        output.accept(ME_INTERFACE_UPGRADE.get());
                        if (ModList.get().isLoaded("extendedae")) {
                            ExtendedAEContent.addCreativeItems(output);
                        }
                    })
                    .build());

    private AOContent() {
    }

    public static void register(IEventBus modEventBus) {
        if (ModList.get().isLoaded("extendedae")) {
            ExtendedAEContent.register(modEventBus);
        }
        for (var name : new String[] { "me_interface", "me_pattern_provider" }) {
            var oldId = Identifier.fromNamespaceAndPath(Applied_oritech.MODID, name + "_upgrade");
            var newId = Identifier.fromNamespaceAndPath(Applied_oritech.MODID, name + "_addon");
            BLOCKS.addAlias(oldId, newId);
            ITEMS.addAlias(oldId, newId);
            BLOCK_ENTITIES.addAlias(oldId, newId);
        }
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
    }

    public static BlockBehaviour.Properties machineAddonProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GRAY)
                .strength(2.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL);
    }

    public static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> blockEntity(
            String name, BlockEntityType.BlockEntitySupplier<T> factory, Supplier<? extends Block> block) {
        return BLOCK_ENTITIES.register(name,
                () -> new BlockEntityType<>(factory, block.get()));
    }
}
