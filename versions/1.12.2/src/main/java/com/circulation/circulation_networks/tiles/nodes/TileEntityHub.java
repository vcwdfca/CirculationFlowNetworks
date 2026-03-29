package com.circulation.circulation_networks.tiles.nodes;

import com.circulation.circulation_networks.CFNConfig;
import com.circulation.circulation_networks.api.IHubNodeBlockEntity;
import com.circulation.circulation_networks.api.hub.IHubPlugin;
import com.circulation.circulation_networks.api.node.IHubNode;
import com.circulation.circulation_networks.container.ContainerHub;
import com.circulation.circulation_networks.gui.GuiHub;
import com.circulation.circulation_networks.inventory.CFNInternalInventory;
import com.circulation.circulation_networks.inventory.CFNInternalInventoryHost;
import com.circulation.circulation_networks.inventory.CFNInventoryChangeOperation;
import com.circulation.circulation_networks.items.HubChannelPluginData;
import com.circulation.circulation_networks.items.ItemHubChannelPlugin;
import com.circulation.circulation_networks.network.nodes.HubNode;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TileEntityHub extends BaseNodeTileEntity implements IHubNodeBlockEntity, CFNInternalInventoryHost {

    private final CFNInternalInventory plugins = new CFNInternalInventory(this, 5, 1).setInputFilter((inventory, slot, itemStack) -> {
        if (!(itemStack.getItem() instanceof IHubPlugin)) {
            return false;
        }
        return isUniquePluginType(inventory, slot, itemStack);
    });
    private boolean init;
    private transient NBTTagCompound initNbt;

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

    public CFNInternalInventory getPlugins() {
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
        plugins.writeToNBT(compound, "plugins");
        return compound;
    }

    @Override
    public final void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        plugins.readFromNBT(nbt, "plugins");

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
        refreshHubChannel();
    }

    @Override
    public void onChangeInventory(CFNInternalInventory inventory, int slot, CFNInventoryChangeOperation operation, ItemStack oldStack, ItemStack newStack) {
        if (init) {
            updateHubChannel(slot, oldStack, newStack);
        }
        markDirty();
    }

    private void updateHubChannel(int slot, ItemStack oldStack, ItemStack newStack) {
        boolean hasHigherPriorityChannel = hasCompleteChannelPluginBefore(slot);
        boolean oldWasActiveChannel = isCompleteChannelPlugin(oldStack) && !hasHigherPriorityChannel;
        boolean newBecomesActiveChannel = isCompleteChannelPlugin(newStack) && !hasHigherPriorityChannel;

        if (oldWasActiveChannel) {
            HubChannelPluginData.setChannelInfo(oldStack, getNode().getChannelId(), getNode().getChannelName());
        }

        if (newBecomesActiveChannel) {
            HubChannelPluginData.applyToHub(getNode(), newStack);
            return;
        }

        if (oldWasActiveChannel) {
            applyFirstCompleteChannelPluginAfter(slot + 1);
        }
    }

    private void refreshHubChannel() {
        applyFirstCompleteChannelPluginAfter(0);
    }

    private boolean hasCompleteChannelPluginBefore(int slot) {
        for (int i = 0; i < slot; i++) {
            if (isCompleteChannelPlugin(plugins.getStackInSlot(i))) {
                return true;
            }
        }
        return false;
    }

    private void applyFirstCompleteChannelPluginAfter(int startSlot) {
        HubChannelPluginData.clearHub(getNode());
        for (int i = startSlot; i < plugins.getSlots(); i++) {
            ItemStack plugin = plugins.getStackInSlot(i);
            if (isCompleteChannelPlugin(plugin)) {
                HubChannelPluginData.applyToHub(getNode(), plugin);
                return;
            }
        }
    }

    private static boolean isCompleteChannelPlugin(ItemStack stack) {
        return stack.getItem() instanceof ItemHubChannelPlugin
            && HubChannelPluginData.isComplete(HubChannelPluginData.getChannelId(stack), HubChannelPluginData.getChannelName(stack));
    }

    private static boolean isUniquePluginType(CFNInternalInventory inventory, int slot, ItemStack stack) {
        Class<?> pluginClass = stack.getItem().getClass();
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (i == slot) {
                continue;
            }

            ItemStack existing = inventory.getStackInSlot(i);
            if (existing.getItem().getClass() == pluginClass) {
                return false;
            }
        }
        return true;
    }
}
