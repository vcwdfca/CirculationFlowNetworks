package com.circulation.circulation_networks.packets;

import com.circulation.circulation_networks.container.CFNBaseContainer;
import com.circulation.circulation_networks.utils.Packet;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class ContainerProgressBar implements Packet<ContainerProgressBar> {

    private final short id;
    private final long value;

    public ContainerProgressBar() {
        this((short) 0, 0L);
    }

    public ContainerProgressBar(short channel, long val) {
        this.id = channel;
        this.value = val;
    }

    public ContainerProgressBar decode(FriendlyByteBuf buf) {
        return new ContainerProgressBar(buf.readShort(), buf.readLong());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeShort(this.id);
        buf.writeLong(this.value);
    }

    public void handle(ContainerProgressBar message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                if (sender.containerMenu instanceof CFNBaseContainer container) {
                    container.init();
                }
                return;
            }

            if (FMLEnvironment.dist.isClient()) {
                var player = Minecraft.getInstance().player;
                if (player != null && player.containerMenu instanceof CFNBaseContainer container) {
                    container.updateFullProgressBar(message.id, message.value);
                }
            }
        });
        context.setPacketHandled(true);
    }
}