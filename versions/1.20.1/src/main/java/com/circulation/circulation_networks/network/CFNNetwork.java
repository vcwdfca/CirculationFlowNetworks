package com.circulation.circulation_networks.network;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import com.circulation.circulation_networks.packets.ConfigOverrideRendering;
import com.circulation.circulation_networks.packets.ContainerProgressBar;
import com.circulation.circulation_networks.packets.ContainerValueConfig;
import com.circulation.circulation_networks.packets.NodeNetworkRendering;
import com.circulation.circulation_networks.packets.RenderingClear;
import com.circulation.circulation_networks.packets.SpoceRendering;
import com.circulation.circulation_networks.packets.UpdateItemModeMessage;
import com.circulation.circulation_networks.utils.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class CFNNetwork {

    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(CirculationFlowNetworks.MOD_ID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );
    private static boolean registered;
    private static int nextMessageId;

    private CFNNetwork() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        nextMessageId = 0;

        registerMessage(ContainerProgressBar.class, NetworkDirection.PLAY_TO_SERVER);
        registerMessage(UpdateItemModeMessage.class, NetworkDirection.PLAY_TO_SERVER);

        registerMessage(SpoceRendering.class, NetworkDirection.PLAY_TO_CLIENT);
        registerMessage(NodeNetworkRendering.class, NetworkDirection.PLAY_TO_CLIENT);
        registerMessage(ConfigOverrideRendering.class, NetworkDirection.PLAY_TO_CLIENT);
        registerMessage(ContainerProgressBar.class, NetworkDirection.PLAY_TO_CLIENT);
        registerMessage(ContainerValueConfig.class, NetworkDirection.PLAY_TO_CLIENT);
        registerMessage(RenderingClear.INSTANCE, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <T extends Packet<T>> void sendToPlayer(T packet, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static <T extends Packet<T>> void sendToServer(T packet) {
        CHANNEL.sendToServer(packet);
    }

    public static <T extends Packet<T>> void registerMessage(Class<T> packetClass, NetworkDirection direction) {
        registerMessage(createPacket(packetClass), direction);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Packet<T>> void registerMessage(T packet, NetworkDirection direction) {
        Class<T> packetClass = (Class<T>) packet.getClass();
        CHANNEL.messageBuilder(packetClass, nextMessageId++, direction)
            .encoder(Packet::encode)
            .decoder(packet::decode)
            .consumerMainThread(packet::handle)
            .add();
    }

    private static <T extends Packet<T>> T createPacket(Class<T> packetClass) {
        try {
            return packetClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to create packet prototype for " + packetClass.getName(), e);
        }
    }
}