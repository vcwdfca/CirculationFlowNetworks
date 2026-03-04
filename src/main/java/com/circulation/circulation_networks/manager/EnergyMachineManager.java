package com.circulation.circulation_networks.manager;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import com.circulation.circulation_networks.api.IEnergyHandler;
import com.circulation.circulation_networks.api.IGrid;
import com.circulation.circulation_networks.api.node.IEnergySupplyNode;
import com.circulation.circulation_networks.api.node.IMachineNode;
import com.circulation.circulation_networks.api.node.INode;
import com.circulation.circulation_networks.events.TileEntityLifeCycleEvent;
import com.circulation.circulation_networks.packets.NodeNetworkRendering;
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
import lombok.Getter;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.circulation.circulation_networks.CirculationFlowNetworks.server;

public final class EnergyMachineManager {

    public static final EnergyMachineManager INSTANCE = new EnergyMachineManager();
    private final Int2ObjectMap<Long2ObjectMap<ReferenceSet<IEnergySupplyNode>>> scopeNode = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Object2ObjectMap<IEnergySupplyNode, LongSet>> nodeScope = new Int2ObjectOpenHashMap<>();
    private final Reference2ObjectMap<INode, Set<TileEntity>> gridMachineMap = new Reference2ObjectOpenHashMap<>();
    @Getter
    private final WeakHashMap<TileEntity, ReferenceSet<INode>> machineGridMap = new WeakHashMap<>();
    @Getter
    private final Reference2ObjectMap<IGrid, Interaction> interaction = new Reference2ObjectOpenHashMap<>();
    private final ReferenceSet<TileEntity> cache = new ReferenceOpenHashSet<>();

