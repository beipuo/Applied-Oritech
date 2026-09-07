package com.java.beipuo.applied_oritech.blockentity;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridMultiblock;
import appeng.api.networking.IGridNode;
import rearth.oritech.util.MachineAddonController;

/**
 * Shared behaviour for the two upgrade addons.
 *
 * <p>An upgrade owns a full AE2 device (a pattern provider or an interface) but has no in-world
 * node: it is invisible to cables and joins the network only through a direct
 * {@link GridHelper#createConnection} to its {@link MEDockBlockEntity}. Keeping the node private
 * to us is what lets the dock's {@link IGridMultiblock} group absorb it, and therefore what keeps
 * upgrades from consuming channels of their own.
 */
public abstract class MEUpgradeBlockEntity extends AOGridAddonBlockEntity implements IGridMultiblock {


    protected MEUpgradeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        getMainNode()
                .setInWorldNode(false)
                .addService(IGridMultiblock.class, this);
    }

    /**
     * Re-applies the grid flags once the AE2 logic object exists.
     *
     * <p>Must be called from the subclass constructor <em>after</em> its logic field is created.
     * Both {@code PatternProviderLogic} and {@code InterfaceLogic} call
     * {@code setFlags(REQUIRE_CHANNEL)} from their own constructors, and {@code setFlags} replaces
     * the flag set rather than adding to it — so without this the {@code MULTIBLOCK} flag would be
     * wiped and the upgrade would bill the network for a second channel.
     */
    protected final void applyGroupFlags() {
        getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL, GridFlags.MULTIBLOCK);
    }

    // ---- dock link -------------------------------------------------------------------------

    @Nullable
    public MEDockBlockEntity getDock() {
        if (level == null || isRemoved()) return null;
        var directMachine = super.getMachine();
        MEDockBlockEntity adjacent = null;
        for (var direction : Direction.values()) {
            var pos = worldPosition.relative(direction);
            if (!level.hasChunkAt(pos)) continue;
            if (!(level.getBlockEntity(pos) instanceof MEDockBlockEntity dock)) continue;
            var machine = dock.getMachine();
            if (machine == null || directMachine != null && machine != directMachine) continue;
            if (adjacent == null || pos.compareTo(adjacent.getBlockPos()) < 0) adjacent = dock;
        }
        return adjacent != null ? adjacent : MEDockBlockEntity.chooseDockFor(level, directMachine, worldPosition);
    }

    @Override
    @Nullable
    public MachineAddonController getMachine() {
        var dock = getDock();
        return dock == null ? super.getMachine() : dock.getMachine();
    }

    @Override
    public boolean isAttachedToMachine() {
        return getMachine() != null;
    }

    @Override
    public Iterator<IGridNode> getMultiblockNodes() {
        var dock = getDock();
        if (dock != null) return dock.collectGroupNodes().iterator();

        // Unattached: the upgrade is its own one-node group.
        var own = getMainNode().getNode();
        return own == null ? Collections.emptyIterator() : List.of(own).iterator();
    }

    /**
     * Brings the grid connection in line with the dock we should be attached to.
     *
     * <p>Written as "destroy everything that is not the connection I want, then make the one I
     * want" so it is self-healing: it recovers from the dock being broken, the machine's addons
     * being rescanned, or a newly placed dock becoming the closer one, without tracking state.
     */
    private void syncDockConnection() {
        var ourNode = getMainNode().getNode();
        if (ourNode == null) return;

        var dock = getDock();
        var dockNode = dock == null ? null : dock.getMainNode().getNode();

        var alreadyLinked = false;
        for (var connection : List.copyOf(ourNode.getConnections())) {
            if (dockNode != null && connection.getOtherSide(ourNode) == dockNode) {
                alreadyLinked = true;
            } else {
                connection.destroy();
            }
        }

        if (dockNode == null || alreadyLinked) return;

        try {
            GridHelper.createConnection(ourNode, dockNode);
        } catch (IllegalStateException ignored) {
            // AE2 throws when the pair is already connected. Harmless: the next sync sees it.
        }
    }

    /** True when the dock exists, has a channel, and the network is powered. */
    public boolean isNetworkOnline() {
        var node = getMainNode().getNode();
        var dock = getDock();
        return node != null && node.isActive() && dock != null && dock.isOnline()
                && node.getGrid() == dock.getMainNode().getGrid();
    }

    // ---- ticking ---------------------------------------------------------------------------

    /** Driven by the block's ticker; server side only. */
    public final void tickServer() {
        syncDockConnection();
        onServerTick();
    }

    /** Per-tick work for the concrete upgrade. */
    protected abstract void onServerTick();

    /**
     * Contents that must not be destroyed when the block breaks — encoded patterns, stocked items,
     * installed upgrade cards. Collected by the block rather than a loot table because these live
     * in AE2 logic objects, not a vanilla container.
     */
    public abstract void addAdditionalDrops(List<ItemStack> drops);

    /**
     * Empties the AE2 logic inventories. Called by the block right after their contents have been
     * dropped, so nothing can be recovered twice.
     */
    public abstract void clearLogicContent();

    /**
     * Oritech calls this whenever a machine claims or releases an addon, so it is the earliest
     * signal that our dock may have changed. Re-linking immediately avoids a visible stall after
     * placing a dock next to an already-installed upgrade.
     */
    @Override
    public void setControllerPos(BlockPos pos) {
        super.setControllerPos(pos);
        if (level != null && !level.isClientSide()) {
            syncDockConnection();
        }
    }
}
