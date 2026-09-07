package com.java.beipuo.applied_oritech.blockentity;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.util.AECableType;
import appeng.helpers.InterfaceLogic;
import appeng.me.helpers.BlockEntityNodeListener;
import appeng.me.helpers.IGridConnectedBlockEntity;

import rearth.oritech.block.blocks.addons.MachineAddonBlock;
import rearth.oritech.block.entity.addons.AddonBlockEntity;
import rearth.oritech.util.MachineAddonController;

import com.java.beipuo.applied_oritech.machine.OritechMachineLink;

/**
 * Base for every block in this mod: an Oritech machine addon that also owns an AE2 grid node.
 *
 * <p>Oritech's {@link AddonBlockEntity} and AE2's {@code AENetworkedBlockEntity} are both
 * concrete classes, so nothing can inherit from both. We extend the Oritech side — that is what
 * makes a machine's addon scan claim the block and hand us a controller position — and
 * re-implement the node lifecycle that AE2's base class would otherwise provide, by implementing
 * {@link IGridConnectedBlockEntity}.
 *
 * <p>Node configuration is deliberately left to each subclass's constructor rather than an
 * overridable hook called from here: AE2's logic classes call {@code setFlags} themselves from
 * their own constructors, so the flags have to be applied after the subclass fields exist.
 */
public abstract class AOGridAddonBlockEntity extends AddonBlockEntity implements IGridConnectedBlockEntity {

    /**
     * Adds a grid-changed callback on top of AE2's stock listener. {@link InterfaceLogic} needs it
     * to re-plan its stock when the network changes, and AE2's own interface block entity installs
     * an equivalent listener for exactly that reason.
     */
    private static final BlockEntityNodeListener<AOGridAddonBlockEntity> NODE_LISTENER =
            new BlockEntityNodeListener<>() {
                @Override
                public void onGridChanged(AOGridAddonBlockEntity owner, IGridNode node) {
                    owner.onGridChanged();
                }
            };

    private final IManagedGridNode mainNode;

    protected AOGridAddonBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.mainNode = GridHelper.createManagedNode(this, NODE_LISTENER)
                .setVisualRepresentation(state.getBlock())
                .setTagName("ao_node");
    }

    /** Called when the grid this node belongs to changes. */
    protected void onGridChanged() {
    }

    @Override
    public final IManagedGridNode getMainNode() {
        return mainNode;
    }

    @Override
    public void saveChanges() {
        setChanged();
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    // ---- node lifecycle --------------------------------------------------------------------

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        // AE2 requires create() to run in a ticking chunk, never during load. This mirrors
        // what AEBaseBlockEntity does via scheduleInit()/onReady().
        GridHelper.onFirstTick(this, be -> be.mainNode.create(be.getLevel(), be.getBlockPos()));
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        mainNode.destroy();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        mainNode.destroy();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        mainNode.saveToNBT(nbt);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        mainNode.loadFromNBT(nbt);
    }

    // ---- Oritech machine attachment -------------------------------------------------------

    /**
     * Whether a machine's addon scan has claimed this block. Until then {@link #getControllerPos()}
     * points at a stale or zero offset and must not be trusted.
     */
    public boolean isAttachedToMachine() {
        return getBlockState().getValue(MachineAddonBlock.ADDON_USED);
    }

    /** The Oritech machine controlling this addon, or null when unattached. */
    @Nullable
    public MachineAddonController getMachine() {
        if (isRemoved() || !getBlockState().getValue(MachineAddonBlock.ADDON_USED) || level == null
                || !level.hasChunkAt(getControllerPos())) return null;
        return level.getBlockEntity(getControllerPos()) instanceof MachineAddonController machine
                && machine.getConnectedAddons().contains(worldPosition)
                ? machine
                : null;
    }

    /** A resolved input/output slot view of the attached machine, or null when unavailable. */
    @Nullable
    public OritechMachineLink getMachineLink() {
        var machine = getMachine();
        return machine == null ? null : OritechMachineLink.resolve(level, machine.getPosForAddon());
    }

    /**
     * Spends Oritech RF from the attached machine's buffer.
     *
     * <p>Deliberately writes {@code amount} directly instead of calling
     * {@code DynamicEnergyStorage#extract}: that method is capped by {@code maxExtract}, which is
     * zero on machines that never push energy out, so extract() would always return 0. Oritech's
     * own machines pay for recipes the same way (see {@code MachineBlockEntity#tick}).
     *
     * @return true when the full cost was paid; false leaves the buffer untouched.
     */
    public boolean spendMachineEnergy(long cost) {
        if (cost <= 0) return true;

        var machine = getMachine();
        if (machine == null) return false;

        var storage = machine.getStorageForAddon();
        if (storage == null || storage.amount < cost) return false;

        storage.amount -= cost;
        if (machine instanceof net.minecraft.world.level.block.entity.BlockEntity entity) {
            entity.setChanged();
        }
        return true;
    }

    public boolean hasMachineEnergy(long cost) {
        var machine = getMachine();
        if (machine == null) return false;
        var storage = machine.getStorageForAddon();
        return cost <= 0 || storage != null && storage.amount >= cost;
    }
}
