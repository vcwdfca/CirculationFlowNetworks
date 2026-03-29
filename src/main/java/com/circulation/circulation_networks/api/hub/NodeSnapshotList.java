package com.circulation.circulation_networks.api.hub;

import com.circulation.circulation_networks.api.IGrid;
import com.circulation.circulation_networks.api.node.INode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
//~ mc_imports
import net.minecraft.tileentity.TileEntity;
//? if <1.20 {
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
//?} else {
/*import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
*///?}

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class NodeSnapshotList {

    private static final Gson GSON = new GsonBuilder().create();
    public static final NodeSnapshotList EMPTY = new NodeSnapshotList(Collections.emptyList());
    public static final String EMPTY_JSON = EMPTY.toJson();

    private final List<NodeSnapshotEntry> entries;
    private String json;
    private byte[] bytes;

    public NodeSnapshotList(List<NodeSnapshotEntry> entries) {
        this.entries = entries.isEmpty()
            ? Collections.emptyList()
            : Collections.unmodifiableList(new ObjectArrayList<>(entries));
    }

    public List<NodeSnapshotEntry> getEntries() {
        return entries;
    }

    public String toJson() {
        if (json == null) {
            json = GSON.toJson(this);
        }
        return json;
    }

    public byte[] toBytes() {
        if (bytes == null) {
            try (ByteArrayOutputStream output = new ByteArrayOutputStream();
                 DataOutputStream data = new DataOutputStream(output)) {
                writeVarInt(data, entries.size());
                int previousX = 0;
                int previousY = 0;
                int previousZ = 0;
                boolean first = true;
                for (NodeSnapshotEntry entry : entries) {
                    writeVarInt(data, resolveBlockIntId(entry.getBlockId()));
                    if (first) {
                        writeZigZagInt(data, entry.getX());
                        writeZigZagInt(data, entry.getY());
                        writeZigZagInt(data, entry.getZ());
                        first = false;
                    } else {
                        writeZigZagInt(data, entry.getX() - previousX);
                        writeZigZagInt(data, entry.getY() - previousY);
                        writeZigZagInt(data, entry.getZ() - previousZ);
                    }
                    writeNullableString(data, entry.getCustomName());
                    previousX = entry.getX();
                    previousY = entry.getY();
                    previousZ = entry.getZ();
                }
                bytes = output.toByteArray();
            } catch (IOException e) {
                throw new IllegalStateException("Failed to encode node snapshot", e);
            }
        }
        return bytes.clone();
    }

    public static NodeSnapshotList fromJson(String json) {
        NodeSnapshotList list = GSON.fromJson(json, NodeSnapshotList.class);
        if (list == null || list.entries == null || list.entries.isEmpty()) {
            return EMPTY;
        }
        return new NodeSnapshotList(list.entries);
    }

    public static NodeSnapshotList fromBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return EMPTY;
        }
        try (DataInputStream data = new DataInputStream(new ByteArrayInputStream(bytes))) {
            int size = readVarInt(data);
            if (size <= 0) {
                return EMPTY;
            }
            List<NodeSnapshotEntry> entries = new ObjectArrayList<>(size);
            int previousX = 0;
            int previousY = 0;
            int previousZ = 0;
            boolean first = true;
            for (int i = 0; i < size; i++) {
                String blockId = resolveBlockId(readVarInt(data));
                int x;
                int y;
                int z;
                if (first) {
                    x = readZigZagInt(data);
                    y = readZigZagInt(data);
                    z = readZigZagInt(data);
                    first = false;
                } else {
                    x = previousX + readZigZagInt(data);
                    y = previousY + readZigZagInt(data);
                    z = previousZ + readZigZagInt(data);
                }
                String customName = readNullableString(data);
                entries.add(new NodeSnapshotEntry(blockId, x, y, z, customName));
                previousX = x;
                previousY = y;
                previousZ = z;
            }
            return entries.isEmpty() ? EMPTY : new NodeSnapshotList(entries);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to decode node snapshot", e);
        }
    }

    public static NodeSnapshotList fromGrid(@Nullable IGrid grid) {
        if (grid == null || grid.getNodes().isEmpty()) {
            return EMPTY;
        }

        List<NodeSnapshotEntry> entries = new ObjectArrayList<>(grid.getNodes().size());
        for (INode node : grid.getNodes()) {
            String blockId = resolveBlockId(node);
            var pos = node.getPos();
            entries.add(new NodeSnapshotEntry(blockId, pos.getX(), pos.getY(), pos.getZ(), node.getCustomName()));
        }
        entries.sort(Comparator.comparingInt(NodeSnapshotEntry::getX)
            .thenComparingInt(NodeSnapshotEntry::getY)
            .thenComparingInt(NodeSnapshotEntry::getZ)
            .thenComparing(NodeSnapshotEntry::getBlockId));
        return entries.isEmpty() ? EMPTY : new NodeSnapshotList(entries);
    }

    private static String resolveBlockId(INode node) {
        //? if <1.20 {
        TileEntity blockEntity = node.getBlockEntity();
        if (blockEntity == null) {
            return "";
        }
        ResourceLocation registryName = blockEntity.getWorld().getBlockState(blockEntity.getPos()).getBlock().getRegistryName();
        return registryName != null ? registryName.toString() : "";
        //?} else {
        /*BlockEntity blockEntity = node.getBlockEntity();
        if (blockEntity == null) {
            return "";
        }
        return BuiltInRegistries.BLOCK.getKey(blockEntity.getBlockState().getBlock()).toString();
        *///?}
    }

    private static int resolveBlockIntId(String blockId) {
        //? if <1.20 {
        if (blockId == null || blockId.isEmpty()) {
            return 0;
        }
        Block block = Block.REGISTRY.getObject(new ResourceLocation(blockId));
        return block != null ? Block.getIdFromBlock(block) : 0;
        //?} else if <1.21 {
        /*if (blockId == null || blockId.isEmpty()) {
            return 0;
        }
        return BuiltInRegistries.BLOCK.getId(BuiltInRegistries.BLOCK.get(new ResourceLocation(blockId)));
        *///?} else {
        /*if (blockId == null || blockId.isEmpty()) {
            return 0;
        }
        return BuiltInRegistries.BLOCK.getId(BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockId)));
        *///?}
    }

    private static String resolveBlockId(int blockId) {
        //? if <1.20 {
        Block block = Block.getBlockById(blockId);
        if (block == null) {
            return "";
        }
        ResourceLocation registryName = block.getRegistryName();
        return registryName != null ? registryName.toString() : "";
        //?} else {
        /*return BuiltInRegistries.BLOCK.byId(blockId).builtInRegistryHolder().key().location().toString();
        *///?}
    }

    private static void writeNullableString(DataOutputStream data, @Nullable String value) throws IOException {
        if (value == null) {
            data.writeBoolean(false);
            return;
        }
        data.writeBoolean(true);
        byte[] encoded = value.getBytes(StandardCharsets.UTF_8);
        writeVarInt(data, encoded.length);
        data.write(encoded);
    }

    @Nullable
    private static String readNullableString(DataInputStream data) throws IOException {
        if (!data.readBoolean()) {
            return null;
        }
        int length = readVarInt(data);
        byte[] encoded = new byte[length];
        data.readFully(encoded);
        return new String(encoded, StandardCharsets.UTF_8);
    }

    private static void writeZigZagInt(DataOutputStream data, int value) throws IOException {
        writeVarInt(data, (value << 1) ^ (value >> 31));
    }

    private static int readZigZagInt(DataInputStream data) throws IOException {
        int value = readVarInt(data);
        return (value >>> 1) ^ -(value & 1);
    }

    private static void writeVarInt(DataOutputStream data, int value) throws IOException {
        int current = value;
        while ((current & ~0x7F) != 0) {
            data.writeByte((current & 0x7F) | 0x80);
            current >>>= 7;
        }
        data.writeByte(current);
    }

    private static int readVarInt(DataInputStream data) throws IOException {
        int value = 0;
        int position = 0;
        while (true) {
            int currentByte = data.readUnsignedByte();
            value |= (currentByte & 0x7F) << position;
            if ((currentByte & 0x80) == 0) {
                return value;
            }
            position += 7;
            if (position >= 32) {
                throw new IOException("VarInt is too big");
            }
        }
    }
}