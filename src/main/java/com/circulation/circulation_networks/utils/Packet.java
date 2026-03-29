package com.circulation.circulation_networks.utils;

//? if <1.20 {
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
public interface Packet<T extends Packet<T>> extends IMessageHandler<T, IMessage>, IMessage {
}
//?} else if <1.21 {
/*import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public interface Packet<T extends Packet<T>> {

    T decode(FriendlyByteBuf buf);

    void encode(FriendlyByteBuf buf);

    void handle(T message, Supplier<NetworkEvent.Context> contextSupplier);

}
*///?} else {
/*import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface Packet<T extends Packet<T>> extends CustomPacketPayload {

    T decode(RegistryFriendlyByteBuf buf);

    void encode(RegistryFriendlyByteBuf buf);

    void handle(T message, IPayloadContext context);

    default StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        return new StreamCodec<>() {
            @Override
            public T decode(RegistryFriendlyByteBuf buf) {
                return Packet.this.decode(buf);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, T value) {
                value.encode(buf);
            }
        };
    }
}
*///?}