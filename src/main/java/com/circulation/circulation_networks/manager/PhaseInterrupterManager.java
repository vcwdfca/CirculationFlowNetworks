package com.circulation.circulation_networks.manager;

import com.circulation.circulation_networks.api.IPhaseInterrupterBlockEntity;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.objects.ReferenceSets;
//~ mc_imports
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class PhaseInterrupterManager {

    public static final PhaseInterrupterManager INSTANCE = new PhaseInterrupterManager();

    private final Int2ObjectMap<ReferenceSet<IPhaseInterrupterBlockEntity>> dimInterrupters = new Int2ObjectOpenHashMap<>();

    public PhaseInterrupterManager() {
        dimInterrupters.defaultReturnValue(ReferenceSets.emptySet());
    }

    public Int2ObjectMap<ReferenceSet<IPhaseInterrupterBlockEntity>> getDimInterrupters() {
        return dimInterrupters;
    }

    public ReferenceSet<IPhaseInterrupterBlockEntity> getInterruptersForDim(int dimId) {
        return dimInterrupters.get(dimId);
    }

    public void register(IPhaseInterrupterBlockEntity interrupter, int dimId) {
        if (interrupter == null) return;

        ReferenceSet<IPhaseInterrupterBlockEntity> set = dimInterrupters.get(dimId);
        if (set == dimInterrupters.defaultReturnValue()) {
            dimInterrupters.put(dimId, set = new ReferenceOpenHashSet<>());
        }
        set.add(interrupter);
    }

    public void unregister(IPhaseInterrupterBlockEntity interrupter, int dimId) {
        if (interrupter == null) return;

        var interrupters = dimInterrupters.get(dimId);
        if (interrupters == null) return;
        interrupters.remove(interrupter);
    }

    //~ if >=1.20 ' World ' -> ' Level ' {
    public boolean isBlockedByInterrupter(BlockPos tePos, World world) {
    //~}
        if (world == null || isClientWorld(world)) return false;
        int dimId = getDimensionId(world);

        var interrupters = dimInterrupters.get(dimId);
        if (interrupters == null || interrupters.isEmpty()) return false;

        for (IPhaseInterrupterBlockEntity interrupter : interrupters) {
            if (!interrupter.isActive()) continue;
            if (!interrupter.checkScope(tePos)) continue;
            return true;
        }

        return false;
    }

    //~ if >=1.20 '(World ' -> '(Level ' {
    //~ if >=1.20 '.isRemote' -> '.isClientSide' {
    //~ if >=1.20 '.provider.getDimension()' -> '.dimension().location().hashCode()' {
    private static boolean isClientWorld(World world) {
        return world.isRemote;
    }

    private static int getDimensionId(World world) {
        return world.provider.getDimension();
    }
    //~}
    //~}
    //~}
}