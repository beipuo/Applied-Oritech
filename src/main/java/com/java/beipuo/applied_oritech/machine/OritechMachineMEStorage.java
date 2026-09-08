package com.java.beipuo.applied_oritech.machine;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import net.minecraft.network.chat.Component;

public final class OritechMachineMEStorage implements MEStorage {
    private final MEStorage items;
    private final MEStorage fluids;
    public OritechMachineMEStorage(MEStorage items, MEStorage fluids) {
        this.items = items;
        this.fluids = fluids;
    }
    @Override public long insert(AEKey key, long amount, Actionable mode, IActionSource source) {
        return key instanceof AEFluidKey ? fluids.insert(key, amount, mode, source) : items.insert(key, amount, mode, source);
    }
    @Override public long extract(AEKey key, long amount, Actionable mode, IActionSource source) {
        return key instanceof AEFluidKey ? fluids.extract(key, amount, mode, source) : items.extract(key, amount, mode, source);
    }
    @Override public void getAvailableStacks(KeyCounter out) {
        items.getAvailableStacks(out);
        fluids.getAvailableStacks(out);
    }
    @Override public Component getDescription() { return items.getDescription(); }
}
