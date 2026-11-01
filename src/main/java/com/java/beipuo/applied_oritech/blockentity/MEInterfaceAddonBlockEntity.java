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
import net.minecraft.world.level.block.entity.BlockEntityType;
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
 * ME Interface Upgrade: keeps configured items and fluids stocked in the attached Oritech machine.
 * Backed by {@link InterfaceLogic} for configuration, upgrade cards and crafting requests.
 * Oritech machines do not pull from interfaces, so this addon pushes the buffer into the machine
 * and optionally returns machine outputs to the network.
 */
public class MEInterfaceAddonBlockEntity extends MEAddonBlockEntity
        implements InterfaceLogicHost, ISegmentedInventory {

    private final InterfaceLogic logic;
    private final IActionSource actionSource = IActionSource.ofMachine(this);

    public MEInterfaceAddonBlockEntity(BlockPos pos, BlockState state) {
        this(AOContent.ME_INTERFACE_UPGRADE_ENTITY.get(), pos, state, 9);
    }

    protected MEInterfaceAddonBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int slots) {
        super(type, pos, state);
        this.logic = new InterfaceLogic(getMainNode(), this, state.getBlock().asItem(), slots);
        // Must follow the logic construction — see MEAddonBlockEntity#applyGroupFlags.
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
        return new ItemStack(getBlockState().getBlock());
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
        if (link == null) return;
        if (autoReturn) returnOutputs(link);
        if (!link.hasInputs() && !link.hasFluids()) return;

        var stocked = logic.getStorage();
        appeng.api.storage.MEStorage machine = new OritechMachineStorage(link, machineName());
        if (link.hasFluids()) {
            machine = new com.java.beipuo.applied_oritech.machine.OritechMachineMEStorage(machine,
                    new com.java.beipuo.applied_oritech.machine.OritechFluidStorage(link.fluidStorage(), machineName()));
        }
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

    private boolean autoReturn = AOConfig.autoReturnOutputsByDefault();

    public boolean isAutoReturn() {
        return autoReturn;
    }

    public boolean toggleAutoReturn() {
        autoReturn = !autoReturn;
        setChanged();
        return autoReturn;
    }

    private void returnOutputs(com.java.beipuo.applied_oritech.machine.OritechMachineLink link) {
        var grid = getMainNode().getGrid();
        if (grid == null) return;
        com.java.beipuo.applied_oritech.machine.OritechOutputTransfer.sweep(link,
                (key, amount, mode) -> appeng.api.storage.StorageHelper.poweredInsert(
                        grid.getEnergyService(), grid.getStorageService().getInventory(), key, amount, actionSource, mode),
                () -> hasMachineEnergy(AOConfig.rfPerTransfer()),
                () -> spendMachineEnergy(AOConfig.rfPerTransfer()));
    }

    private Component machineName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        logic.writeToNBT(nbt, registries);
        nbt.putBoolean("auto_return", autoReturn);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        logic.readFromNBT(nbt, registries);
        autoReturn = nbt.contains("auto_return") ? nbt.getBoolean("auto_return") : AOConfig.autoReturnOutputsByDefault();
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
