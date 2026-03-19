package com.circulation.circulation_networks.manager;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import com.circulation.circulation_networks.api.EnergyAmount;
import com.circulation.circulation_networks.api.IEnergyHandler;
import com.circulation.circulation_networks.api.IGrid;
import com.circulation.circulation_networks.api.node.IEnergySupplyNode;
import com.circulation.circulation_networks.api.node.IMachineNode;
import com.circulation.circulation_networks.api.node.INode;
import com.circulation.circulation_networks.events.BlockEntityLifeCycleEvent;
//? if <1.20 {
import com.circulation.circulation_networks.packets.NodeNetworkRendering;
//?}
import com.circulation.circulation_networks.registry.RegistryEnergyHandler;
import com.circulation.circulation_networks.utils.Functions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.objects.ReferenceSets;
//? if <1.20 {
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
//?} else {
/*import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
*///?}

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.server.MinecraftServer;

public final class EnergyMachineManager {

    public static final EnergyMachineManager INSTANCE = new EnergyMachineManager();
    private final Int2ObjectMap<Long2ObjectMap<ReferenceSet<IEnergySupplyNode>>> scopeNode = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Object2ObjectMap<IEnergySupplyNode, LongSet>> nodeScope = new Int2ObjectOpenHashMap<>();
    //? if <1.20 {
    private final Reference2ObjectMap<INode, Set<TileEntity>> gridMachineMap = new Reference2ObjectOpenHashMap<>();
    private final WeakHashMap<TileEntity, ReferenceSet<INode>> machineGridMap = new WeakHashMap<>();
    //?} else {
    /*private final Reference2ObjectMap<INode, Set<BlockEntity>> gridMachineMap = new Reference2ObjectOpenHashMap<>();
    private final WeakHashMap<BlockEntity, ReferenceSet<INode>> machineGridMap = new WeakHashMap<>();
    *///?}
    private final Reference2ObjectMap<IGrid, Interaction> interaction = new Reference2ObjectOpenHashMap<>();
    //? if <1.20 {
    private final ReferenceSet<TileEntity> cache = new ReferenceOpenHashSet<>();
    //?} else {
    /*private final ReferenceSet<BlockEntity> cache = new ReferenceOpenHashSet<>();
    *///?}

    {
        scopeNode.defaultReturnValue(Long2ObjectMaps.emptyMap());
        nodeScope.defaultReturnValue(Object2ObjectMaps.emptyMap());
        gridMachineMap.defaultReturnValue(ReferenceSets.emptySet());
    }

    //? if <1.20 {
    public WeakHashMap<TileEntity, ReferenceSet<INode>> getMachineGridMap() {
        return machineGridMap;
    }
    //?} else {
    /*public WeakHashMap<BlockEntity, ReferenceSet<INode>> getMachineGridMap() {
        return machineGridMap;
    }
    *///?}

    public Reference2ObjectMap<IGrid, Interaction> getInteraction() {
        return interaction;
    }

    static void transferEnergy(Collection<IEnergyHandler> send, Collection<IEnergyHandler> receive, Status status, IGrid grid) {
        if (send.isEmpty() || receive.isEmpty()) return;
        var si = send.iterator();
        while (si.hasNext()) {
            var sender = si.next();
            if (receive.isEmpty()) return;
            var ri = receive.iterator();
            while (ri.hasNext()) {
                var receiver = ri.next();
                if (sender.canExtract(receiver) && receiver.canReceive(sender)) {
                    EnergyAmount extractable = sender.canExtractValue();
                    EnergyAmount receivable = receiver.canReceiveValue();
                    try {
                        int compare = extractable.compareTo(receivable);
                        EnergyAmount transferLimit = EnergyAmount.obtain(extractable).min(receivable);
                        try {
                            if (transferLimit.isZero()) {
                                continue;
                            }
                            EnergyAmount extracted = sender.extractEnergy(transferLimit);
                            try {
                                EnergyAmount received = receiver.receiveEnergy(extracted);
                                try {
                                    if (!received.isZero()) {
                                        status.interaction(received, grid);
                                    }
                                } finally {
                                    received.recycle();
                                }
                            } finally {
                                extracted.recycle();
                            }
                        } finally {
                            transferLimit.recycle();
                        }

                        if (compare >= 0 && receiver.getType() != IEnergyHandler.EnergyType.STORAGE) {
                            receiver.recycle();
                            ri.remove();
                        }
                        if (compare <= 0) {
                            sender.recycle();
                            si.remove();
                            break;
                        }
                    } finally {
                        extractable.recycle();
                        receivable.recycle();
                    }
                }
            }
        }
    }

