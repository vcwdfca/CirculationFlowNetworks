package com.circulation.circulation_networks.packets;

import com.circulation.circulation_networks.handlers.NodeNetworkRenderingHandler;
import com.circulation.circulation_networks.handlers.SpoceRenderingHandler;
import com.circulation.circulation_networks.utils.Packet;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class RenderingClear implements Packet<RenderingClear> {

    public static final RenderingClear INSTANCE = new RenderingClear();

    public RenderingClear decode(FriendlyByteBuf buf) {
        return INSTANCE;
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public void handle(RenderingClear message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (!FMLEnvironment.dist.isClient()) {
                return;
            }
            if (Minecraft.getInstance().player == null) {
                return;
            }
            NodeNetworkRenderingHandler.INSTANCE.clearLinks();
            if (SpoceRenderingHandler.INSTANCE != null) {
                SpoceRenderingHandler.INSTANCE.clear();
            }
        });
        context.setPacketHandled(true);
    }
}
