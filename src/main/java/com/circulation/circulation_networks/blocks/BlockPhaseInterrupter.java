package com.circulation.circulation_networks.blocks;

import com.circulation.circulation_networks.tiles.BaseTileEntity;
import com.circulation.circulation_networks.tiles.TileEntityPhaseInterrupter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BlockPhaseInterrupter extends BaseBlock {

    public BlockPhaseInterrupter() {
        super("phase_interrupter");
    }

    public boolean hasGui() {
        return true;
    }

    @Override
    public boolean hasTileEntity(@NotNull IBlockState state) {
        return true;
    }

    @Override
    public @NotNull BaseTileEntity createNewTileEntity(@Nullable World world, int meta) {
        return new TileEntityPhaseInterrupter();
    }
}
