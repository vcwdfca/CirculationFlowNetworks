package com.circulation.circulation_networks.packets;

import com.circulation.circulation_networks.handlers.CirculationShielderRenderingHandler;
import com.circulation.circulation_networks.handlers.ConfigOverrideRenderingHandler;
import com.circulation.circulation_networks.handlers.EnergyWarningRenderingHandler;
import com.circulation.circulation_networks.handlers.NodeHighlightRenderingHandler;
import com.circulation.circulation_networks.handlers.NodeNetworkRenderingHandler;
import com.circulation.circulation_networks.handlers.PocketNodeRenderingHandler;
import com.circulation.circulation_networks.handlers.SpoceRenderingHandler;
import com.circulation.circulation_networks.utils.Packet;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.Nullable;

public class RenderingClear implements Packet<RenderingClear> {

    public static final RenderingClear INSTANCE = new RenderingClear();

    public RenderingClear() {

    }

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    @Override
    public @Nullable IMessage onMessage(RenderingClear message, MessageContext ctx) {
        NodeNetworkRenderingHandler.INSTANCE.clearLinks();
        EnergyWarningRenderingHandler.INSTANCE.clear();
        ConfigOverrideRenderingHandler.INSTANCE.clear();
        PocketNodeRenderingHandler.INSTANCE.clear();
        NodeHighlightRenderingHandler.INSTANCE.clear();
        CirculationShielderRenderingHandler.INSTANCE.clear();
        SpoceRenderingHandler.INSTANCE.clear();
        return null;
    }
}
