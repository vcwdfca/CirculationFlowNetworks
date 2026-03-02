package com.circulation.circulation_networks.manager;

import com.circulation.circulation_networks.tiles.TileEntityPhaseInterrupter;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.objects.ReferenceSets;
import lombok.Getter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class PhaseInterrupterManager {

    public static final PhaseInterrupterManager INSTANCE = new PhaseInterrupterManager();

    @Getter
    private final Int2ObjectMap<ReferenceSet<TileEntityPhaseInterrupter>> dimInterrupters = new Int2ObjectOpenHashMap<>();

    public PhaseInterrupterManager() {
        dimInterrupters.defaultReturnValue(ReferenceSets.emptySet());
    }

    public void register(TileEntityPhaseInterrupter interrupter, int dimId) {
        if (interrupter == null) return;

        ReferenceSet<TileEntityPhaseInterrupter> set = dimInterrupters.get(dimId);
        if (set == dimInterrupters.defaultReturnValue()) {
            dimInterrupters.put(dimId, set = new ReferenceOpenHashSet<>());
        }
        set.add(interrupter);
    }

    public void unregister(TileEntityPhaseInterrupter interrupter, int dimId) {
        if (interrupter == null) return;

        var interrupters = dimInterrupters.get(dimId);
        if (interrupters == null) return;
        interrupters.remove(interrupter);
    }

    public boolean isBlockedByInterrupter(BlockPos tePos, World world) {
        if (world == null || world.isRemote) return false;

        int dimId = world.provider.getDimension();
        var interrupters = dimInterrupters.get(dimId);
        if (interrupters == null || interrupters.isEmpty()) return false;

        for (TileEntityPhaseInterrupter interrupter : interrupters) {
            if (!interrupter.isActive()) continue;
            if (!interrupter.checkScope(tePos)) continue;
            return true;
        }

        return false;
    }
}
