package com.circulation.circulation_networks.packets;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import com.circulation.circulation_networks.api.hub.PermissionMode;
import com.circulation.circulation_networks.container.ContainerHub;
import com.circulation.circulation_networks.manager.HubChannelManager;
import com.circulation.circulation_networks.network.hub.HubCapabilitys;
import com.circulation.circulation_networks.utils.Packet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public final class CreateHubChannel implements Packet<CreateHubChannel> {

    public static final Type<CreateHubChannel> TYPE = new Type<>(
        ResourceLocation.parse(CirculationFlowNetworks.MOD_ID + ":create_hub_channel")
    );

    private String name;
    private byte permissionModeId;

    public CreateHubChannel() {
    }

    public CreateHubChannel(String name, PermissionMode permissionMode) {
        this.name = name == null ? "" : name;
        this.permissionModeId = (byte) (permissionMode != null ? permissionMode.getId() : PermissionMode.PRIVATE.getId());
    }

    @Override
    public CreateHubChannel decode(RegistryFriendlyByteBuf buf) {
        CreateHubChannel msg = new CreateHubChannel();
        msg.name = buf.readUtf();
        msg.permissionModeId = buf.readByte();
        return msg;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(name == null ? "" : name);
        buf.writeByte(permissionModeId);
    }

    @Override
    public void handle(CreateHubChannel message, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer sender)) return;
        context.enqueueWork(() -> {
            if (!(sender.containerMenu instanceof ContainerHub containerHub)) return;
            if (!containerHub.node.hasPluginCapability(HubCapabilitys.CHANNEL_CAPABILITY)) return;

            HubChannelManager.INSTANCE.createChannel(
                containerHub.node,
                sender.getUUID(),
                message.name,
                PermissionMode.fromId(message.permissionModeId)
            );
        });
    }

    @NotNull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
