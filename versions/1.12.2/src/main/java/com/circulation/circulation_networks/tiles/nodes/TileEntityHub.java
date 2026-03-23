package com.circulation.circulation_networks.tiles.nodes;

import com.circulation.circulation_networks.CFNConfig;
import com.circulation.circulation_networks.api.IHubNodeBlockEntity;
import com.circulation.circulation_networks.api.node.IHubNode;
import com.circulation.circulation_networks.container.ContainerHub;
import com.circulation.circulation_networks.gui.GuiHub;
import com.circulation.circulation_networks.items.HubChannelPluginData;
import com.circulation.circulation_networks.items.ItemHubChannelPlugin;
import com.circulation.circulation_networks.network.nodes.HubNode;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TileEntityHub extends BaseNodeTileEntity implements IHubNodeBlockEntity {

    private final IInventory plugins = new InventoryBasic("",false,5);
    private boolean init;

    @Override
    public boolean hasGui() {
        return true;
    }

    @Override
    public @NotNull IHubNode getNode() {
        return (IHubNode) super.getNode();
    }

    @Override
    public @NotNull ContainerHub getContainer(EntityPlayer player) {
        return new ContainerHub(player, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer getGui(EntityPlayer player) {
        return new GuiHub(player, this);
    }

    @Override
    protected @NotNull IHubNode createNode() {
        return new HubNode(this,
            CFNConfig.NODE.hub.energyScope,
            CFNConfig.NODE.hub.chargingScope,
            CFNConfig.NODE.hub.linkScope);
    }

    public IInventory getPlugins() {
        return plugins;
    }

    @Override
    protected void onInvalidate() {
        if (getNode() != null && getNode().getGrid() != null) {
            getNode().getGrid().setHubNode(null);
        }
        super.onInvalidate();
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        var pluginList = new NBTTagList();
        for (int i = 0; i < getPlugins().getSizeInventory(); i++) {
            var plugin = getPlugins().getStackInSlot(i);
            pluginList.appendTag(plugin.writeToNBT(new NBTTagCompound()));
        }
        compound.setTag("plugins", pluginList);
        return compound;
    }

    private transient NBTTagCompound initNbt;

    @Override
    public final void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("plugins", Constants.NBT.TAG_LIST)) {
            var pluginList = nbt.getTagList("plugins", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < Math.min(pluginList.tagCount(), getPlugins().getSizeInventory()); i++) {
                getPlugins().setInventorySlotContents(i, new ItemStack(pluginList.getCompoundTagAt(i)));
            }
        }

        if (!init) {
            initNbt = nbt;
            init = true;
        } else {
            delayedReadFromNBT(nbt);
        }
    }

    protected void onValidate() {
        super.onValidate();
        if (initNbt != null) {
            delayedReadFromNBT(initNbt);
            initNbt = null;
        }
    }

    public void delayedReadFromNBT(NBTTagCompound nbt) {
        for (int i = 0; i < getPlugins().getSizeInventory(); i++) {
            var plugin = getPlugins().getStackInSlot(i);
            if (plugin.getItem() instanceof ItemHubChannelPlugin) {
                getNode().setChannelId(HubChannelPluginData.getChannelId(plugin));
                getNode().setChannelName(HubChannelPluginData.getChannelName(plugin));
                if (HubChannelPluginData.isComplete(getNode().getChannelId(), getNode().getChannelName())) {
                    break;
                }
            }
        }
    }
}
