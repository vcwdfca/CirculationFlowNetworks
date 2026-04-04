package com.circulation.circulation_networks.blocks;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class BlockNodePedestal extends BaseBlock {

    public BlockNodePedestal() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0f, 6.0f).requiresCorrectToolForDrops());
    }

    @Override
    public boolean hasGui() {
        return false;
    }
}
