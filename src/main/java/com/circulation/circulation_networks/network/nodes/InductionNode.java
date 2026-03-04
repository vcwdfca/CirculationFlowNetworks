package com.circulation.circulation_networks.network.nodes;

import com.circulation.circulation_networks.api.INodeTileEntity;
import com.circulation.circulation_networks.api.node.IEnergySupplyNode;
import net.minecraft.nbt.NBTTagCompound;

public final class InductionNode extends Node implements IEnergySupplyNode {

    private final double energyScope;
    private final double energyScopeSq;

    public InductionNode(NBTTagCompound tag) {
        super(tag);
        energyScope = tag.getDouble("energyScope");
        energyScopeSq = energyScope * energyScope;
    }

    public InductionNode(INodeTileEntity tileEntity, double energyScope, double linkScope) {
        super(tileEntity, linkScope);
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
        NBTTagCompound tag = super.serialize();
        tag.setDouble("energyScope", energyScope);
        return tag;
    }
}
