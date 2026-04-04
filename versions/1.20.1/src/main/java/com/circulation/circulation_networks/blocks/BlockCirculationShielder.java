package com.circulation.circulation_networks.blocks;

import com.circulation.circulation_networks.tiles.CirculationShielderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

public final class BlockCirculationShielder extends BaseBlock {

    public BlockCirculationShielder() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0f, 6.0f).requiresCorrectToolForDrops());
    }

    @Override
    public boolean hasGui() {
        return true;
    }

    @NotNull
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new CirculationShielderBlockEntity(pos, state);
    }
}
