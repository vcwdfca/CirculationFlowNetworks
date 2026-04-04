package com.circulation.circulation_networks.packets;

import com.circulation.circulation_networks.handlers.PocketNodeRenderingHandler;
import com.circulation.circulation_networks.manager.PocketNodeManager;
import com.circulation.circulation_networks.pocket.PocketNodeRecord;
import com.circulation.circulation_networks.registry.NodeTypes;
import com.circulation.circulation_networks.utils.Packet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class PocketNodeRendering implements Packet<PocketNodeRendering> {

    public static final int SET = 0;
    public static final int ADD = 1;
    public static final int REMOVE = 2;

    private int mode;
    private int dim;
    private long posLong;
    private ObjectList<PocketNodeRecord> records;
    private transient int parsedMode;
    private transient int parsedDim;
    private transient long parsedPosLong;
    private transient ObjectList<PocketNodeRecord> parsedRecords;

    public PocketNodeRendering() {
    }

    public PocketNodeRendering(ServerPlayer player) {
        this.mode = SET;
        this.dim = player.level().dimension().location().hashCode();
        this.records = PocketNodeManager.INSTANCE.getActiveRecords(dim);
    }

    public PocketNodeRendering(PocketNodeRecord record) {
        this.mode = ADD;
        this.dim = record.getDimensionId();
        this.records = new ObjectArrayList<>();
        this.records.add(record);
    }

    public PocketNodeRendering(int dim, BlockPos pos) {
        this.mode = REMOVE;
        this.dim = dim;
        this.posLong = pos.asLong();
    }

    @Override
    public PocketNodeRendering decode(FriendlyByteBuf buf) {
        PocketNodeRendering message = new PocketNodeRendering();
        message.parsedMode = buf.readByte();
        message.parsedDim = buf.readInt();
        if (message.parsedMode == REMOVE) {
            message.parsedPosLong = buf.readLong();
            return message;
        }
        int count = buf.readInt();
        message.parsedRecords = new ObjectArrayList<>(count);
        for (int i = 0; i < count; i++) {
            var nodeType = NodeTypes.getById(buf.readUtf());
            BlockPos pos = BlockPos.of(buf.readLong());
            Direction face = buf.readBoolean() ? Direction.byName(buf.readUtf()) : null;
            if (nodeType != null && nodeType.allowsPocketNode()) {
                message.parsedRecords.add(new PocketNodeRecord(message.parsedDim, pos, nodeType, face, null));
            }
        }
        return message;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeByte(mode);
        buf.writeInt(dim);
        if (mode == REMOVE) {
            buf.writeLong(posLong);
            return;
        }
        buf.writeInt(records.size());
        for (var record : records) {
            buf.writeUtf(record.getNodeType().getId());
            buf.writeLong(record.getPos().asLong());
            buf.writeBoolean(record.getAttachmentFace() != null);
            if (record.getAttachmentFace() != null) {
                buf.writeUtf(record.getAttachmentFace().getName());
            }
        }
    }

    @Override
    public void handle(PocketNodeRendering message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (!FMLEnvironment.dist.isClient() || Minecraft.getInstance().player == null) {
                return;
            }
            if (message.parsedMode == SET) {
                PocketNodeRenderingHandler.INSTANCE.setDimensionState(message.parsedDim, message.parsedRecords == null ? new ObjectArrayList<>() : message.parsedRecords);
            } else if (message.parsedMode == ADD) {
                if (message.parsedRecords != null) {
                    for (var record : message.parsedRecords) {
                        PocketNodeRenderingHandler.INSTANCE.add(record);
                    }
                }
            } else if (message.parsedMode == REMOVE) {
                PocketNodeRenderingHandler.INSTANCE.remove(message.parsedDim, BlockPos.of(message.parsedPosLong));
            }
        });
        context.setPacketHandled(true);
    }
}
