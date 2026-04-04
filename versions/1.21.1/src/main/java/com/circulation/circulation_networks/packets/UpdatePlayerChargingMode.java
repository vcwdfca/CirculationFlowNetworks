package com.circulation.circulation_networks.packets;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import com.circulation.circulation_networks.api.hub.ChargingDefinition;
import com.circulation.circulation_networks.container.ContainerHub;
import com.circulation.circulation_networks.utils.Packet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public final class UpdatePlayerChargingMode implements Packet<UpdatePlayerChargingMode> {

    public static final Type<UpdatePlayerChargingMode> TYPE = new Type<>(
        ResourceLocation.parse(CirculationFlowNetworks.MOD_ID + ":update_player_charging_mode")
    );
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
    public UpdatePlayerChargingMode decode(RegistryFriendlyByteBuf buf) {
        UpdatePlayerChargingMode msg = new UpdatePlayerChargingMode();
        msg.bytes = buf.readByte();
        msg.mode = buf.readByte();
        return msg;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeByte(bytes);
        buf.writeByte(mode);
    }

    @Override
    public void handle(UpdatePlayerChargingMode message, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer sender)) return;
        context.enqueueWork(() -> {
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
    }

    @NotNull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
