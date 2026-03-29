package com.circulation.circulation_networks.registry;

import com.circulation.circulation_networks.api.NodeDeserializer;
import com.circulation.circulation_networks.api.node.INode;
import com.circulation.circulation_networks.network.nodes.ChargingNode;
import com.circulation.circulation_networks.network.nodes.HubNode;
import com.circulation.circulation_networks.network.nodes.InductionNode;
import com.circulation.circulation_networks.network.nodes.machine_node.ConsumerNode;
import com.circulation.circulation_networks.network.nodes.machine_node.GeneratorNode;
import com.circulation.circulation_networks.network.nodes.machine_node.StorageNode;
//~ mc_imports
import net.minecraft.nbt.NBTTagCompound;
//? if <1.20
import net.minecraftforge.common.DimensionManager;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;

public final class RegistryNodes {

    private static final Object2ReferenceMap<String, NodeDeserializer> map = new Object2ReferenceOpenHashMap<>();

    static {
        register(InductionNode.class, InductionNode::new);
        register(ChargingNode.class, ChargingNode::new);
        register(HubNode.class, HubNode::new);
        register(GeneratorNode.class, GeneratorNode::new);
        register(StorageNode.class, StorageNode::new);
        register(ConsumerNode.class, ConsumerNode::new);
    }

    public static void register(Class<? extends INode> nodeClass, NodeDeserializer function) {
        map.put(nodeClass.getName(), function);
    }

    //~ if >=1.20 'NBTTagCompound' -> 'CompoundTag' {
    //~ if >=1.20 '.hasKey(' -> '.contains(' {
    public static INode deserialize(NBTTagCompound tag) {
        if (tag == null) return null;
        if (!tag.hasKey("name")) return null;
        //? if <1.20 {
        if (!tag.hasKey("dim") || !isRegisteredDimension(tag.getInteger("dim"))) return null;
        //?} else {
        /*if (!tag.contains("dim") || !isRegisteredDimension(tag.getString("dim"))) return null;
        *///?}

        var d = map.get(tag.getString("name"));
        if (d != null) {
            return d.apply(tag);
        }
        return null;
    }
    //~}
    //~}

    //? if <1.20 {
    private static boolean isRegisteredDimension(int dimId) {
        return DimensionManager.isDimensionRegistered(dimId);
    }
    //?} else if <1.21 {
    /*private static boolean isRegisteredDimension(String dimKey) {
        var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;
        return server.getLevel(
            net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.DIMENSION,
                new net.minecraft.resources.ResourceLocation(dimKey)
            )
        ) != null;
    }
    *///?} else {
    /*private static boolean isRegisteredDimension(String dimKey) {
        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return false;
        return server.getLevel(
            net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.DIMENSION,
                net.minecraft.resources.ResourceLocation.parse(dimKey)
            )
        ) != null;
    }
    *///?}
}