    public void onBlockEntityValidate(BlockEntityLifeCycleEvent.Validate event) {
        if (isClientWorld(event.getWorld())) return;
        if (NetworkManager.INSTANCE.isInit()) {
            addMachine(event.getBlockEntity());
            var node = NetworkManager.INSTANCE.getNodeFromPos(event.getWorld(), event.getPos());
            if (node instanceof IMachineNode im) {
                addMachineNode(im, event.getBlockEntity());
            }
        } else cache.add(event.getBlockEntity());
    }

    public void onBlockEntityInvalidate(BlockEntityLifeCycleEvent.Invalidate event) {
        if (isClientWorld(event.getWorld())) return;
        removeMachine(event.getBlockEntity());
    }

    public void onServerTick() {
        var server = getServer();
        if (server == null || !NetworkManager.INSTANCE.isInit()) return;
        interaction.values().forEach(Interaction::reset);
        var overrideManager = EnergyTypeOverrideManager.get();
        var gridMap = new Reference2ObjectOpenHashMap<IGrid, EnumMap<IEnergyHandler.EnergyType, Set<IEnergyHandler>>>();
        for (var entry : machineGridMap.entrySet()) {
            var te = entry.getKey();
            //? if <1.20 {
            if (!te.getWorld().isBlockLoaded(te.getPos())) continue;
            if (PhaseInterrupterManager.INSTANCE.isBlockedByInterrupter(te.getPos(), te.getWorld())) continue;
            //?} else {
            /*if (!te.getLevel().isLoaded(te.getBlockPos())) continue;
            if (PhaseInterrupterManager.INSTANCE.isBlockedByInterrupter(te.getBlockPos(), te.getLevel())) continue;
            *///?}
            var handler = IEnergyHandler.release(te);

            if (handler == null) {
                continue;
            }

            var type = handler.getType();
            if (overrideManager != null) {
                //? if <1.20 {
                var override = overrideManager.getOverride(getDimensionId(te.getWorld()), te.getPos());
                //?} else {
                /*var override = overrideManager.getOverride(getDimensionId(te.getLevel()), te.getBlockPos());
                *///?}
                if (override != null) type = override;
            }

            if (type == IEnergyHandler.EnergyType.INVALID) {
                handler.recycle();
                continue;
            }

            for (var node : entry.getValue()) {
                if (node.getGrid() == null) continue;
                gridMap.computeIfAbsent(node.getGrid(), g -> new EnumMap<>(IEnergyHandler.EnergyType.class))
                       .computeIfAbsent(type, s -> new ObjectLinkedOpenHashSet<>())
                       .add(handler);
            }
        }

        ReferenceSet<IGrid> processedGrids = new ReferenceOpenHashSet<>();

        for (var e : gridMap.entrySet()) {
            var grid = e.getKey();
            if (processedGrids.contains(grid)) continue;
            var hubNode = grid.getHubNode();
            if (hubNode != null && hubNode.isActive()) {
                var channelId = hubNode.getChannelId();
                if (channelId != null) {
                    var channelGrids = HubChannelManager.INSTANCE.getChannelGrids(channelId);
                    if (channelGrids != null && channelGrids.size() > 1) {
                        var mergedSend = new ObjectLinkedOpenHashSet<IEnergyHandler>();
                        var mergedStorage = new ObjectLinkedOpenHashSet<IEnergyHandler>();
                        var mergedReceive = new ObjectLinkedOpenHashSet<IEnergyHandler>();
                        for (var cg : channelGrids) {
                            var handlers = gridMap.get(cg);
                            if (handlers != null) {
                                mergedSend.addAll(handlers.getOrDefault(IEnergyHandler.EnergyType.SEND, ObjectSets.emptySet()));
                                mergedStorage.addAll(handlers.getOrDefault(IEnergyHandler.EnergyType.STORAGE, ObjectSets.emptySet()));
                                mergedReceive.addAll(handlers.getOrDefault(IEnergyHandler.EnergyType.RECEIVE, ObjectSets.emptySet()));
                            }
                            processedGrids.add(cg);
                        }
                        transferEnergy(mergedSend, mergedReceive, Status.INTERACTION, grid);
                        transferEnergy(mergedStorage, mergedReceive, Status.EXTRACT, grid);
                        transferEnergy(mergedSend, mergedStorage, Status.RECEIVE, grid);
                        continue;
                    }
                }
            }

            processedGrids.add(grid);
            var handlers = e.getValue();
            var send = handlers.getOrDefault(IEnergyHandler.EnergyType.SEND, ObjectSets.emptySet());
            var storage = handlers.getOrDefault(IEnergyHandler.EnergyType.STORAGE, ObjectSets.emptySet());
            var receive = handlers.getOrDefault(IEnergyHandler.EnergyType.RECEIVE, ObjectSets.emptySet());

            transferEnergy(send, receive, Status.INTERACTION, grid);
            transferEnergy(storage, receive, Status.EXTRACT, grid);
            transferEnergy(send, storage, Status.RECEIVE, grid);
        }

        ChargingManager.INSTANCE.onServerTick(server, gridMap);

        for (var value : gridMap.values()) {
            for (var handlers : value.values()) {
                for (var handler : handlers) {
                    handler.recycle();
                }
            }
        }
    }

