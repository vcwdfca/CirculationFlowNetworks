package com.circulation.circulation_networks.blocks.nodes;

import com.circulation.circulation_networks.registry.CFNBlockEntityTypes;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class BlockRelayNode extends PedestalRequiredNodeBlock {

    public BlockRelayNode() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0f, 6.0f).requiresCorrectToolForDrops(),
            () -> CFNBlockEntityTypes.RELAY_NODE);
    }
}
