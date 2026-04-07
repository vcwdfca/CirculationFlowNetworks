package com.circulation.circulation_networks.packets;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import com.circulation.circulation_networks.api.hub.HubPermissionLevel;
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

import java.util.UUID;

public final class UpdateHubChannelPermission implements Packet<UpdateHubChannelPermission> {

    public static final Type<UpdateHubChannelPermission> TYPE = new Type<>(
        ResourceLocation.parse(CirculationFlowNetworks.MOD_ID + ":update_hub_channel_permission")
    );

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
    public UpdateHubChannelPermission decode(RegistryFriendlyByteBuf buf) {
        UpdateHubChannelPermission msg = new UpdateHubChannelPermission();
        msg.mostSigBits = buf.readLong();
        msg.leastSigBits = buf.readLong();
        msg.permissionId = buf.readByte();
        return msg;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeLong(mostSigBits);
        buf.writeLong(leastSigBits);
        buf.writeByte(permissionId);
    }

    @Override
    public void handle(UpdateHubChannelPermission message, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer sender)) return;
        context.enqueueWork(() -> {
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
    }

    @NotNull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
