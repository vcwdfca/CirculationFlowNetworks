package com.circulation.circulation_networks.energy.manager;

import com.circulation.circulation_networks.api.IEnergyHandler;
import com.circulation.circulation_networks.api.IEnergyHandlerManager;
import com.circulation.circulation_networks.energy.handler.MEKHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class MEKHandlerManager implements IEnergyHandlerManager {

    private static final Direction[] DIRECTIONS = Direction.values();

    @Override
    public boolean isAvailable(BlockEntity blockEntity) {
        var level = blockEntity.getLevel();
        if (level == null) {
            return false;
        }
        var pos = blockEntity.getBlockPos();
        for (Direction direction : DIRECTIONS) {
            if (level.getCapability(Capabilities.STRICT_ENERGY.block(), pos, direction) != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAvailable(ItemStack itemStack) {
        return itemStack.getCapability(Capabilities.STRICT_ENERGY.item()) != null;
    }

    @Override
    public Class<MEKHandler> getEnergyHandlerClass() {
        return MEKHandler.class;
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public IEnergyHandler newBlockEntityInstance() {
        return new MEKHandler();
    }

    @Override
    public IEnergyHandler newItemInstance() {
        return new MEKHandler();
    }

    @Override
    public String getUnit() {
        return "J";
    }

    @Override
    public double getMultiplying() {
        return 0.4D;
    }
}
