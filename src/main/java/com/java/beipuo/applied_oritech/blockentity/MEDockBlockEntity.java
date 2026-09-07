package com.java.beipuo.applied_oritech.blockentity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedHashSet;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridMultiblock;
import appeng.api.networking.IGridNode;

import rearth.oritech.util.MachineAddonController;

import com.java.beipuo.applied_oritech.AOConfig;
import com.java.beipuo.applied_oritech.AOContent;

/**
 * The ME Dock: the single point where an Oritech machine meets an ME network.
 *
 * <p>This is the only block in the mod whose node is exposed in-world, so it is the only one a
 * cable can attach to, and the only one that bills the network for a channel. Every upgrade on
 * the same machine gets its own node — necessary, because {@code PatternProviderLogic} and
 * {@code InterfaceLogic} each register an {@code IGridTickable} service and would overwrite one
 * another on a shared node — and those nodes join this dock's {@link IGridMultiblock} group.
 *
 * <p>AE2's {@code PathingCalculation} grants a channel to the first {@code MULTIBLOCK} member it
 * reaches and records every other node in the group as already served, so the dock plus any
 * number of upgrades costs exactly one channel. That is the mechanism behind the design goal that
 * upgrade modules are channel-free.
 */
public class MEDockBlockEntity extends AOGridAddonBlockEntity implements IGridMultiblock {

    public MEDockBlockEntity(BlockPos pos, BlockState state) {
        super(AOContent.ME_DOCK_ENTITY.get(), pos, state);
        getMainNode()
                .setFlags(GridFlags.REQUIRE_CHANNEL, GridFlags.MULTIBLOCK)
                .setIdlePowerUsage(AOConfig.dockIdlePower())
                .setInWorldNode(true)
                .addService(IGridMultiblock.class, this);
    }

    @Override
    public Iterator<IGridNode> getMultiblockNodes() {
        return collectGroupNodes().iterator();
    }

    /**
     * This dock's node plus the node of every upgrade on the same machine that resolves to this
     * dock. Null nodes are filtered out: AE2 logs an error and abandons the multiblock if the
     * iterator yields one.
     */
    public List<IGridNode> collectGroupNodes() {
        var nodes = new ArrayList<IGridNode>();

        var own = getMainNode().getNode();
        if (own != null) nodes.add(own);

        for (var upgrade : findGroupUpgrades()) {
            var node = upgrade.getMainNode().getNode();
            if (node != null) nodes.add(node);
        }

        return nodes;
    }

    /** Every upgrade addon on the same machine that has picked this dock. */
    public List<MEUpgradeBlockEntity> findGroupUpgrades() {
        var machine = getMachine();
        if (machine == null || level == null) return List.of();

        var candidates = new LinkedHashSet<BlockPos>(machine.getConnectedAddons());
        for (var direction : Direction.values()) candidates.add(worldPosition.relative(direction));
        var result = new ArrayList<MEUpgradeBlockEntity>();
        for (var addonPos : candidates) {
            if (!level.hasChunkAt(addonPos)) continue;
            if (level.getBlockEntity(addonPos) instanceof MEUpgradeBlockEntity upgrade
                    && upgrade.getDock() == this) {
                result.add(upgrade);
            }
        }
        return result;
    }

    /**
     * Picks the dock an upgrade at {@code upgradePos} belongs to.
     *
     * <p>Intentionally a pure function of the machine's current addon list rather than something
     * cached on either side. Both the dock (building its multiblock group) and the upgrade
     * (building its grid connection) have to agree on the answer every time they are asked, and
     * they are asked at unrelated moments — pathing recalculation versus block ticks. A cache
     * would let the two disagree for a few ticks, and a disagreement means an upgrade outside the
     * group silently charges the network its own channel.
     */
    @Nullable
    public static MEDockBlockEntity chooseDockFor(@Nullable Level level,
            @Nullable MachineAddonController machine, BlockPos upgradePos) {
        if (level == null || machine == null) return null;

        MEDockBlockEntity best = null;
        for (var addonPos : machine.getConnectedAddons()) {
            if (!level.hasChunkAt(addonPos)) continue;
            if (!(level.getBlockEntity(addonPos) instanceof MEDockBlockEntity dock)) continue;
            if (dock.getMachine() != machine) continue;
            if (best == null || isPreferred(addonPos, best.getBlockPos(), upgradePos)) {
                best = dock;
            }
        }
        return best;
    }

    /** Nearest dock wins; ties broken by position so the choice is stable. */
    private static boolean isPreferred(BlockPos candidate, BlockPos current, BlockPos from) {
        var candidateDistance = candidate.distSqr(from);
        var currentDistance = current.distSqr(from);
        if (candidateDistance != currentDistance) return candidateDistance < currentDistance;
        return candidate.compareTo(current) < 0;
    }

    /** True when the dock has a channel and the network is powered. */
    public boolean isOnline() {
        var node = getMainNode().getNode();
        return node != null && node.isActive();
    }
}
