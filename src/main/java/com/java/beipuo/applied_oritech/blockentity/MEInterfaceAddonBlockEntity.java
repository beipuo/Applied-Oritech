package com.java.beipuo.applied_oritech.blockentity;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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
import appeng.api.storage.MEStorage;

import com.java.beipuo.applied_oritech.AOConfig;
import com.java.beipuo.applied_oritech.AOContent;
import com.java.beipuo.applied_oritech.machine.OritechFluidStorage;
import com.java.beipuo.applied_oritech.machine.OritechMachineMEStorage;
import com.java.beipuo.applied_oritech.machine.OritechMachineStorage;

/**
 * ME Interface Upgrade: keeps a configured set of resources stocked in the attached Oritech machine's
 * input slots or fluid tank, and can request crafts from the network to refill them.
 *
 * <p>Backed by a real {@link InterfaceLogic}, so the configuration UI, fuzzy/crafting upgrade
 * cards, priority and crafting requests all work as on AE2's own interface.
 *
 * <p>One behaviour is inverted relative to AE2. A stock ME Interface is passive: it stocks resources
 * into an internal buffer and publishes that buffer as an {@code ME_STORAGE} capability for an
 * adjacent machine to pull from (see AE2's {@code InitCapabilityProviders}). Oritech machines never
 * pull from their neighbours, so a passive interface would sit there full while the machine starved.
 * This upgrade therefore pushes its stocked buffer into the machine's input slots or fluid tank itself.
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
    public InternalInventory getSubInventory(Identifier id) {
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
        MEStorage machine = new OritechMachineStorage(link, machineName());
        if (link.hasFluids()) {
            machine = new OritechMachineMEStorage(machine,
                    new OritechFluidStorage(link.fluidStorage(), machineName()));
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
        var storage = grid.getStorageService().getInventory();
        var energy = grid.getEnergyService();
        var inventory = link.inventory();
        for (var slot : link.outputSlots()) {
            var stack = link.stackInSlot(slot);
            if (stack.isEmpty()) continue;
            var key = appeng.api.stacks.AEItemKey.of(stack);
            var accepted = appeng.api.storage.StorageHelper.poweredInsert(energy, storage, key,
                    stack.getCount(), actionSource, Actionable.SIMULATE);
            if (accepted <= 0) continue;
            if (!hasMachineEnergy(AOConfig.rfPerTransfer())) break;
            int taken;
            try (var transaction = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                taken = inventory.extract(slot, net.neoforged.neoforge.transfer.item.ItemResource.of(stack),
                        (int) accepted, transaction);
                transaction.commit();
            }
            if (taken <= 0) continue;
            var stored = appeng.api.storage.StorageHelper.poweredInsert(energy, storage, key,
                    taken, actionSource, Actionable.MODULATE);
            if (stored < taken) {
                inventory.set(slot, net.neoforged.neoforge.transfer.item.ItemResource.of(stack),
                        stack.getCount() - (int) stored);
            }
            if (stored > 0) spendMachineEnergy(AOConfig.rfPerTransfer());
        }
    }

    private Component machineName() {
        return getBlockState().getBlock().getName();
    }

    // ---- persistence ----------------------------------------------------------------------

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        logic.writeToNBT(output);
        output.putBoolean("auto_return", autoReturn);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        logic.readFromNBT(input);
        autoReturn = input.getBooleanOr("auto_return", AOConfig.autoReturnOutputsByDefault());
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
