package com.circulation.circulation_networks.manager;

import com.circulation.circulation_networks.api.ClientTickMachine;
import com.circulation.circulation_networks.api.ServerTickMachine;
import com.circulation.circulation_networks.events.BlockEntityLifeCycleEvent;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;

public class MachineNodeBlockEntityManager {

    public static final MachineNodeBlockEntityManager INSTANCE = new MachineNodeBlockEntityManager();

    private final ReferenceSet<ServerTickMachine> serverTe = new ReferenceLinkedOpenHashSet<>();
    private final ReferenceSet<ClientTickMachine> clientTe = new ReferenceLinkedOpenHashSet<>();

    public void onBlockEntityValidate(BlockEntityLifeCycleEvent.Validate event) {
        if (isClientWorld(event.getWorld())) {
            if (event.getBlockEntity() instanceof ClientTickMachine te) clientTe.add(te);
        } else if (event.getBlockEntity() instanceof ServerTickMachine te) serverTe.add(te);
    }

    public void onBlockEntityInvalidate(BlockEntityLifeCycleEvent.Invalidate event) {
        if (isClientWorld(event.getWorld())) {
            if (event.getBlockEntity() instanceof ClientTickMachine te) clientTe.remove(te);
        } else if (event.getBlockEntity() instanceof ServerTickMachine te) serverTe.remove(te);
    }

    public void onClientTick() {
        for (var machine : clientTe) {
            machine.clientUpdate();
        }
    }

    public void onServerTick() {
        for (var machine : serverTe) {
            machine.serverUpdate();
        }
    }

    public void clear() {
        serverTe.clear();
        clientTe.clear();
    }

    //~ if >=1.20 'net.minecraft.world.World' -> 'net.minecraft.world.level.Level' {
    //~ if >=1.20 '.isRemote' -> '.isClientSide' {
    private static boolean isClientWorld(net.minecraft.world.World world) {
        return world.isRemote;
    }
    //~}
    //~}
}