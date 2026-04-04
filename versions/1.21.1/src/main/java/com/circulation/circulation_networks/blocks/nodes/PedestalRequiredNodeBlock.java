package com.circulation.circulation_networks.blocks.nodes;

import com.circulation.circulation_networks.blocks.BlockNodePedestal;
import com.circulation.circulation_networks.tiles.nodes.BaseNodeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class PedestalRequiredNodeBlock extends BaseNodeBlock {

    protected PedestalRequiredNodeBlock(Properties properties, Supplier<BlockEntityType<? extends BaseNodeBlockEntity<?>>> blockEntityTypeSupplier) {
        super(properties, blockEntityTypeSupplier);
    }

    @Override
    public boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
        BlockPos below = pos.below();
        BlockState stateBelow = level.getBlockState(below);
        return stateBelow.getBlock() instanceof BlockNodePedestal && super.canSurvive(state, level, pos);
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                @NotNull Block block, @NotNull BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide() && !(level.getBlockState(pos.below()).getBlock() instanceof BlockNodePedestal)) {
            level.destroyBlock(pos, true);
        }
    }
}
