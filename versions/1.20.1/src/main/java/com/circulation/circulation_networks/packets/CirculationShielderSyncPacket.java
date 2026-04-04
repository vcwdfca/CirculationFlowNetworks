package com.circulation.circulation_networks.packets;

import com.circulation.circulation_networks.container.ContainerCirculationShielder;
import com.circulation.circulation_networks.tiles.CirculationShielderBlockEntity;
import com.circulation.circulation_networks.utils.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CirculationShielderSyncPacket implements Packet<CirculationShielderSyncPacket> {

    private int scope;
    private boolean redstoneMode;

    public CirculationShielderSyncPacket() {
    }

    public CirculationShielderSyncPacket(CirculationShielderBlockEntity te) {
        this.scope = te.getScope();
        this.redstoneMode = te.getRedstoneMode();
    }

    @Override
    public CirculationShielderSyncPacket decode(FriendlyByteBuf buf) {
        CirculationShielderSyncPacket msg = new CirculationShielderSyncPacket();
        msg.scope = buf.readInt();
        msg.redstoneMode = buf.readBoolean();
        return msg;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.scope);
        buf.writeBoolean(this.redstoneMode);
    }

    @Override
    public void handle(CirculationShielderSyncPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null) return;
            if (sender.containerMenu instanceof ContainerCirculationShielder c) {
                var te = c.te;
                if (te != null) {
                    te.setScope(message.scope);
                    te.setRedstoneMode(message.redstoneMode);
                    te.setChanged();
                }
            }
        });
        context.setPacketHandled(true);
    }
}
