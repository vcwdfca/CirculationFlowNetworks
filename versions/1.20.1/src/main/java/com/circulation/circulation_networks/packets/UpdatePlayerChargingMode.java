package com.circulation.circulation_networks.packets;

import com.circulation.circulation_networks.api.hub.ChargingDefinition;
import com.circulation.circulation_networks.container.ContainerHub;
import com.circulation.circulation_networks.utils.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class UpdatePlayerChargingMode implements Packet<UpdatePlayerChargingMode> {

    private static final ChargingDefinition[] CHARGING_DEFINITIONS = ChargingDefinition.values();

    private byte bytes;
    private byte mode;

    public UpdatePlayerChargingMode() {
    }

    public UpdatePlayerChargingMode(ChargingDefinition index) {
        this.bytes = (byte) index.ordinal();
    }

    public UpdatePlayerChargingMode(byte modes) {
        this.mode = modes;
    }

    @Override
    public UpdatePlayerChargingMode decode(FriendlyByteBuf buf) {
        UpdatePlayerChargingMode msg = new UpdatePlayerChargingMode();
        msg.bytes = buf.readByte();
        msg.mode = buf.readByte();
        return msg;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeByte(bytes);
        buf.writeByte(mode);
    }

    @Override
    public void handle(UpdatePlayerChargingMode message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null) return;
            if (sender.containerMenu instanceof ContainerHub containerHub) {
                switch (message.mode) {
                    case 0 -> {
                        int index = message.bytes & 0x7F;
                        if (index >= CHARGING_DEFINITIONS.length) {
                            return;
                        }
                        var cd = CHARGING_DEFINITIONS[index];
                        containerHub.chargingMode.setPreference(cd, !containerHub.chargingMode.getPreference(cd));
                    }
                    case 1 -> containerHub.chargingMode.setPrefs((byte) 0b00111111);
                    case 2 -> containerHub.chargingMode.setPrefs((byte) 0);
                }
            }
        });
        context.setPacketHandled(true);
    }
}
