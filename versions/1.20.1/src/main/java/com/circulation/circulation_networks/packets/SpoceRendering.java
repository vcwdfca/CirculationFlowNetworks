package com.circulation.circulation_networks.packets;

import com.circulation.circulation_networks.handlers.SpoceRenderingHandler;
import com.circulation.circulation_networks.utils.Packet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class SpoceRendering implements Packet<SpoceRendering> {

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

    public SpoceRendering decode(FriendlyByteBuf buf) {
        return new SpoceRendering(BlockPos.of(buf.readLong()), buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeLong(pos.asLong());
        buf.writeDouble(linkScope);
        buf.writeDouble(energyScope);
        buf.writeDouble(chargingScope);
    }

    public void handle(SpoceRendering message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (!FMLEnvironment.dist.isClient()) {
                return;
            }
            var mc = Minecraft.getInstance();
            if (mc.level == null || SpoceRenderingHandler.INSTANCE == null) {
                return;
            }
            var blockEntity = mc.level.getBlockEntity(message.pos);
            if (blockEntity != null) {
                SpoceRenderingHandler.INSTANCE.setStaus(blockEntity, message.linkScope, message.energyScope, message.chargingScope);
            }
        });
        context.setPacketHandled(true);
    }
}
