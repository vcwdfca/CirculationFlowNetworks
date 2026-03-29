package com.circulation.circulation_networks.packets;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import com.circulation.circulation_networks.handlers.NodeNetworkRenderingHandler;
import com.circulation.circulation_networks.handlers.SpoceRenderingHandler;
import com.circulation.circulation_networks.utils.Packet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public final class RenderingClear implements Packet<RenderingClear> {

    public static final RenderingClear INSTANCE = new RenderingClear();
    public static final Type<RenderingClear> TYPE = new Type<>(
        ResourceLocation.parse(CirculationFlowNetworks.MOD_ID + ":rendering_clear")
    );

    public RenderingClear decode(RegistryFriendlyByteBuf buf) {
        return INSTANCE;
    }

    public void encode(RegistryFriendlyByteBuf buf) {
    }

    public void handle(RenderingClear message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (net.minecraft.client.Minecraft.getInstance().player == null) {
                return;
            }
            NodeNetworkRenderingHandler.INSTANCE.clearLinks();
            if (SpoceRenderingHandler.INSTANCE != null) {
                SpoceRenderingHandler.INSTANCE.clear();
            }
        });
    }

    @NotNull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
