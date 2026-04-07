package com.circulation.circulation_networks.packets;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import com.circulation.circulation_networks.handlers.SpoceRenderingHandler;
import com.circulation.circulation_networks.utils.Packet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public final class SpoceRendering implements Packet<SpoceRendering> {

    public static final Type<SpoceRendering> TYPE = new Type<>(
        ResourceLocation.parse(CirculationFlowNetworks.MOD_ID + ":spoce_rendering")
    );
    private final BlockPos pos;
    private final double linkScope;
    private final double energyScope;
    private final double chargingScope;

    public SpoceRendering() {
        this(BlockPos.ZERO, 0.0, 0.0, 0.0);
    }

    public SpoceRendering(BlockPos pos, double linkScope, double energyScope, double chargingScope) {
        this.pos = pos;
        this.linkScope = linkScope;
        this.energyScope = energyScope;
        this.chargingScope = chargingScope;
    }

    public SpoceRendering decode(RegistryFriendlyByteBuf buf) {
        return new SpoceRendering(BlockPos.of(buf.readLong()), buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeLong(pos.asLong());
        buf.writeDouble(linkScope);
        buf.writeDouble(energyScope);
        buf.writeDouble(chargingScope);
    }

    public void handle(SpoceRendering message, IPayloadContext context) {
        context.enqueueWork(() -> {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level == null || SpoceRenderingHandler.INSTANCE == null) {
                return;
            }
            int dimId = mc.level.dimension().location().hashCode();
            SpoceRenderingHandler.INSTANCE.setStaus(dimId, message.pos, message.linkScope, message.energyScope, message.chargingScope);
        });
    }

    @NotNull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
