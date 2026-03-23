package com.circulation.circulation_networks.blocks.nodes;

import com.circulation.circulation_networks.tiles.nodes.TileEntityHub;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public final class BlockHub extends BaseNodeBlock {

    public BlockHub() {
        super("hub");
        this.setNodeTileClass(TileEntityHub.class);
    }

    @Override
    public boolean hasGui() {
        return true;
    }

    @Override
    public void breakBlock(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state) {
        var te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityHub hub) {
            var inv = hub.getPlugins();
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                var plugin = inv.getStackInSlot(i);
                if (!plugin.isEmpty()) {
                    worldIn.spawnEntity(new EntityItem(worldIn,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        plugin.copy()));
                }
            }
        }
        super.breakBlock(worldIn, pos, state);
    }
}
