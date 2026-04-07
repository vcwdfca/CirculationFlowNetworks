package com.circulation.circulation_networks.packets;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import com.circulation.circulation_networks.container.ContainerHub;
import com.circulation.circulation_networks.manager.HubChannelManager;
import com.circulation.circulation_networks.network.hub.HubCapabilitys;
import com.circulation.circulation_networks.utils.HubPlatformServices;
import com.circulation.circulation_networks.utils.Packet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public final class DeleteHubChannel implements Packet<DeleteHubChannel> {

    public static final Type<DeleteHubChannel> TYPE = new Type<>(
        ResourceLocation.parse(CirculationFlowNetworks.MOD_ID + ":delete_hub_channel")
    );

    public DeleteHubChannel() {
    }

    @Override
    public DeleteHubChannel decode(RegistryFriendlyByteBuf buf) {
        return new DeleteHubChannel();
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
    }

    @Override
    public void handle(DeleteHubChannel message, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer sender)) return;
        context.enqueueWork(() -> {
            if (!(sender.containerMenu instanceof ContainerHub containerHub)) return;
            if (!containerHub.node.hasPluginCapability(HubCapabilitys.CHANNEL_CAPABILITY)) return;

            HubChannelManager.INSTANCE.deleteChannel(
                containerHub.node,
                sender.getUUID(),
                HubPlatformServices.INSTANCE.hasChannelManagementOverride(sender)
            );
        });
    }

    @NotNull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