    //? if <1.20 {
    public void addMachine(TileEntity blockEntity) {
    //?} else {
    /*public void addMachine(BlockEntity blockEntity) {
    *///?}
        if (!RegistryEnergyHandler.isEnergyTileEntity(blockEntity)) return;
        if (RegistryEnergyHandler.isBlack(blockEntity)) return;
        //? if <1.20 {
        var pos = blockEntity.getPos();
        long chunkCoord = Functions.mergeChunkCoords(pos);

        var dim = getDimensionId(blockEntity.getWorld());
        //?} else {
        /*var pos = blockEntity.getBlockPos();
        long chunkCoord = Functions.mergeChunkCoords(pos);

        var dim = getDimensionId(blockEntity.getLevel());
        *///?}
        var map = scopeNode.get(dim);
        if (map == scopeNode.defaultReturnValue()) {
            scopeNode.put(dim, map = new Long2ObjectOpenHashMap<>());
            map.defaultReturnValue(ReferenceSets.emptySet());
        }
        ReferenceSet<IEnergySupplyNode> set = map.get(chunkCoord);
        if (!set.isEmpty()) {
            var s = machineGridMap.get(blockEntity);
            if (s == null) s = new ReferenceOpenHashSet<>();
            for (var node : set) {
                if (!node.supplyScopeCheck(pos)) continue;
                if (node.isBlacklisted(blockEntity)) continue;

                var set1 = gridMachineMap.get(node);
                if (set1 == gridMachineMap.defaultReturnValue()) {
                    gridMachineMap.put(node, set1 = Collections.newSetFromMap(new WeakHashMap<>()));
                }
                s.add(node);
                set1.add(blockEntity);

                //? if <1.20 {
                var players = NodeNetworkRendering.getPlayers(node.getGrid());
                if (players != null && !players.isEmpty()) {
                    for (var player : players) {
                        CirculationFlowNetworks.NET_CHANNEL.sendTo(new NodeNetworkRendering(player, blockEntity, node, NodeNetworkRendering.MACHINE_ADD), player);
                    }
                }
                //?}
            }
            if (s.isEmpty()) return;
            machineGridMap.putIfAbsent(blockEntity, s);
        }
    }

    //? if <1.20 {
    public void removeMachine(TileEntity blockEntity) {
    //?} else {
    /*public void removeMachine(BlockEntity blockEntity) {
    *///?}
        var set = machineGridMap.remove(blockEntity);
        if (set == null || set.isEmpty()) return;
        for (var node : set) {
            gridMachineMap.get(node).remove(blockEntity);

            //? if <1.20 {
            var players = NodeNetworkRendering.getPlayers(node.getGrid());
            if (players != null && !players.isEmpty()) {
                for (var player : players) {
                    CirculationFlowNetworks.NET_CHANNEL.sendTo(new NodeNetworkRendering(player, blockEntity, node, NodeNetworkRendering.MACHINE_REMOVE), player);
                }
            }
            //?}
        }
    }

