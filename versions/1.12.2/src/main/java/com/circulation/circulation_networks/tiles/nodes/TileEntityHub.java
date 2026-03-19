package com.circulation.circulation_networks.tiles.nodes;

import com.circulation.circulation_networks.CFNConfig;
import com.circulation.circulation_networks.api.node.IHubNode;
import com.circulation.circulation_networks.api.node.INode;
import com.circulation.circulation_networks.container.ContainerHub;
import com.circulation.circulation_networks.gui.GuiHub;
import com.circulation.circulation_networks.network.nodes.HubNode;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class TileEntityHub extends BaseNodeTileEntity {

    @Override
    public boolean hasGui() {
        return true;
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
    protected @NotNull INode createNode() {
        return new HubNode(this,
            CFNConfig.NODE.hub.energyScope,
            CFNConfig.NODE.hub.chargingScope,
            CFNConfig.NODE.hub.linkScope);
    }

    private @Nullable IHubNode getHubNode() {
        var node = getNode();
        return node instanceof IHubNode hub ? hub : null;
    }

    public ItemStack[] getPlugins() {
        var hub = getHubNode();
        return hub != null ? hub.getPlugins() : new ItemStack[IHubNode.PLUGIN_SLOTS];
    }

    @Override
    protected void onInvalidate() {
        if (getNode() != null && getNode().getGrid() != null) {
            getNode().getGrid().setHubNode(null);
        }
        super.onInvalidate();
    }
}