    {
        scopeNode.defaultReturnValue(Long2ObjectMaps.emptyMap());
        nodeScope.defaultReturnValue(Object2ObjectMaps.emptyMap());
        gridMachineMap.defaultReturnValue(ReferenceSets.emptySet());
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
                if (sender.canExtract() && receiver.canReceive()) {
                    var e = sender.canExtractValue();
                    var r = receiver.canReceiveValue();
                    if (e > r) {
                        if (r != 0) status.interaction(receiver.receiveEnergy(sender.extractEnergy(r)), grid);
                        if (receiver.getType() != IEnergyHandler.EnergyType.STORAGE) {
                            receiver.recycle();
                            ri.remove();
                        }
                    } else if (e == r) {
                        if (e != 0) status.interaction(receiver.receiveEnergy(sender.extractEnergy(r)), grid);
                        sender.recycle();
                        si.remove();
                        if (receiver.getType() != IEnergyHandler.EnergyType.STORAGE) {
                            receiver.recycle();
                            ri.remove();
                        }
                        break;
                    } else {
                        if (e != 0) status.interaction(receiver.receiveEnergy(sender.extractEnergy(e)), grid);
                        sender.recycle();
                        si.remove();
                        break;
                    }
                }
            }
        }
    }

    public void onTileEntityValidate(TileEntityLifeCycleEvent.Validate event) {
        if (event.getWorld().isRemote) return;
        if (NetworkManager.INSTANCE.isInit()) {
            addMachine(event.getTileEntity());
            var node = NetworkManager.INSTANCE.getNodeFromPos(event.getWorld(), event.getPos());
            if (node instanceof IMachineNode im) {
                addMachineNode(im, event.getTileEntity());
            }
        } else cache.add(event.getTileEntity());
    }

    public void onTileEntityInvalidate(TileEntityLifeCycleEvent.Invalidate event) {
        if (event.getWorld().isRemote) return;
        removeMachine(event.getTileEntity());
    }

    public void onServerTick() {
        if (server == null || !NetworkManager.INSTANCE.isInit()) return;
        interaction.values().forEach(Interaction::reset);
        var overrideManager = EnergyTypeOverrideManager.get();
        var gridMap = new Reference2ObjectOpenHashMap<IGrid, EnumMap<IEnergyHandler.EnergyType, Set<IEnergyHandler>>>();
        for (var entry : machineGridMap.entrySet()) {
            var te = entry.getKey();
            if (!te.getWorld().isBlockLoaded(te.getPos())) continue;
            if (PhaseInterrupterManager.INSTANCE.isBlockedByInterrupter(te.getPos(), te.getWorld())) continue;
            var handler = IEnergyHandler.release(te);

            if (handler == null) {
                continue;
            }

            var type = handler.getType();
            if (overrideManager != null) {
                var override = overrideManager.getOverride(te.getWorld().provider.getDimension(), te.getPos());
                if (override != null) type = override;
            }

            if (type == IEnergyHandler.EnergyType.INVALID) {
                handler.recycle();
                continue;
            }

            for (var node : entry.getValue()) {
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
            if (hubNode != null) {
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

        ChargingManager.INSTANCE.onServerTick(gridMap);

        for (var value : gridMap.values()) {
            for (var handlers : value.values()) {
                for (var handler : handlers) {
                    handler.recycle();
                }
            }
        }
    }

    public void addMachine(TileEntity tileEntity) {
        if (!RegistryEnergyHandler.isEnergyTileEntity(tileEntity)) return;
        if (RegistryEnergyHandler.isBlack(tileEntity)) return;
        var pos = tileEntity.getPos();
        long chunkCoord = Functions.mergeChunkCoords(pos);

        var dim = tileEntity.getWorld().provider.getDimension();
        var map = scopeNode.get(dim);
        if (map == scopeNode.defaultReturnValue()) {
            scopeNode.put(dim, map = new Long2ObjectOpenHashMap<>());
            map.defaultReturnValue(ReferenceSets.emptySet());
        }
        ReferenceSet<IEnergySupplyNode> set = map.get(chunkCoord);
        if (!set.isEmpty()) {
            var s = machineGridMap.get(tileEntity);
            if (s == null) s = new ReferenceOpenHashSet<>();
            for (var node : set) {
                if (!node.supplyScopeCheck(pos)) continue;

                var set1 = gridMachineMap.get(node);
                if (set1 == gridMachineMap.defaultReturnValue()) {
                    gridMachineMap.put(node, set1 = Collections.newSetFromMap(new WeakHashMap<>()));
                }
                s.add(node);
                set1.add(tileEntity);

                var players = NodeNetworkRendering.getPlayers(node.getGrid());
                if (players != null && !players.isEmpty()) {
                    for (var player : players) {
                        CirculationFlowNetworks.NET_CHANNEL.sendTo(new NodeNetworkRendering(player, tileEntity, node, NodeNetworkRendering.MACHINE_ADD), player);
                    }
                }
            }
            if (s.isEmpty()) return;
            machineGridMap.putIfAbsent(tileEntity, s);
        }
    }

    public void removeMachine(TileEntity tileEntity) {
        var set = machineGridMap.remove(tileEntity);
        if (set == null || set.isEmpty()) return;
        for (var node : set) {
            gridMachineMap.get(node).remove(tileEntity);

            var players = NodeNetworkRendering.getPlayers(node.getGrid());
            if (players != null && !players.isEmpty()) {
                for (var player : players) {
                    CirculationFlowNetworks.NET_CHANNEL.sendTo(new NodeNetworkRendering(player, tileEntity, node, NodeNetworkRendering.MACHINE_REMOVE), player);
                }
            }
        }
    }

    public void addMachineNode(IMachineNode iMachineNode, TileEntity te) {
        var allConnected = new ReferenceOpenHashSet<INode>();
        for (INode candidate : NetworkManager.INSTANCE.getNodesCoveringPosition(iMachineNode.getWorld(), iMachineNode.getPos())) {
            if (candidate.linkScopeCheck(iMachineNode) != INode.LinkType.DISCONNECT) {
                allConnected.add(candidate);
            }
        }

        if (!allConnected.isEmpty()) {
            var s = machineGridMap.get(te);
            if (s == null) s = new ReferenceOpenHashSet<>();
            for (var node : allConnected) {
                var set1 = gridMachineMap.get(node);
                if (set1 == gridMachineMap.defaultReturnValue()) {
                    gridMachineMap.put(node, set1 = Collections.newSetFromMap(new WeakHashMap<>()));
                }
                s.add(node);
                set1.add(te);
            }
            if (s.isEmpty()) return;
            machineGridMap.putIfAbsent(te, s);
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

            int dimId = node.getWorld().provider.getDimension();

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

                    Chunk chunk = node.getWorld().getChunkProvider().getLoadedChunk(cx, cz);
                    if (chunk == null || chunk.isEmpty()) {
                        continue;
                    }
                    var set2 = gridMachineMap.get(node);
                    for (TileEntity tileEntity : chunk.getTileEntityMap().values()) {
                        if (!energySupplyNode.supplyScopeCheck(tileEntity.getPos())) continue;
                        if (RegistryEnergyHandler.isBlack(tileEntity)) continue;
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
                }
            }

            Object2ObjectMap<IEnergySupplyNode, LongSet> nodeScopeMap = nodeScope.get(dimId);
            if (nodeScopeMap == nodeScope.defaultReturnValue()) {
                nodeScope.put(dimId, nodeScopeMap = new Object2ObjectOpenHashMap<>());
            }
            nodeScopeMap.put(energySupplyNode, LongSets.unmodifiable(chunksCovered));
        }
    }

    void initGrid(ConcurrentLinkedQueue<NetworkManager.GridEntry> entries) {
        for (var entry : entries) {
            var dim = entry.dimId();
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
            var node = NetworkManager.INSTANCE.getNodeFromPos(te.getWorld(), te.getPos());
            if (node instanceof IMachineNode im) {
                addMachineNode(im, te);
            }
        }
        cache.clear();
    }

    public void removeNode(INode node) {
        if (node instanceof IEnergySupplyNode removedNode) {
            var world = removedNode.getWorld();
            int dimId = world.provider.getDimension();

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

            Set<TileEntity> c = gridMachineMap.remove(removedNode);
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

    /**
     * 获取范围包含此位置所在区块的所有节点
     *
     * @param pos 目标位置
     * @return 可能覆盖该位置的节点集合
     */
    public @Nonnull ReferenceSet<IEnergySupplyNode> getEnergyNodes(World world, BlockPos pos) {
        return getEnergyNodes(world, pos.getX() >> 4, pos.getZ() >> 4);
    }

    /**
     * 获取范围包含此区块的所有节点
     *
     * @param chunkX 目标区块的X坐标
     * @param chunkZ 目标区块的Z坐标
     * @return 可能覆盖该位置的节点集合
     */
    public @Nonnull ReferenceSet<IEnergySupplyNode> getEnergyNodes(World world, int chunkX, int chunkZ) {
        var map = scopeNode.get(world.provider.getDimension());
        return map.get(Functions.mergeChunkCoords(chunkX, chunkZ));
    }

    /**
     * @param node 节点，必须是能量节点以防获取到了节点机器
     * @return 节点所链接的所有设备
     */
    public @Nonnull Set<TileEntity> getMachinesSuppliedBy(IEnergySupplyNode node) {
        return gridMachineMap.getOrDefault(node, Collections.emptySet());
    }

    enum Status {
        EXTRACT,
        INTERACTION,
        RECEIVE;

        private void interaction(long value, IGrid grid) {
            var i = EnergyMachineManager.INSTANCE.interaction.get(grid);
            switch (this) {
                case INTERACTION -> {
                    i.input += value;
                    i.output += value;
                }
                case EXTRACT -> i.output += value;
                case RECEIVE -> i.input += value;
            }
        }
    }

    public static class Interaction {
        @Getter
        private double input = 0;
        @Getter
        private double output = 0;

        private void reset() {
            input = 0;
            output = 0;
        }
    }

}