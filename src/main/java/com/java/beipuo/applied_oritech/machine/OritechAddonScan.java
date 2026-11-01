package com.java.beipuo.applied_oritech.machine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import rearth.oritech.block.blocks.addons.MachineAddonBlock;
import rearth.oritech.block.entity.addons.AddonBlockEntity;
import rearth.oritech.block.entity.addons.CombiAddonEntity;
import rearth.oritech.init.OritechConfig;
import rearth.oritech.util.Geometry;
import rearth.oritech.util.MachineAddonController;

import com.java.beipuo.applied_oritech.blockentity.AOGridAddonBlockEntity;

public final class OritechAddonScan {
    private OritechAddonScan() {
    }

    @Nullable
    public static List<MachineAddonController.AddonBlock> withHeart(MachineAddonController machine,
            @Nullable BlockPos brokenAddon) {
        var level = machine.getWorldForAddon();
        if (level == null) return null;
        var controllerPos = machine.getPosForAddon();
        var queued = new LinkedHashSet<BlockPos>();
        for (var offset : machine.getAddonSlots()) {
            queued.add((BlockPos) Geometry.offsetToWorldPosition(machine.getFacingForAddon(), offset, controllerPos));
        }
        var visited = new HashSet<BlockPos>();
        var open = new ArrayList<BlockPos>();
        var found = new ArrayList<MachineAddonController.AddonBlock>();
        var iterations = (int) machine.getCoreQuality() + 1;
        var layered = OritechConfig.layeredExtenders.get();
        var extenders = 0;
        for (int depth = 0; depth < iterations && !queued.isEmpty(); depth++) {
            var next = new LinkedHashSet<BlockPos>();
            for (var pos : queued) {
                if (!visited.add(pos) || !level.hasChunkAt(pos)) continue;
                if (pos.equals(brokenAddon)) {
                    open.add(pos);
                    continue;
                }
                var state = level.getBlockState(pos);
                if (!(state.getBlock() instanceof MachineAddonBlock block)
                        || !(level.getBlockEntity(pos) instanceof AddonBlockEntity entity)) {
                    if (!pos.equals(controllerPos)) open.add(pos);
                    continue;
                }
                if (state.getValue(MachineAddonBlock.ADDON_USED)
                        && !entity.getControllerPos().equals(controllerPos)
                        && level.getBlockEntity(entity.getControllerPos()) instanceof MachineAddonController) {
                    open.add(pos);
                    continue;
                }
                if (block.getAddonSettings().extender() && !layered && extenders++ >= iterations - 1) continue;
                found.add(new MachineAddonController.AddonBlock(block, state, pos, entity));
                if (block.getAddonSettings().extender()) {
                    for (var direction : Direction.values()) {
                        var neighbor = pos.relative(direction);
                        if (!visited.contains(neighbor)) next.add(neighbor);
                    }
                }
            }
            queued = next;
        }
        if (found.stream().noneMatch(addon -> addon.addonEntity() instanceof AOGridAddonBlockEntity)
                || found.stream().noneMatch(addon -> addon.addonEntity() instanceof CombiAddonEntity)) return null;
        var heart = found.stream().filter(addon -> addon.addonEntity() instanceof CombiAddonEntity)
                .findFirst().orElseThrow();
        found.removeIf(addon -> addon != heart
                && !(addon.addonEntity() instanceof AOGridAddonBlockEntity)
                && !addon.addonBlock().getAddonSettings().extender());
        machine.getOpenAddonSlots().clear();
        machine.getOpenAddonSlots().addAll(open);
        return found;
    }
}
