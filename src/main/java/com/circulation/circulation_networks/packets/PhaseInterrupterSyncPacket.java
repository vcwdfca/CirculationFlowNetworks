package com.circulation.circulation_networks.packets;

import com.circulation.circulation_networks.container.ContainerPhaseInterrupter;
import com.circulation.circulation_networks.tiles.TileEntityPhaseInterrupter;
import com.circulation.circulation_networks.utils.Packet;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PhaseInterrupterSyncPacket implements Packet<PhaseInterrupterSyncPacket> {
    private int scope;
    private boolean redstoneMode;

    public PhaseInterrupterSyncPacket() {
    }

    public PhaseInterrupterSyncPacket(TileEntityPhaseInterrupter te) {
        this.scope = te.getScope();
        this.redstoneMode = te.getRedstoneMode();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.scope = buf.readInt();
        this.redstoneMode = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.scope);
        buf.writeBoolean(this.redstoneMode);
    }

    @Override
    public IMessage onMessage(PhaseInterrupterSyncPacket message, MessageContext ctx) {
        if (ctx.getServerHandler().player.openContainer instanceof ContainerPhaseInterrupter c) {
            var te = c.te;
            if (te != null) {
                te.setScope(message.scope);
                te.setRedstoneMode(message.redstoneMode);
                te.markDirty();
            }
        }
        return null;
    }
}