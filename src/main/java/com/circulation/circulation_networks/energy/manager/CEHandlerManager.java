package com.circulation.circulation_networks.energy.manager;

import com.circulation.circulation_networks.api.IEnergyHandler;
import com.circulation.circulation_networks.api.IEnergyHandlerManager;
import com.circulation.circulation_networks.api.IMachineNodeBlockEntity;
import com.circulation.circulation_networks.energy.handler.CEHandler;
//~ mc_imports
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public final class CEHandlerManager implements IEnergyHandlerManager {

    //~ if >=1.20 '(TileEntity ' -> '(BlockEntity ' {
    //~ if >=1.20 ' TileEntity ' -> ' BlockEntity ' {
    @Override
    public boolean isAvailable(TileEntity tileEntity) {
        return tileEntity instanceof IMachineNodeBlockEntity;
    }

    @Override
    public boolean isAvailable(ItemStack itemStack) {
        return false;
    }

    @Override
    public Class<CEHandler> getEnergyHandlerClass() {
        return CEHandler.class;
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public IEnergyHandler newInstance(TileEntity tileEntity) {
        return ((IMachineNodeBlockEntity) tileEntity).getEnergyHandler();
    }

    @Override
    public IEnergyHandler newInstance(ItemStack itemStack) {
        return null;
    }

    @Override
    public String getUnit() {
        return "CE";
    }
    //~}
    //~}
}
