package com.circulation.circulation_networks.packets;

import com.circulation.circulation_networks.api.hub.PermissionMode;
import com.circulation.circulation_networks.container.ContainerHub;
import com.circulation.circulation_networks.manager.HubChannelManager;
import com.circulation.circulation_networks.network.hub.HubCapabilitys;
import com.circulation.circulation_networks.utils.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

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
    public UpdateHubChannelSettings decode(FriendlyByteBuf buf) {
        UpdateHubChannelSettings msg = new UpdateHubChannelSettings();
        msg.name = buf.readUtf();
        msg.permissionModeId = buf.readByte();
        return msg;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(name == null ? "" : name);
        buf.writeByte(permissionModeId);
    }

    @Override
    public void handle(UpdateHubChannelSettings message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null) return;
            if (!(sender.containerMenu instanceof ContainerHub containerHub)) return;
            if (!containerHub.node.hasPluginCapability(HubCapabilitys.CHANNEL_CAPABILITY)) return;

            HubChannelManager.INSTANCE.updateChannelSettings(
                containerHub.node,
                sender.getUUID(),
                message.name,
                PermissionMode.fromId(message.permissionModeId)
            );
        });
        context.setPacketHandled(true);
    }
}
