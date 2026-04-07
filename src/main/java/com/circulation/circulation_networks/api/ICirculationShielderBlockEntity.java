package com.circulation.circulation_networks.api;

//~ mc_imports

import net.minecraft.util.math.BlockPos;

public interface ICirculationShielderBlockEntity {

    boolean checkScope(BlockPos pos);

    boolean isActive();

    int getScope();

    boolean isShowingRange();

    BlockPos getBEPos();
}