    //? if <1.20 {
    public void addMachineNode(IMachineNode iMachineNode, TileEntity blockEntity) {
    //?} else {
    /*public void addMachineNode(IMachineNode iMachineNode, BlockEntity blockEntity) {
    *///?}
        var allConnected = new ReferenceOpenHashSet<INode>();
        for (INode candidate : NetworkManager.INSTANCE.getNodesCoveringPosition(iMachineNode.getWorld(), iMachineNode.getPos())) {
            if (candidate.linkScopeCheck(iMachineNode) != INode.LinkType.DISCONNECT) {
                allConnected.add(candidate);
            }
        }

        if (!allConnected.isEmpty()) {
            var s = machineGridMap.get(blockEntity);
            if (s == null) s = new ReferenceOpenHashSet<>();
            for (var node : allConnected) {
                var set1 = gridMachineMap.get(node);
                if (set1 == gridMachineMap.defaultReturnValue()) {
                    gridMachineMap.put(node, set1 = Collections.newSetFromMap(new WeakHashMap<>()));
                }
                s.add(node);
                set1.add(blockEntity);
            }
            if (s.isEmpty()) return;
            machineGridMap.putIfAbsent(blockEntity, s);
        }
    }

    public void addNode(INode node) {
        if (node instanceof IEnergySupplyNode energySupplyNode) {
            int nodeX = energySupplyNode.getPos().getX();
            int nodeZ = energySupplyNode.getPos().getZ();
            int range = (int) energySupplyNode.getEnergyScope();
            int minChunkX = (nodeX - range) >> 4;
            int maxChunkX = (nodeX + range) >> 4;
            int minChunkZ = (nodeZ - range) >> 4;
            int maxChunkZ = (nodeZ + range) >> 4;
            LongSet chunksCovered = new LongOpenHashSet();

            int dimId = getDimensionId(node.getWorld());

            Long2ObjectMap<ReferenceSet<IEnergySupplyNode>> map = scopeNode.get(dimId);
            if (map == scopeNode.defaultReturnValue()) {
                Long2ObjectMap<ReferenceSet<IEnergySupplyNode>> newMap = new Long2ObjectOpenHashMap<>();
                newMap.defaultReturnValue(ReferenceSets.emptySet());
                scopeNode.put(dimId, map = newMap);
            }

            for (int cx = minChunkX; cx <= maxChunkX; ++cx) {
                for (int cz = minChunkZ; cz <= maxChunkZ; ++cz) {
                    long chunkCoord = Functions.mergeChunkCoords(cx, cz);
                    chunksCovered.add(chunkCoord);

                    ReferenceSet<IEnergySupplyNode> set = map.get(chunkCoord);
                    if (set == map.defaultReturnValue()) {
                        map.put(chunkCoord, set = new ReferenceOpenHashSet<>());
                    }
                    set.add(energySupplyNode);

                    //? if <1.20 {
                    var chunk = node.getWorld().getChunkProvider().getLoadedChunk(cx, cz);
                    if (chunk == null || chunk.isEmpty()) {
                        continue;
                    }
                    var set2 = gridMachineMap.get(node);
                    for (var tileEntity : chunk.getTileEntityMap().values()) {
                        if (!energySupplyNode.supplyScopeCheck(tileEntity.getPos())) continue;
                        if (RegistryEnergyHandler.isBlack(tileEntity)) continue;
                        if (energySupplyNode.isBlacklisted(tileEntity)) continue;
                        if (RegistryEnergyHandler.isEnergyTileEntity(tileEntity)) {
                            if (set2 == gridMachineMap.defaultReturnValue()) {
                                gridMachineMap.put(energySupplyNode, set2 = Collections.newSetFromMap(new WeakHashMap<>()));
                            }
                            set2.add(tileEntity);

                            var set3 = machineGridMap.get(tileEntity);
                            if (set3 == null) {
                                machineGridMap.put(tileEntity, set3 = new ReferenceOpenHashSet<>());
                            }
                            set3.add(energySupplyNode);
                        }
                    }
                    //?} else {
                    /*var chunk = node.getWorld().getChunkSource().getChunkNow(cx, cz);
                    if (chunk == null) {
                        continue;
                    }
                    var set2 = gridMachineMap.get(node);
                    for (var blockEntity : chunk.getBlockEntities().values()) {
                        if (!energySupplyNode.supplyScopeCheck(blockEntity.getBlockPos())) continue;
                        if (RegistryEnergyHandler.isBlack(blockEntity)) continue;
                        if (energySupplyNode.isBlacklisted(blockEntity)) continue;
                        if (RegistryEnergyHandler.isEnergyTileEntity(blockEntity)) {
                            if (set2 == gridMachineMap.defaultReturnValue()) {
                                gridMachineMap.put(energySupplyNode, set2 = Collections.newSetFromMap(new WeakHashMap<>()));
                            }
                            set2.add(blockEntity);

                            var set3 = machineGridMap.get(blockEntity);
                            if (set3 == null) {
                                machineGridMap.put(blockEntity, set3 = new ReferenceOpenHashSet<>());
                            }
                            set3.add(energySupplyNode);
                        }
                    }
                    *///?}
                }
            }

            Object2ObjectMap<IEnergySupplyNode, LongSet> nodeScopeMap = nodeScope.get(dimId);
            if (nodeScopeMap == nodeScope.defaultReturnValue()) {
                nodeScope.put(dimId, nodeScopeMap = new Object2ObjectOpenHashMap<>());
            }
            nodeScopeMap.put(energySupplyNode, LongSets.unmodifiable(chunksCovered));
        }
    }

