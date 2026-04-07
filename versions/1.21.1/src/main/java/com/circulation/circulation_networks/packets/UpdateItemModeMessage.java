package com.circulation.circulation_networks.packets;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import com.circulation.circulation_networks.items.CirculationConfiguratorModeModel;
import com.circulation.circulation_networks.items.CirculationConfiguratorState;
import com.circulation.circulation_networks.registry.CFNItems;
import com.circulation.circulation_networks.utils.Packet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public final class UpdateItemModeMessage implements Packet<UpdateItemModeMessage> {

    public static final Type<UpdateItemModeMessage> TYPE = new Type<>(
        ResourceLocation.parse(CirculationFlowNetworks.MOD_ID + ":update_item_mode")
    );
    private final byte mode;

    public UpdateItemModeMessage() {
        this(0);
    }

    public UpdateItemModeMessage(int mode) {
        this.mode = (byte) mode;
    }

    public UpdateItemModeMessage decode(RegistryFriendlyByteBuf buf) {
        return new UpdateItemModeMessage(buf.readByte());
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeByte(mode);
    }

    public void handle(UpdateItemModeMessage message, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        context.enqueueWork(() -> {
            var stack = serverPlayer.getMainHandItem();
            if (stack.getItem() == CFNItems.circulationConfigurator) {
                var function = CirculationConfiguratorState.getFunction(stack);
                CirculationConfiguratorState.setSubMode(stack, CirculationConfiguratorModeModel.wrapSubMode(message.mode, function));
            }
        });
    }

    @NotNull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
