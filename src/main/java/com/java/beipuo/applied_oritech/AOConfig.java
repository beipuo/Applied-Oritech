package com.java.beipuo.applied_oritech;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Config for the addon.
 *
 * <p>Running cost is split across both mods deliberately: the grid nodes draw AE/t so the network's
 * own power readout accounts for them, and every item actually moved costs Oritech RF out of the
 * machine's buffer so the bridge is not free on the Oritech side either.
 */
public final class AOConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.DoubleValue DOCK_IDLE_POWER = BUILDER
            .comment("AE/t drawn from the ME network by an ME Dock, whether or not anything is attached.")
            .defineInRange("power.dockIdleDraw", 1.0D, 0.0D, 1024.0D);

    private static final ModConfigSpec.DoubleValue UPGRADE_IDLE_POWER = BUILDER
            .comment("AE/t drawn from the ME network by each installed upgrade module.")
            .defineInRange("power.upgradeIdleDraw", 2.0D, 0.0D, 1024.0D);

    private static final ModConfigSpec.LongValue RF_PER_TRANSFER = BUILDER
            .comment("Oritech RF taken from the machine's own buffer for each stack moved in or out.",
                    "Set to 0 to make transfers cost nothing on the Oritech side.")
            .defineInRange("power.rfPerTransfer", 20L, 0L, Long.MAX_VALUE);

    private static final ModConfigSpec.BooleanValue AUTO_RETURN_OUTPUTS = BUILDER
            .comment("Default state of an ME Pattern Provider Upgrade's output sweep.",
                    "Oritech machines never push their results anywhere, so with this off an",
                    "autocrafting job will never complete unless you move results back to the",
                    "network yourself (an interface upgrade, an export bus, or Oritech's own",
                    "inventory proxy). Each upgrade can be toggled individually by",
                    "sneak-right-clicking it with an empty hand; this only sets the initial value.")
            .define("behaviour.autoReturnOutputs", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private AOConfig() {
    }

    public static double dockIdlePower() {
        return DOCK_IDLE_POWER.get();
    }

    public static double upgradeIdlePower() {
        return UPGRADE_IDLE_POWER.get();
    }

    public static long rfPerTransfer() {
        return RF_PER_TRANSFER.get();
    }

    public static boolean autoReturnOutputsByDefault() {
        return AUTO_RETURN_OUTPUTS.get();
    }
}
