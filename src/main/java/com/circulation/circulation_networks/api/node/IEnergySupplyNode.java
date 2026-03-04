package com.circulation.circulation_networks.api.node;

import net.minecraft.util.math.BlockPos;

/**
 * 标识符，确定节点可用于与设备交互能量
 */
public interface IEnergySupplyNode extends INode {

    double getEnergyScope();

    /**
     * 返回能量范围的平方，用于距离检测。<br>
     * 实现类应缓存此值以避免每次调用时重复乘法运算。
     */
    default double getEnergyScopeSq() {
        double s = getEnergyScope();
        return s * s;
    }

    default boolean supplyScopeCheck(BlockPos pos) {
        return this.distanceSq(pos) <= getEnergyScopeSq();
    }
}
