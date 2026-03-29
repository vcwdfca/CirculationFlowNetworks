package com.circulation.circulation_networks.api.node;

import com.circulation.circulation_networks.registry.RegistryEnergyHandler;
//~ mc_imports
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

/**
 * 标识符，确定节点可用于与设备交互能量
 */
public interface IEnergySupplyNode extends INode {

    double getEnergyScope();

    double getEnergyScopeSq();

    default boolean supplyScopeCheck(BlockPos pos) {
        return this.distanceSq(pos) <= getEnergyScopeSq();
    }

    //~ if >=1.20 '(TileEntity ' -> '(BlockEntity ' {
    default boolean isBlacklisted(TileEntity blockEntity) {
        return RegistryEnergyHandler.isSupplyBlack(blockEntity);
    }
    //~}
}
