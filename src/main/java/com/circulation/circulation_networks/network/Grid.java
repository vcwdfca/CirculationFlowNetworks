package com.circulation.circulation_networks.network;

import com.circulation.circulation_networks.api.IGrid;
import com.circulation.circulation_networks.api.node.IHubNode;
import com.circulation.circulation_networks.api.node.INode;
import com.circulation.circulation_networks.registry.RegistryNodes;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
//~ mc_imports
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
//? if <1.20 {
import net.minecraftforge.common.util.Constants;
//?} else {
/*import net.minecraft.nbt.Tag;
*///?}

import javax.annotation.Nullable;
import java.util.UUID;

public final class Grid implements IGrid {

    private final UUID id;
    private final ReferenceSet<INode> nodes = new ReferenceOpenHashSet<>();
    private long snapshotVersion = 1L;
    @Nullable
    private IHubNode hubNode;

    public Grid(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public ReferenceSet<INode> getNodes() {
        return nodes;
    }

    @Nullable
    public IHubNode getHubNode() {
        return hubNode;
    }

    public void setHubNode(@Nullable IHubNode hubNode) {
        this.hubNode = hubNode;
        markSnapshotDirty();
    }

    @Override
    public long getSnapshotVersion() {
        return snapshotVersion;
    }

    @Override
    public void markSnapshotDirty() {
        snapshotVersion++;
    }

    //? if <1.20 {
    public static Grid deserialize(NBTTagCompound nbt) {
        var grid = new Grid(nbt.getUniqueId("id"));
        var list = nbt.getTagList("nodes", 10);

        var posMap = new Long2ReferenceOpenHashMap<INode>();
        for (var nbtBase : list) {
            var nodeNbt = (NBTTagCompound) nbtBase;
            var node = RegistryNodes.deserialize(nodeNbt);
            if (node != null) {
                node.setGrid(grid);
                node.setActive(true);
                grid.nodes.add(node);
                posMap.put(nodeNbt.getLong("pos"), node);
                if (node instanceof IHubNode hub) {
                    grid.setHubNode(hub);
                }
            }
        }
        for (var nbtBase : list) {
            var nodeNbt = (NBTTagCompound) nbtBase;
            var node = posMap.get(nodeNbt.getLong("pos"));
            if (node == null) continue;
            var neighborList = nodeNbt.getTagList("neighbors", Constants.NBT.TAG_LONG);
            for (var nb : neighborList) {
                var neighbor = posMap.get(((NBTTagLong) nb).getLong());
                if (neighbor != null) {
                    node.addNeighbor(neighbor);
                }
            }
        }

        return grid;
    }
    //?} else {
    /*public static Grid deserialize(CompoundTag nbt) {
        var grid = new Grid(nbt.getUUID("id"));
        var list = nbt.getList("nodes", Tag.TAG_COMPOUND);

        var posMap = new Long2ReferenceOpenHashMap<INode>();
        for (var nbtBase : list) {
            var nodeNbt = (CompoundTag) nbtBase;
            var node = RegistryNodes.deserialize(nodeNbt);
            if (node != null) {
                node.setGrid(grid);
                node.setActive(true);
                grid.nodes.add(node);
                posMap.put(nodeNbt.getLong("pos"), node);
                if (node instanceof IHubNode hub) {
                    grid.setHubNode(hub);
                }
            }
        }
        for (var nbtBase : list) {
            var nodeNbt = (CompoundTag) nbtBase;
            var node = posMap.get(nodeNbt.getLong("pos"));
            if (node == null) continue;
            var neighborList = nodeNbt.getList("neighbors", Tag.TAG_LONG);
            for (var nb : neighborList) {
                var neighbor = posMap.get(((LongTag) nb).getAsLong());
                if (neighbor != null) {
                    node.addNeighbor(neighbor);
                }
            }
        }

        return grid;
    }
    *///?}

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    //? if <1.20 {
    @Override
    public NBTTagCompound serialize() {
        var nbt = new NBTTagCompound();
        var list = new NBTTagList();
        nbt.setUniqueId("id", id);
        if (!nodes.isEmpty()) {
            for (var node : nodes) {
                nbt.setInteger("dim", getDimensionId(node));
                break;
            }
            for (var node : nodes) {
                list.appendTag(node.serialize());
            }
        }
        nbt.setTag("nodes", list);
        return nbt;
    }
    //?} else {
    /*@Override
    public CompoundTag serialize() {
        var nbt = new CompoundTag();
        var list = new ListTag();
        nbt.putUUID("id", id);
        if (!nodes.isEmpty()) {
            for (var node : nodes) {
                nbt.putString("dim", node.getWorld().dimension().location().toString());
                break;
            }
            for (var node : nodes) {
                list.add(node.serialize());
            }
        }
        nbt.put("nodes", list);
        return nbt;
    }
    *///?}

    //~ if >=1.20 '.provider.getDimension()' -> '.dimension().location().hashCode()' {
    private static int getDimensionId(INode node) {
        return node.getWorld().provider.getDimension();
    }
    //~}

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Grid other = (Grid) obj;
        return this.id.equals(other.id);
    }
}