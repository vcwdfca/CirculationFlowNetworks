package com.circulation.circulation_networks.packets;

import com.circulation.circulation_networks.api.node.INode;
import com.circulation.circulation_networks.container.ContainerHub;
import com.circulation.circulation_networks.manager.NetworkManager;
import com.circulation.circulation_networks.manager.PocketNodeManager;
import com.circulation.circulation_networks.utils.Packet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public final class UpdateNodeCustomName implements Packet<UpdateNodeCustomName> {

    private long posLong;
    private String customName;

    public UpdateNodeCustomName() {
    }

    public UpdateNodeCustomName(BlockPos pos, String customName) {
        this.posLong = pos.asLong();
        this.customName = customName == null ? "" : customName;
    }

    @Override
    public UpdateNodeCustomName decode(FriendlyByteBuf buf) {
        UpdateNodeCustomName msg = new UpdateNodeCustomName();
        msg.posLong = buf.readLong();
        msg.customName = buf.readUtf();
        return msg;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeLong(posLong);
        buf.writeUtf(customName == null ? "" : customName);
    }

    @Override
    public void handle(UpdateNodeCustomName message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null) return;
            if (!(sender.containerMenu instanceof ContainerHub containerHub)) return;
            if (containerHub.node == null || containerHub.node.getWorld() == null || containerHub.node.getGrid() == null)
                return;

            BlockPos pos = BlockPos.of(message.posLong);
            INode node = NetworkManager.INSTANCE.getNodeFromPos(containerHub.node.getWorld(), pos);
            if (node == null || node.getGrid() == null) return;
            if (!Objects.equals(node.getGrid().getId(), containerHub.node.getGrid().getId())) return;

            node.setCustomName(message.customName);
            if (PocketNodeManager.INSTANCE.isActivePocketNode(containerHub.node.getWorld(), pos, node.getNodeType())) {
                PocketNodeManager.INSTANCE.markDirty();
            }
        });
        context.setPacketHandled(true);
    }
}
