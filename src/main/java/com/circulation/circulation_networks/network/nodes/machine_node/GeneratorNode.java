package com.circulation.circulation_networks.network.nodes.machine_node;

import com.circulation.circulation_networks.api.IEnergyHandler;
import com.circulation.circulation_networks.api.IMachineNodeBlockEntity;
//~ mc_imports
import net.minecraft.nbt.NBTTagCompound;

public final class GeneratorNode extends MachineNode {

    public GeneratorNode(IMachineNodeBlockEntity blockEntity, double energyScope, double linkScope) {
        super(blockEntity, energyScope, linkScope);
    }

    //~ if >=1.20 'NBTTagCompound' -> 'CompoundTag' {
    public GeneratorNode(NBTTagCompound tag) {
        super(tag);
    }
    //~}

    @Override
    public IEnergyHandler.EnergyType getType() {
        return IEnergyHandler.EnergyType.SEND;
    }
}