package com.java.beipuo.applied_oritech;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;

/**
 * Applied Oritech — connects Oritech machines to Applied Energistics 2 ME networks.
 *
 * <p>Three addon blocks, all of which attach to an Oritech machine through Oritech's own addon slot
 * system:
 *
 * <ul>
 * <li><b>ME Dock</b> — the network connection point, and the group's single channel.
 * <li><b>ME Pattern Provider Upgrade</b> — makes the machine an autocrafting target.
 * <li><b>ME Interface Upgrade</b> — keeps the machine stocked with configured items.
 * </ul>
 *
 * <p>Upgrades cost no channels of their own: they join the dock's {@code IGridMultiblock} group, so
 * AE2 charges the network once for the whole assembly.
 */
@Mod(Applied_oritech.MODID)
public class Applied_oritech {

    public static final String MODID = "applied_oritech";
    public static final Logger LOGGER = LoggerFactory.getLogger("Applied Oritech");

    public Applied_oritech(IEventBus modEventBus, ModContainer modContainer) {
        AOContent.register(modEventBus);
        modEventBus.addListener(AOCapabilities::register);
        modEventBus.addListener(this::commonSetup);

        modContainer.registerConfig(ModConfig.Type.COMMON, AOConfig.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Upgrades.add(AEItems.CRAFTING_CARD, AOContent.ME_INTERFACE_UPGRADE.get(), 1);
            Upgrades.add(AEItems.FUZZY_CARD, AOContent.ME_INTERFACE_UPGRADE.get(), 1);
        });
    }
}
