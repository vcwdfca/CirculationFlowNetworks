package com.circulation.circulation_networks.tiles;

import com.circulation.circulation_networks.container.CFNBaseContainer;
import com.circulation.circulation_networks.container.ContainerPhaseInterrupter;
import com.circulation.circulation_networks.gui.GuiPhaseInterrupter;
import com.circulation.circulation_networks.manager.PhaseInterrupterManager;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class TileEntityPhaseInterrupter extends BaseTileEntity {

    private transient final BlockPos.MutableBlockPos min = new BlockPos.MutableBlockPos();
    private transient final BlockPos.MutableBlockPos max = new BlockPos.MutableBlockPos();
    @Getter
    private int scope;
    private boolean redstoneMode = false;
    @Setter
    @Getter
    private boolean showingRange = false;

    @Override
    public boolean hasGui() {
        return true;
    }


    @Override
    @NotNull
    public CFNBaseContainer getContainer(EntityPlayer player) {
        return new ContainerPhaseInterrupter(player, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer getGui(EntityPlayer player) {
        return new GuiPhaseInterrupter(player, this);
    }

    public void setScope(int scope) {
        this.min.setPos(this.getPos().getX() - scope, this.getPos().getY() - scope, this.getPos().getZ() - scope);
        this.max.setPos(this.getPos().getX() + scope, this.getPos().getY() + scope, this.getPos().getZ() + scope);
        this.scope = scope;
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("scope", this.scope);
        compound.setBoolean("RedstoneMode", this.redstoneMode);
        return compound;
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound compound) {
        super.readFromNBT(compound);
        setScope(compound.getInteger("scope"));
        this.redstoneMode = compound.getBoolean("RedstoneMode");
    }

    public boolean checkScope(BlockPos pos) {
        return min.getX() <= pos.getX() && min.getY() <= pos.getY() && min.getZ() <= pos.getZ()
            && max.getX() >= pos.getX() && max.getY() >= pos.getY() && max.getZ() >= pos.getZ();
    }

    public boolean isActive() {
        int redstoneState = world.isBlockPowered(pos) ? 1 : 0;
        if (redstoneMode) {
            return redstoneState == 1;
        } else {
            return redstoneState == 0;
        }
    }

    public void toggleRedstoneMode() {
        this.redstoneMode = !this.redstoneMode;
        markDirty();
    }

    public boolean getRedstoneMode() {
        return redstoneMode;
    }

    public void setRedstoneMode(boolean mode) {
        this.redstoneMode = mode;
        markDirty();
    }

    @Override
    public void validate() {
        super.validate();
        if (!world.isRemote) {
            PhaseInterrupterManager.INSTANCE.register(this, world.provider.getDimension());
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (world != null && !world.isRemote) {
            PhaseInterrupterManager.INSTANCE.unregister(this, world.provider.getDimension());
        }
    }

}
