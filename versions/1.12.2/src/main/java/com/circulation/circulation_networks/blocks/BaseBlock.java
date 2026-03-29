package com.circulation.circulation_networks.blocks;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import com.circulation.circulation_networks.items.BaseItemTooltipModel;
import com.circulation.circulation_networks.tiles.BaseTileEntity;
import com.circulation.circulation_networks.tooltip.LocalizedComponent;
import com.circulation.circulation_networks.utils.CI18n;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.Block;
import java.util.Collections;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.circulation.circulation_networks.CirculationFlowNetworks.CREATIVE_TAB;
import static com.circulation.circulation_networks.CirculationFlowNetworks.MOD_ID;

public abstract class BaseBlock extends Block implements ITileEntityProvider {

    protected BaseBlock(String name) {
        super(Material.IRON);
        this.setRegistryName(new ResourceLocation(MOD_ID, name));
        this.setTranslationKey(MOD_ID + "." + name);
        this.setCreativeTab(CREATIVE_TAB);
    }

    public boolean hasTileEntity(@NotNull IBlockState state) {
        return false;
    }

    private String[] cachedAutoTooltipKeys;

    protected List<LocalizedComponent> buildTooltips(ItemStack stack) {
        if (cachedAutoTooltipKeys == null) {
            cachedAutoTooltipKeys = BaseItemTooltipModel.resolveTooltipKeys(getTranslationKey(), CI18n::hasKey);
        }
        if (cachedAutoTooltipKeys.length == 0) {
            return Collections.emptyList();
        }
        List<LocalizedComponent> result = new ObjectArrayList<>(cachedAutoTooltipKeys.length);
        for (var key : cachedAutoTooltipKeys) {
            result.add(LocalizedComponent.of(key));
        }
        return result;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public final void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip, @NotNull ITooltipFlag flagIn) {
        for (var lc : buildTooltips(stack)) {
            tooltip.add(lc.get());
        }
    }

    /**
     * @return 如果返回true，对应的TileEntity应该实现{@link BaseTileEntity#getContainer}与{@link BaseTileEntity#getGui}
     */
    public abstract boolean hasGui();

    @Override
    public boolean onBlockActivated(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer playerIn, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote && hasGui() && hand == EnumHand.MAIN_HAND) {
            CirculationFlowNetworks.openGui(playerIn, worldIn, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public final @Nullable BaseTileEntity createTileEntity(@NotNull World world, @NotNull IBlockState state) {
        return createNewTileEntity(world, 0);
    }

    @Override
    public @Nullable BaseTileEntity createNewTileEntity(@Nullable World world, int meta) {
        return null;
    }

}