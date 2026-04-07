package com.circulation.circulation_networks.packets;

import com.circulation.circulation_networks.api.hub.HubPermissionLevel;
import com.circulation.circulation_networks.container.ContainerHub;
import com.circulation.circulation_networks.manager.HubChannelManager;
import com.circulation.circulation_networks.network.hub.HubCapabilitys;
import com.circulation.circulation_networks.utils.HubPlatformServices;
import com.circulation.circulation_networks.utils.Packet;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class UpdateHubChannelPermission implements Packet<UpdateHubChannelPermission> {

    private long mostSigBits;
    private long leastSigBits;
    private byte permissionId;

    public UpdateHubChannelPermission() {
    }

    public UpdateHubChannelPermission(UUID targetPlayerId, HubPermissionLevel permissionLevel) {
        this.mostSigBits = targetPlayerId.getMostSignificantBits();
        this.leastSigBits = targetPlayerId.getLeastSignificantBits();
        this.permissionId = (byte) (permissionLevel != null ? permissionLevel.getId() : HubPermissionLevel.NONE.getId());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        mostSigBits = buf.readLong();
        leastSigBits = buf.readLong();
        permissionId = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(mostSigBits);
        buf.writeLong(leastSigBits);
        buf.writeByte(permissionId);
    }

    @Override
    public @Nullable IMessage onMessage(UpdateHubChannelPermission message, MessageContext ctx) {
        if (!(ctx.getServerHandler().player.openContainer instanceof ContainerHub containerHub)) {
            return null;
        }
        if (!containerHub.node.hasPluginCapability(HubCapabilitys.CHANNEL_CAPABILITY)) {
            return null;
        }

        HubChannelManager.INSTANCE.updateExplicitPermission(
            containerHub.node,
            ctx.getServerHandler().player.getUniqueID(),
            new UUID(message.mostSigBits, message.leastSigBits),
            HubPermissionLevel.fromId(message.permissionId),
            HubPlatformServices.INSTANCE.hasChannelManagementOverride(ctx.getServerHandler().player)
        );
        return null;
    }
}
