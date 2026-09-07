package com.java.beipuo.applied_oritech.blockentity;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;

import com.java.beipuo.applied_oritech.AOConfig;
import com.java.beipuo.applied_oritech.AOContent;
import com.java.beipuo.applied_oritech.machine.OritechMachineLink;

/**
 * ME Pattern Provider Upgrade: makes the attached Oritech machine a crafting target for the ME
 * network, driven by a genuine {@link PatternProviderLogic} so patterns, blocking mode, priority
 * and the Pattern Access Terminal all behave exactly as they do on AE2's own block.
 *
 * <p>Two things need bridging, because AE2 assumes a very different machine than Oritech provides.
 *
 * <p><b>Pushing ingredients.</b> AE2 resolves its push target one block away and prefers the
 * {@code ME_STORAGE} capability over a plain item handler. We register that capability on Oritech
 * machines for the sides an upgrade occupies (see {@code AOCapabilities}), mapped onto recipe
 * input slots only, so ingredients can no longer land in a result slot.
 *
 * <p><b>Getting results back.</b> A stock pattern provider waits for the machine to push its
 * output into the provider's return inventory. Oritech machines never push anything — results
 * just sit in their output slots — so a crafting job would hang forever. This upgrade therefore
 * sweeps the machine's result slots into the network itself. That behaviour is a toggle
 * ({@link #isAutoReturn()}): its default comes from config, and it can be flipped per block.
 */
public class MEPatternProviderUpgradeBlockEntity extends MEUpgradeBlockEntity
        implements PatternProviderLogicHost {

    private final PatternProviderLogic logic;
    private final IActionSource actionSource = IActionSource.ofMachine(this);

    private boolean autoReturn = AOConfig.autoReturnOutputsByDefault();
    private boolean patternsInitialised;

    public MEPatternProviderUpgradeBlockEntity(BlockPos pos, BlockState state) {
        super(AOContent.ME_PATTERN_PROVIDER_UPGRADE_ENTITY.get(), pos, state);
        this.logic = new PatternProviderLogic(getMainNode(), this);
        // Must follow the logic construction — see MEUpgradeBlockEntity#applyGroupFlags.
        applyGroupFlags();
        getMainNode().setIdlePowerUsage(AOConfig.upgradeIdlePower());
    }

    // ---- PatternProviderLogicHost ----------------------------------------------------------

    @Override
    public PatternProviderLogic getLogic() {
        return logic;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    /**
     * The sides AE2 may push ingredients towards: whichever of our six neighbours belongs to the
     * attached machine. Returns a fresh mutable set on every call because
     * {@code PatternProviderLogic#getActiveSides} removes entries from it.
     */
    @Override
    public EnumSet<Direction> getTargets() {
        if (!isNetworkOnline()) return EnumSet.noneOf(Direction.class);
        var machine = getMachine();
        var sides = OritechMachineLink.sidesFacingMachine(level, worldPosition,
                machine == null ? null : machine.getPosForAddon());
        var dock = getDock();
        if (dock != null) {
            for (var side : Direction.values()) {
                if (worldPosition.relative(side).equals(dock.getBlockPos())) sides.add(side);
            }
        }
        return sides;
    }

    @Override
    public AEItemKey getTerminalIcon() {
        return AEItemKey.of(AOContent.ME_PATTERN_PROVIDER_UPGRADE.get());
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(AOContent.ME_PATTERN_PROVIDER_UPGRADE.get());
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        logic.onMainNodeStateChanged();
    }

    // ---- the output sweep ------------------------------------------------------------------

    public boolean isAutoReturn() {
        return autoReturn;
    }

    /** @return the new state */
    public boolean toggleAutoReturn() {
        autoReturn = !autoReturn;
        setChanged();
        return autoReturn;
    }

    @Override
    protected void onServerTick() {
        if (!patternsInitialised) {
            patternsInitialised = true;
            // AE2's own block does this in onReady(); we have no such hook.
            logic.updatePatterns();
        }

        if (!autoReturn || !isNetworkOnline()) return;

        var link = getMachineLink();
        if (link == null || !link.hasOutputs()) return;
        var returns = logic.getReturnInv();
        var inventory = link.inventory();

        for (var slot : link.outputSlots()) {
            var stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty()) continue;

            var key = AEItemKey.of(stack);
            if (key == null) continue;

            var accepted = returns.insert(key, stack.getCount(), Actionable.SIMULATE, actionSource);
            if (accepted <= 0) continue;

            if (!spendMachineEnergy(AOConfig.rfPerTransfer())) break;

            var original = stack.copy();
            var taken = inventory.extractFromSlot(key.toStack((int) accepted), slot, false);
            if (taken <= 0) continue;
            // The local AE2 buffer persists overflow and notifies crafting-lock tracking on return.
            var stored = returns.insert(key, taken, Actionable.MODULATE, actionSource);
            if (stored < taken) {
                inventory.setStackInSlot(slot, original.copyWithCount(original.getCount() - (int) stored));
            }
            inventory.update();
        }
    }

    // ---- persistence ----------------------------------------------------------------------

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
        if (nbt.contains("auto_return")) {
            autoReturn = nbt.getBoolean("auto_return");
        }
    }

    /** Encoded patterns and anything left in the return inventory must not be destroyed. */
    @Override
    public void addAdditionalDrops(List<ItemStack> drops) {
        logic.addDrops(drops);
    }

    @Override
    public void clearLogicContent() {
        logic.clearContent();
    }
}
