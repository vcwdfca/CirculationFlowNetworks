package com.circulation.circulation_networks.network.nodes;

import com.circulation.circulation_networks.api.INodeBlockEntity;
import com.circulation.circulation_networks.api.node.IChargingNode;
//~ mc_imports
import net.minecraft.nbt.NBTTagCompound;

public final class ChargingNode extends Node implements IChargingNode {

    private final double chargingScope;
    private final double chargingScopeSq;

    //~ if >=1.20 'NBTTagCompound' -> 'CompoundTag' {
    //~ if >=1.20 '.setDouble(' -> '.putDouble(' {
    public ChargingNode(NBTTagCompound tag) {
        super(tag);
        this.chargingScope = tag.getDouble("chargingScope");
        this.chargingScopeSq = chargingScope * chargingScope;
    }

    public ChargingNode(INodeBlockEntity blockEntity, double chargingScope, double linkScope) {
        super(blockEntity, linkScope);
        this.chargingScope = chargingScope;
        this.chargingScopeSq = chargingScope * chargingScope;
    }

    @Override
    public double getChargingScope() {
        return chargingScope;
    }

    @Override
    public double getChargingScopeSq() {
        return chargingScopeSq;
    }

    @Override
    public NBTTagCompound serialize() {
        var nbt = super.serialize();
        nbt.setDouble("chargingScope", chargingScope);
        return nbt;
    }
    //~}
    //~}
}