package com.circulation.circulation_networks.api;

import com.circulation.circulation_networks.api.node.INode;
//~ mc_imports
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public interface INodeBlockEntity {

    @Nonnull
    INode getNode();

    @Nonnull
    BlockPos getNodePos();

    //~ if >=1.20 'World ' -> 'Level ' {
    World getNodeWorld();
    //~}
}