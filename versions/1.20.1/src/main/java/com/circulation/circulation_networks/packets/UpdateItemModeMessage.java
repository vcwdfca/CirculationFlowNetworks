package com.circulation.circulation_networks.packets;

import com.circulation.circulation_networks.items.CirculationConfiguratorModeModel;
import com.circulation.circulation_networks.items.CirculationConfiguratorState;
import com.circulation.circulation_networks.registry.CFNItems;
import com.circulation.circulation_networks.utils.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class UpdateItemModeMessage implements Packet<UpdateItemModeMessage> {

    private final byte mode;

    public UpdateItemModeMessage() {
        this(0);
    }

    public UpdateItemModeMessage(int mode) {
        this.mode = (byte) mode;
    }

    public UpdateItemModeMessage decode(FriendlyByteBuf buf) {
        return new UpdateItemModeMessage(buf.readByte());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeByte(mode);
    }

    public void handle(UpdateItemModeMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null) {
                return;
            }

            var stack = sender.getMainHandItem();
            if (stack.getItem() == CFNItems.circulationConfigurator && stack.getTag() != null) {
                var function = CirculationConfiguratorState.getFunction(stack);
                CirculationConfiguratorState.setSubMode(stack, CirculationConfiguratorModeModel.wrapSubMode(message.mode, function));
            }
        });
        context.setPacketHandled(true);
    }
}
