package com.circulation.circulation_networks.packets;

import com.circulation.circulation_networks.items.CirculationConfiguratorModeModel;
import com.circulation.circulation_networks.items.CirculationConfiguratorState;
import com.circulation.circulation_networks.registry.CFNItems;
import com.circulation.circulation_networks.utils.Packet;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class UpdateItemModeMessage implements Packet<UpdateItemModeMessage> {

    private byte mode;

    public UpdateItemModeMessage() {
    }

    public UpdateItemModeMessage(int mode) {
        this.mode = (byte) mode;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        mode = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(mode);
    }

    @Override
    public IMessage onMessage(UpdateItemModeMessage message, MessageContext ctx) {
        ItemStack stack = ctx.getServerHandler().player.getHeldItemMainhand();

        if (stack.getItem() == CFNItems.circulationConfigurator && stack.getTagCompound() != null) {
            var function = CirculationConfiguratorState.getFunction(stack);
            CirculationConfiguratorState.setSubMode(stack, CirculationConfiguratorModeModel.wrapSubMode(message.mode, function));
        }
        return null;
    }

}