package com.circulation.circulation_networks.energy.handler;

import com.circulation.circulation_networks.api.EnergyAmount;
import com.circulation.circulation_networks.api.EnergyAmounts;
import com.circulation.circulation_networks.api.IEnergyHandler;
import com.circulation.circulation_networks.api.IMachineNodeBlockEntity;
import com.circulation.circulation_networks.utils.CirculationEnergy;
//~ mc_imports
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import java.math.BigInteger;

public final class CEHandler implements IEnergyHandler {

    //~ if >=1.20 '(TileEntity ' -> '(BlockEntity ' {
    //~ if >=1.20 ' TileEntity ' -> ' BlockEntity ' {
    private final EnergyType type;
    @Nonnull
    private final CirculationEnergy energy;

    public CEHandler(IMachineNodeBlockEntity tileEntity) {
        this.type = tileEntity.getNode().getType();
        var energy = CirculationEnergy.create(tileEntity);
        if (energy == null) throw new IllegalStateException("energy is null");
        this.energy = energy;
    }

    @Override
    public IEnergyHandler init(TileEntity tileEntity) {
        return this;
    }

    @Override
    public IEnergyHandler init(ItemStack itemStack) {
        return this;
    }

    @Override
    public void clear() {

    }

    @Override
    public EnergyAmount receiveEnergy(EnergyAmount maxReceive) {
        return energy.receiveEnergy(maxReceive, false);
    }

    @Override
    public EnergyAmount extractEnergy(EnergyAmount maxExtract) {
        return energy.extractEnergy(maxExtract, false);
    }

    @Override
    public EnergyAmount canExtractValue() {
        if (type == EnergyType.RECEIVE) return EnergyAmounts.ZERO;
        return energy.canExtractValue();
    }

    @Override
    public EnergyAmount canReceiveValue() {
        if (type == EnergyType.SEND) return EnergyAmounts.ZERO;
        return energy.canReceiveValue();
    }

    @Override
    public boolean canExtract(IEnergyHandler receiveHandler) {
        if (type == EnergyType.RECEIVE) return false;
        EnergyAmount amount = canExtractValue();
        try {
            return amount.isPositive();
        } finally {
            amount.recycle();
        }
    }

    @Override
    public boolean canReceive(IEnergyHandler sendHandler) {
        if (type == EnergyType.SEND) return false;
        EnergyAmount amount = canReceiveValue();
        try {
            return amount.isPositive();
        } finally {
            amount.recycle();
        }
    }

    @Override
    public EnergyType getType() {
        return type;
    }

    public @Nonnull CirculationEnergy getEnergy() {
        return energy;
    }

    @Override
    public void recycle() {
    }

    //~}
    //~}
    //~ if >=1.20 'NBTTagCompound' -> 'CompoundTag' {
    //~ if >=1.20 '.setLong(' -> '.putLong(' {
    //~ if >=1.20 '.removeTag(' -> '.remove(' {
    //~ if >=1.20 '.setString(' -> '.putString(' {
    //~ if >=1.20 '.hasKey(' -> '.contains(' {
    public void writeToNBT(NBTTagCompound nbt) {
        EnergyAmount current = energy.canExtractValue();
        try {
            nbt.setLong("energy", current.asLongClamped());
            if (current.fitsLong()) {
                nbt.removeTag("energyBig");
            } else {
                nbt.setString("energyBig", current.toString());
            }
        } finally {
            current.recycle();
        }
    }

    public void readNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("energyBig")) {
            energy.setEnergy(new BigInteger(nbt.getString("energyBig")));
        } else {
            energy.setEnergy(nbt.getLong("energy"));
        }
    }
    //~}
    //~}
    //~}
    //~}
    //~}
}