    void initGrid(Collection<NetworkManager.GridEntry> entries) {
        for (var entry : entries) {
            var dim = entry.dimId();
            if (entry.grid().getNodes().isEmpty()) continue;
            for (INode node : entry.grid().getNodes()) {
                if (!(node instanceof IEnergySupplyNode energySupplyNode)) continue;

                int nodeX = energySupplyNode.getPos().getX();
                int nodeZ = energySupplyNode.getPos().getZ();
                int range = (int) energySupplyNode.getEnergyScope();
                int minChunkX = (nodeX - range) >> 4, maxChunkX = (nodeX + range) >> 4;
                int minChunkZ = (nodeZ - range) >> 4, maxChunkZ = (nodeZ + range) >> 4;

                LongSet chunksCovered = new LongOpenHashSet();

                Long2ObjectMap<ReferenceSet<IEnergySupplyNode>> map = scopeNode.get(dim);
                if (map == scopeNode.defaultReturnValue()) {
                    Long2ObjectMap<ReferenceSet<IEnergySupplyNode>> newMap = new Long2ObjectOpenHashMap<>();
                    newMap.defaultReturnValue(ReferenceSets.emptySet());
                    scopeNode.put(dim, map = newMap);
                }

                for (int cx = minChunkX; cx <= maxChunkX; ++cx) {
                    for (int cz = minChunkZ; cz <= maxChunkZ; ++cz) {
                        long chunkCoord = Functions.mergeChunkCoords(cx, cz);
                        chunksCovered.add(chunkCoord);

                        ReferenceSet<IEnergySupplyNode> set = map.get(chunkCoord);
                        if (set == map.defaultReturnValue()) {
                            map.put(chunkCoord, set = new ReferenceOpenHashSet<>());
                        }
                        set.add(energySupplyNode);
                    }
                }

                Object2ObjectMap<IEnergySupplyNode, LongSet> nodeScopeMap = nodeScope.get(dim);
                if (nodeScopeMap == nodeScope.defaultReturnValue()) {
                    nodeScope.put(dim, nodeScopeMap = new Object2ObjectOpenHashMap<>());
                }
                nodeScopeMap.put(energySupplyNode, LongSets.unmodifiable(chunksCovered));
            }
        }

        for (var te : cache) {
            addMachine(te);
            //? if <1.20 {
            var node = NetworkManager.INSTANCE.getNodeFromPos(te.getWorld(), te.getPos());
            //?} else {
            /*var node = NetworkManager.INSTANCE.getNodeFromPos(te.getLevel(), te.getBlockPos());
            *///?}
            if (node instanceof IMachineNode im) {
                addMachineNode(im, te);
            }
        }
        cache.clear();
    }

