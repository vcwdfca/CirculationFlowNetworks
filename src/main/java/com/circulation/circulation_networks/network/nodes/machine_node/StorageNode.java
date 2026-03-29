package com.circulation.circulation_networks.network.nodes.machine_node;

import com.circulation.circulation_networks.api.IEnergyHandler;
import com.circulation.circulation_networks.api.IMachineNodeBlockEntity;
//~ mc_imports
import net.minecraft.nbt.NBTTagCompound;

public final class StorageNode extends MachineNode {

    public StorageNode(IMachineNodeBlockEntity blockEntity, double energyScope, double linkScope) {
        super(blockEntity, energyScope, linkScope);
    }

    //~ if >=1.20 'NBTTagCompound' -> 'CompoundTag' {
    public StorageNode(NBTTagCompound tag) {
        super(tag);
    }
    //~}

    @Override
    public IEnergyHandler.EnergyType getType() {
        return IEnergyHandler.EnergyType.STORAGE;
    }
}