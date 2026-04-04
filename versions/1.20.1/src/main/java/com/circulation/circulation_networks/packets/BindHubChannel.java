package com.circulation.circulation_networks.packets;

import com.circulation.circulation_networks.container.ContainerHub;
import com.circulation.circulation_networks.manager.HubChannelManager;
import com.circulation.circulation_networks.network.hub.HubCapabilitys;
import com.circulation.circulation_networks.utils.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public final class BindHubChannel implements Packet<BindHubChannel> {

    private long mostSigBits;
    private long leastSigBits;

    public BindHubChannel() {
    }

    public BindHubChannel(UUID channelId) {
        this.mostSigBits = channelId.getMostSignificantBits();
        this.leastSigBits = channelId.getLeastSignificantBits();
    }

    @Override
    public BindHubChannel decode(FriendlyByteBuf buf) {
        BindHubChannel msg = new BindHubChannel();
        msg.mostSigBits = buf.readLong();
        msg.leastSigBits = buf.readLong();
        return msg;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeLong(mostSigBits);
        buf.writeLong(leastSigBits);
    }

    @Override
    public void handle(BindHubChannel message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null) return;
            if (!(sender.containerMenu instanceof ContainerHub containerHub)) return;
            if (!containerHub.node.hasPluginCapability(HubCapabilitys.CHANNEL_CAPABILITY)) return;

            HubChannelManager.INSTANCE.bindHubToChannel(
                containerHub.node,
                sender.getUUID(),
                new UUID(message.mostSigBits, message.leastSigBits)
            );
        });
        context.setPacketHandled(true);
    }
}
