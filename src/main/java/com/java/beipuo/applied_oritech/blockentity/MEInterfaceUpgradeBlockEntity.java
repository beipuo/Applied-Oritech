package com.java.beipuo.applied_oritech.blockentity;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.security.IActionSource;
import appeng.api.util.AECableType;
import appeng.helpers.InterfaceLogic;
import appeng.helpers.InterfaceLogicHost;
import appeng.core.definitions.AEItems;
import appeng.api.stacks.GenericStack;

import com.java.beipuo.applied_oritech.AOConfig;
import com.java.beipuo.applied_oritech.AOContent;
import com.java.beipuo.applied_oritech.machine.OritechMachineStorage;

/**
 * ME Interface Upgrade: keeps a configured set of items stocked in the attached Oritech machine's
 * input slots, and can request crafts from the network to refill them.
 *
 * <p>Backed by a real {@link InterfaceLogic}, so the configuration UI, fuzzy/crafting upgrade
 * cards, priority and crafting requests all work as on AE2's own interface.
 *
 * <p>One behaviour is inverted relative to AE2. A stock ME Interface is passive: it stocks items
 * into an internal buffer and publishes that buffer as an {@code ME_STORAGE} capability for an
 * adjacent machine to pull from (see AE2's {@code InitCapabilityProviders}). Oritech machines never
 * pull from their neighbours, so a passive interface would sit there full while the machine starved.
 * This upgrade therefore pushes its stocked buffer into the machine's input slots itself.
 */
public class MEInterfaceUpgradeBlockEntity extends MEUpgradeBlockEntity
        implements InterfaceLogicHost, ISegmentedInventory {

    private final InterfaceLogic logic;
    private final IActionSource actionSource = IActionSource.ofMachine(this);

    public MEInterfaceUpgradeBlockEntity(BlockPos pos, BlockState state) {
        super(AOContent.ME_INTERFACE_UPGRADE_ENTITY.get(), pos, state);
        this.logic = new InterfaceLogic(getMainNode(), this, AOContent.ME_INTERFACE_UPGRADE.get());
        // Must follow the logic construction — see MEUpgradeBlockEntity#applyGroupFlags.
        applyGroupFlags();
        getMainNode().setIdlePowerUsage(AOConfig.upgradeIdlePower());
    }

    // ---- InterfaceLogicHost ----------------------------------------------------------------

    @Override
    public InterfaceLogic getInterfaceLogic() {
        return logic;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(AOContent.ME_INTERFACE_UPGRADE.get());
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return logic.getCableConnectionType(dir);
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (getMainNode().hasGridBooted()) {
            logic.notifyNeighbors();
        }
    }

    @Override
    protected void onGridChanged() {
        logic.gridChanged();
    }

    /** Lets AE2's interface menu find the upgrade card slots. */
    @Nullable
    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        return id.equals(UPGRADES) ? logic.getUpgrades() : null;
    }

    // ---- stocking the machine --------------------------------------------------------------

    @Override
    protected void onServerTick() {
        if (!isNetworkOnline()) return;

        var link = getMachineLink();
        if (link == null || !link.hasInputs()) return;

        var stocked = logic.getStorage();
        var machine = new OritechMachineStorage(link, machineName());
        for (int slot = 0; slot < stocked.size(); slot++) {
            var entry = stocked.getStack(slot);
            var configured = logic.getConfig().getStack(slot);
            if (entry == null || configured == null || configured.amount() <= 0) continue;
            var key = entry.what();
            var matches = key.equals(configured.what())
                    || logic.getUpgrades().isInstalled(AEItems.FUZZY_CARD)
                    && configured.what().fuzzyEquals(key,
                            logic.getConfigManager().getSetting(Settings.FUZZY_MODE));
            if (!matches) continue;
            var amount = entry.amount();

            // Ask the machine what it would take before removing anything from the buffer.
            var acceptable = machine.insert(key, amount, Actionable.SIMULATE, actionSource);
            if (acceptable <= 0) continue;

            if (!spendMachineEnergy(AOConfig.rfPerTransfer())) break;

            var taken = stocked.extract(slot, key, acceptable, Actionable.MODULATE);
            if (taken <= 0) continue;

            var inserted = machine.insert(key, taken, Actionable.MODULATE, actionSource);
            if (inserted < taken) {
                // Return the remainder to the buffer rather than dropping it on the floor.
                stocked.setStack(slot, new GenericStack(key, entry.amount() - inserted));
            }
        }
    }

    private Component machineName() {
        return getBlockState().getBlock().getName();
    }

    // ---- persistence ----------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        logic.writeToNBT(nbt, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        logic.readFromNBT(nbt, registries);
    }

    /** Stocked items and installed upgrade cards must survive breaking the block. */
    @Override
    public void addAdditionalDrops(List<ItemStack> drops) {
        logic.addDrops(drops);
    }

    @Override
    public void clearLogicContent() {
        logic.clearContent();
    }
}
