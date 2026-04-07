package com.circulation.circulation_networks.packets;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import com.circulation.circulation_networks.container.ContainerHub;
import com.circulation.circulation_networks.manager.HubChannelManager;
import com.circulation.circulation_networks.network.hub.HubCapabilitys;
import com.circulation.circulation_networks.utils.Packet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class BindHubChannel implements Packet<BindHubChannel> {

    public static final Type<BindHubChannel> TYPE = new Type<>(
        ResourceLocation.parse(CirculationFlowNetworks.MOD_ID + ":bind_hub_channel")
    );

    private long mostSigBits;
    private long leastSigBits;

    public BindHubChannel() {
    }

    public BindHubChannel(UUID channelId) {
        this.mostSigBits = channelId.getMostSignificantBits();
        this.leastSigBits = channelId.getLeastSignificantBits();
    }

    @Override
    public BindHubChannel decode(RegistryFriendlyByteBuf buf) {
        BindHubChannel msg = new BindHubChannel();
        msg.mostSigBits = buf.readLong();
        msg.leastSigBits = buf.readLong();
        return msg;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeLong(mostSigBits);
        buf.writeLong(leastSigBits);
    }

    @Override
    public void handle(BindHubChannel message, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer sender)) return;
        context.enqueueWork(() -> {
            if (!(sender.containerMenu instanceof ContainerHub containerHub)) return;
            if (!containerHub.node.hasPluginCapability(HubCapabilitys.CHANNEL_CAPABILITY)) return;

            HubChannelManager.INSTANCE.bindHubToChannel(
                containerHub.node,
                sender.getUUID(),
                new UUID(message.mostSigBits, message.leastSigBits)
            );
        });
    }

    @NotNull
    @Override
    public Type<BindHubChannel> type() {
        return TYPE;
    }
}
