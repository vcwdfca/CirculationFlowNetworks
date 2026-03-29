package com.circulation.circulation_networks.network;

import com.circulation.circulation_networks.packets.ConfigOverrideRendering;
import com.circulation.circulation_networks.packets.ContainerProgressBar;
import com.circulation.circulation_networks.packets.ContainerValueConfig;
import com.circulation.circulation_networks.packets.NodeNetworkRendering;
import com.circulation.circulation_networks.packets.RenderingClear;
import com.circulation.circulation_networks.packets.SpoceRendering;
import com.circulation.circulation_networks.packets.UpdateItemModeMessage;
import com.circulation.circulation_networks.utils.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class CFNNetwork {

    private CFNNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registerPlayToServer(registrar, ContainerProgressBar.class);
        registerPlayToServer(registrar, UpdateItemModeMessage.class);

        registerPlayToClient(registrar, SpoceRendering.class);
        registerPlayToClient(registrar, NodeNetworkRendering.class);
        registerPlayToClient(registrar, ConfigOverrideRendering.class);
        registerPlayToClient(registrar, ContainerProgressBar.class);
        registerPlayToClient(registrar, ContainerValueConfig.class);
        registerPlayToClient(registrar, RenderingClear.INSTANCE);
    }

    public static <T extends Packet<T>> void sendToPlayer(T packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static <T extends Packet<T>> void sendToServer(T packet) {
        PacketDistributor.sendToServer(packet);
    }

    public static <T extends Packet<T>> void registerPlayToClient(PayloadRegistrar registrar, Class<T> packetClass) {
        registerPlayToClient(registrar, createPacket(packetClass));
    }

    public static <T extends Packet<T>> void registerPlayToClient(PayloadRegistrar registrar, T packet) {
        registrar.playToClient(payloadType(packet), packet.streamCodec(), packet::handle);
    }

    public static <T extends Packet<T>> void registerPlayToServer(PayloadRegistrar registrar, Class<T> packetClass) {
        registerPlayToServer(registrar, createPacket(packetClass));
    }

    public static <T extends Packet<T>> void registerPlayToServer(PayloadRegistrar registrar, T packet) {
        registrar.playToServer(payloadType(packet), packet.streamCodec(), packet::handle);
    }

    private static <T extends Packet<T>> T createPacket(Class<T> packetClass) {
        try {
            return packetClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to create packet prototype for " + packetClass.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Packet<T>> CustomPacketPayload.Type<T> payloadType(T packet) {
        return (CustomPacketPayload.Type<T>) packet.type();
    }
}