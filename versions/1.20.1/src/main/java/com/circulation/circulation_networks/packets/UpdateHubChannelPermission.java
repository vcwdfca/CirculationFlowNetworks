package com.circulation.circulation_networks.packets;

import com.circulation.circulation_networks.api.hub.HubPermissionLevel;
import com.circulation.circulation_networks.container.ContainerHub;
import com.circulation.circulation_networks.manager.HubChannelManager;
import com.circulation.circulation_networks.network.hub.HubCapabilitys;
import com.circulation.circulation_networks.utils.HubPlatformServices;
import com.circulation.circulation_networks.utils.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

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
    public UpdateHubChannelPermission decode(FriendlyByteBuf buf) {
        UpdateHubChannelPermission msg = new UpdateHubChannelPermission();
        msg.mostSigBits = buf.readLong();
        msg.leastSigBits = buf.readLong();
        msg.permissionId = buf.readByte();
        return msg;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeLong(mostSigBits);
        buf.writeLong(leastSigBits);
        buf.writeByte(permissionId);
    }

    @Override
    public void handle(UpdateHubChannelPermission message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null) return;
            if (!(sender.containerMenu instanceof ContainerHub containerHub)) return;
            if (!containerHub.node.hasPluginCapability(HubCapabilitys.CHANNEL_CAPABILITY)) return;

            HubChannelManager.INSTANCE.updateExplicitPermission(
                containerHub.node,
                sender.getUUID(),
                new UUID(message.mostSigBits, message.leastSigBits),
                HubPermissionLevel.fromId(message.permissionId),
                HubPlatformServices.INSTANCE.hasChannelManagementOverride(sender)
            );
        });
        context.setPacketHandled(true);
    }
}
