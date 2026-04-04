package com.circulation.circulation_networks.blocks.nodes;

import com.circulation.circulation_networks.blocks.BaseBlock;
import com.circulation.circulation_networks.tiles.nodes.BaseNodeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class BaseNodeBlock extends BaseBlock {

    private final Supplier<BlockEntityType<? extends BaseNodeBlockEntity<?>>> blockEntityTypeSupplier;

    protected BaseNodeBlock(Properties properties, Supplier<BlockEntityType<? extends BaseNodeBlockEntity<?>>> blockEntityTypeSupplier) {
        super(properties);
        this.blockEntityTypeSupplier = blockEntityTypeSupplier;
    }

    @Override
    public boolean hasGui() {
        return false;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return blockEntityTypeSupplier.get().create(pos, state);
    }
}
