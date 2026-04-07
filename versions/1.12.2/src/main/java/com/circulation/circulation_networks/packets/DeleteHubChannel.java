package com.circulation.circulation_networks.packets;

import com.circulation.circulation_networks.container.ContainerHub;
import com.circulation.circulation_networks.manager.HubChannelManager;
import com.circulation.circulation_networks.network.hub.HubCapabilitys;
import com.circulation.circulation_networks.utils.HubPlatformServices;
import com.circulation.circulation_networks.utils.Packet;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.Nullable;

public final class DeleteHubChannel implements Packet<DeleteHubChannel> {

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    @Override
    public @Nullable IMessage onMessage(DeleteHubChannel message, MessageContext ctx) {
        if (!(ctx.getServerHandler().player.openContainer instanceof ContainerHub containerHub)) {
            return null;
        }
        if (!containerHub.node.hasPluginCapability(HubCapabilitys.CHANNEL_CAPABILITY)) {
            return null;
        }

        HubChannelManager.INSTANCE.deleteChannel(
            containerHub.node,
            ctx.getServerHandler().player.getUniqueID(),
            HubPlatformServices.INSTANCE.hasChannelManagementOverride(ctx.getServerHandler().player)
        );
        return null;
    }
}
