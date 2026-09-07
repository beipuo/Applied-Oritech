package com.java.beipuo.applied_oritech.machine;

import java.util.function.Supplier;

import net.minecraft.network.chat.Component;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;

import com.java.beipuo.applied_oritech.AOConfig;
import com.java.beipuo.applied_oritech.blockentity.MEPatternProviderUpgradeBlockEntity;

/** A cached AE2 capability must resolve attachment again before every operation. */
public final class OritechPatternInputStorage implements MEStorage {
    private final Supplier<MEPatternProviderUpgradeBlockEntity> provider;
    private final Component description;

    public OritechPatternInputStorage(Supplier<MEPatternProviderUpgradeBlockEntity> provider,
            Component description) {
        this.provider = provider;
        this.description = description;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        var upgrade = provider.get();
        var cost = AOConfig.rfPerTransfer();
        if (upgrade == null || !upgrade.isNetworkOnline() || !upgrade.hasMachineEnergy(cost)) return 0;
        var link = upgrade.getMachineLink();
        if (link == null) return 0;
        var storage = new OritechMachineStorage(link, description);
        var accepted = storage.insert(what, amount, Actionable.SIMULATE, source);
        if (mode == Actionable.SIMULATE || accepted <= 0) return accepted;
        if (!upgrade.spendMachineEnergy(cost)) return 0;
        return storage.insert(what, accepted, Actionable.MODULATE, source);
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        var upgrade = provider.get();
        if (upgrade == null || !upgrade.isNetworkOnline()) return;
        var link = upgrade.getMachineLink();
        if (link == null) return;
        // AE2 uses this list for blocking mode. Extraction remains disabled.
        for (var slot : link.inputSlots()) {
            var stack = link.inventory().getStackInSlot(slot);
            if (!stack.isEmpty()) out.add(AEItemKey.of(stack), stack.getCount());
        }
    }

    @Override
    public Component getDescription() {
        return description;
    }
}
