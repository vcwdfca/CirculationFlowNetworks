package com.circulation.circulation_networks.network.nodes;

import com.circulation.circulation_networks.api.INodeBlockEntity;
import com.circulation.circulation_networks.api.node.IEnergySupplyNode;
//~ mc_imports
import net.minecraft.nbt.NBTTagCompound;

public final class InductionNode extends Node implements IEnergySupplyNode {

    private final double energyScope;
    private final double energyScopeSq;

    //~ if >=1.20 'NBTTagCompound' -> 'CompoundTag' {
    //~ if >=1.20 '.set' -> '.put' {
    public InductionNode(NBTTagCompound tag) {
        super(tag);
        energyScope = tag.getDouble("energyScope");
        energyScopeSq = energyScope * energyScope;
    }

    public InductionNode(INodeBlockEntity blockEntity, double energyScope, double linkScope) {
        super(blockEntity, linkScope);
        this.energyScope = energyScope;
        this.energyScopeSq = energyScope * energyScope;
    }

    @Override
    public double getEnergyScope() {
        return energyScope;
    }

    @Override
    public double getEnergyScopeSq() {
        return energyScopeSq;
    }

    @Override
    public NBTTagCompound serialize() {
        var tag = super.serialize();
        tag.setDouble("energyScope", energyScope);
        return tag;
    }
    //~}
    //~}
}