    public void removeNode(INode node) {
        if (node instanceof IEnergySupplyNode removedNode) {
            var world = removedNode.getWorld();
            int dimId = getDimensionId(world);

            var nodeScopeMap = nodeScope.get(dimId);
            if (nodeScopeMap == nodeScope.defaultReturnValue()) return;

            LongSet coveredChunks = nodeScopeMap.remove(removedNode);
            if (coveredChunks == null || coveredChunks.isEmpty()) return;

            var scopeMap = scopeNode.get(dimId);
            if (scopeMap == scopeNode.defaultReturnValue()) return;

            for (long coveredChunk : coveredChunks) {
                var set = scopeMap.get(coveredChunk);
                if (set == scopeMap.defaultReturnValue()) {
                    continue;
                }
                if (set.size() == 1) scopeMap.remove(coveredChunk);
                else set.remove(removedNode);
            }

            var c = gridMachineMap.remove(removedNode);
            if (c == null || c.isEmpty()) return;
            for (var te : c) {
                var set = machineGridMap.get(te);
                if (set == null) {
                    continue;
                }
                if (set.size() == 1) machineGridMap.remove(te);
                else set.remove(removedNode);
            }
        }
    }

    public void onServerStop() {
        scopeNode.clear();
        nodeScope.clear();
        gridMachineMap.clear();
        machineGridMap.clear();
        interaction.clear();
    }

    //? if <1.20 {
    public @Nonnull ReferenceSet<IEnergySupplyNode> getEnergyNodes(World world, BlockPos pos) {
    //?} else {
    /*public @Nonnull ReferenceSet<IEnergySupplyNode> getEnergyNodes(Level world, BlockPos pos) {
    *///?}
        return getEnergyNodes(world, pos.getX() >> 4, pos.getZ() >> 4);
    }

    //? if <1.20 {
    public @Nonnull ReferenceSet<IEnergySupplyNode> getEnergyNodes(World world, int chunkX, int chunkZ) {
    //?} else {
    /*public @Nonnull ReferenceSet<IEnergySupplyNode> getEnergyNodes(Level world, int chunkX, int chunkZ) {
    *///?}
        var map = scopeNode.get(getDimensionId(world));
        return map.get(Functions.mergeChunkCoords(chunkX, chunkZ));
    }

    //? if <1.20 {
    public @Nonnull Set<TileEntity> getMachinesSuppliedBy(IEnergySupplyNode node) {
    //?} else {
    /*public @Nonnull Set<BlockEntity> getMachinesSuppliedBy(IEnergySupplyNode node) {
    *///?}
        return gridMachineMap.getOrDefault(node, Collections.emptySet());
    }

    //? if <1.20 {
    private static MinecraftServer getServer() {
        return CirculationFlowNetworks.server;
    }

    private static boolean isClientWorld(World world) {
        return world.isRemote;
    }

    private static int getDimensionId(World world) {
        return world.provider.getDimension();
    }
    //?} else if <1.21 {
    /*private static MinecraftServer getServer() {
        return net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
    }

    private static boolean isClientWorld(Level world) {
        return world.isClientSide;
    }

    private static int getDimensionId(Level world) {
        return world.dimension().location().hashCode();
    }
    *///?} else {
    /*private static MinecraftServer getServer() {
        return net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
    }

    private static boolean isClientWorld(Level world) {
        return world.isClientSide;
    }

    private static int getDimensionId(Level world) {
        return world.dimension().location().hashCode();
    }
    *///?}

    enum Status {
        EXTRACT,
        INTERACTION,
        RECEIVE;

        private void interaction(EnergyAmount value, IGrid grid) {
            var i = EnergyMachineManager.INSTANCE.interaction.get(grid);
            if (i == null) {
                return;
            }
            switch (this) {
                case INTERACTION -> {
                    i.input.add(value);
                    i.output.add(value);
                }
                case EXTRACT -> i.output.add(value);
                case RECEIVE -> i.input.add(value);
            }
        }
    }

    @SuppressWarnings("unused")
    public static class Interaction {
        private final EnergyAmount input = new EnergyAmount(0L);
        private final EnergyAmount output = new EnergyAmount(0L);

        public EnergyAmount getInput() {
            return input;
        }

        public EnergyAmount getOutput() {
            return output;
        }

        private void reset() {
            input.setZero();
            output.setZero();
        }
    }
}