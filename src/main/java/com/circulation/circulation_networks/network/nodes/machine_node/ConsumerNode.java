package com.circulation.circulation_networks.network.nodes.machine_node;

import com.circulation.circulation_networks.api.IEnergyHandler;
import com.circulation.circulation_networks.api.IMachineNodeBlockEntity;
//~ mc_imports
import net.minecraft.nbt.NBTTagCompound;

public class ConsumerNode extends MachineNode {

    public ConsumerNode(IMachineNodeBlockEntity blockEntity, double linkScope) {
        super(blockEntity, 0, linkScope);
    }

    //~ if >=1.20 'NBTTagCompound' -> 'CompoundTag' {
    public ConsumerNode(NBTTagCompound compound) {
        super(compound);
    }
    //~}

    @Override
    public IEnergyHandler.EnergyType getType() {
        return IEnergyHandler.EnergyType.RECEIVE;
    }
}