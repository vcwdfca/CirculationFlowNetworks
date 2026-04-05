package com.circulation.circulation_networks.packets;

import com.circulation.circulation_networks.items.InspectionToolModeModel;
import com.circulation.circulation_networks.items.InspectionToolState;
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

        if (stack.getItem() == CFNItems.inspectionTool && stack.getTagCompound() != null) {
            var function = InspectionToolState.getFunction(stack);
            InspectionToolState.setSubMode(stack, InspectionToolModeModel.wrapSubMode(message.mode, function));
        }
        return null;
    }

}