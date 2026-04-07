package com.circulation.circulation_networks.packets;

import com.circulation.circulation_networks.api.hub.PermissionMode;
import com.circulation.circulation_networks.container.ContainerHub;
import com.circulation.circulation_networks.manager.HubChannelManager;
import com.circulation.circulation_networks.network.hub.HubCapabilitys;
import com.circulation.circulation_networks.utils.HubPlatformServices;
import com.circulation.circulation_networks.utils.Packet;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.Nullable;

public final class UpdateHubChannelSettings implements Packet<UpdateHubChannelSettings> {

    private String name;
    private byte permissionModeId;

    public UpdateHubChannelSettings() {
    }

    public UpdateHubChannelSettings(String name, PermissionMode permissionMode) {
        this.name = name == null ? "" : name;
        this.permissionModeId = (byte) (permissionMode != null ? permissionMode.getId() : PermissionMode.PRIVATE.getId());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        name = ByteBufUtils.readUTF8String(buf);
        permissionModeId = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, name == null ? "" : name);
        buf.writeByte(permissionModeId);
    }

    @Override
    public @Nullable IMessage onMessage(UpdateHubChannelSettings message, MessageContext ctx) {
        if (!(ctx.getServerHandler().player.openContainer instanceof ContainerHub containerHub)) {
            return null;
        }
        if (!containerHub.node.hasPluginCapability(HubCapabilitys.CHANNEL_CAPABILITY)) {
            return null;
        }

        HubChannelManager.INSTANCE.updateChannelSettings(
            containerHub.node,
            ctx.getServerHandler().player.getUniqueID(),
            message.name,
            PermissionMode.fromId(message.permissionModeId),
            HubPlatformServices.INSTANCE.hasChannelManagementOverride(ctx.getServerHandler().player)
        );
        return null;
    }
}
