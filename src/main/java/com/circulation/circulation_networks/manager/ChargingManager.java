package com.circulation.circulation_networks.manager;

import baubles.api.BaublesApi;
import com.circulation.circulation_networks.api.IEnergyHandler;
import com.circulation.circulation_networks.api.IGrid;
import com.circulation.circulation_networks.api.node.IChargingNode;
import com.circulation.circulation_networks.api.node.INode;
import com.circulation.circulation_networks.utils.Functions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.objects.ReferenceSets;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;

import static com.circulation.circulation_networks.CirculationFlowNetworks.server;
import static com.circulation.circulation_networks.manager.EnergyMachineManager.transferEnergy;

public final class ChargingManager {

    public static final ChargingManager INSTANCE = new ChargingManager();
    private static final boolean loadBaubles = Loader.isModLoaded("baubles");

    private final Int2ObjectMap<Long2ObjectMap<ReferenceSet<IChargingNode>>> scopeNode = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Object2ObjectMap<IChargingNode, LongSet>> nodeScope = new Int2ObjectOpenHashMap<>();

    @Optional.Method(modid = "baubles")
    private static void checkBaubles(Collection<IEnergyHandler> invs, EntityPlayer player) {
        var h = BaublesApi.getBaublesHandler(player);
        for (var i = 0; i < h.getSlots(); i++) {
            var stack = h.getStackInSlot(i);
            var handler = IEnergyHandler.release(stack);
            if (handler == null) continue;
            if (handler.canReceive()) {
                invs.add(handler);
                continue;
            }
            handler.recycle();
        }
    }

    public void onServerTick(Reference2ObjectOpenHashMap<IGrid, EnumMap<IEnergyHandler.EnergyType, Set<IEnergyHandler>>> machineMap) {
        var gridMap = new Reference2ObjectOpenHashMap<IGrid, List<IEnergyHandler>>();
        gridMap.defaultReturnValue(ObjectLists.emptyList());
        var players = server.getPlayerList().getPlayers();
        p:
        for (var player : players) {
            var map = scopeNode.get(player.dimension);
            if (map != null) {
                var pos = player.getPosition();
                var set = map.get(Functions.mergeChunkCoords(pos));
                if (set.isEmpty()) continue;
                var invs = new ObjectArrayList<IEnergyHandler>();
                var w = false;
                for (var node : set) {
                    if (gridMap.containsKey(node.getGrid())) continue;
                    if (!node.chargingScopeCheck(pos)) continue;
                    if (!w) {
                        var inv = player.inventory;
                        for (var i = 0; i < inv.getSizeInventory(); i++) {
                            var stack = inv.getStackInSlot(i);
                            var handler = IEnergyHandler.release(stack);
                            if (handler == null) continue;
                            if (handler.canReceive()) {
                                invs.add(handler);
                                continue;
                            }
                            handler.recycle();
                        }
                        if (loadBaubles) checkBaubles(invs, player);
                        if (invs.isEmpty()) continue p;
                        w = true;
                    }
                    gridMap.put(node.getGrid(), invs);
                }
            }
        }

        for (var entry : gridMap.entrySet()) {
            var grid = entry.getKey();
            var receive = entry.getValue();

            var m = machineMap.get(grid);
            if (m != null) {
                var send = m.getOrDefault(IEnergyHandler.EnergyType.SEND, ObjectSets.emptySet());
                transferEnergy(send, receive, EnergyMachineManager.Status.EXTRACT, grid);

                var storage = m.getOrDefault(IEnergyHandler.EnergyType.STORAGE, ObjectSets.emptySet());
                transferEnergy(storage, receive, EnergyMachineManager.Status.EXTRACT, grid);
            }
        }

        for (var value : gridMap.values()) {
            for (var handler : value) {
                handler.recycle();
            }
        }
    }

    public void addNode(INode node) {
        if (node instanceof IChargingNode chargingNode) {
            int nodeX = chargingNode.getPos().getX();
            int nodeZ = chargingNode.getPos().getZ();
            int range = (int) chargingNode.getChargingScope();
            int minChunkX = (nodeX - range) >> 4;
            int maxChunkX = (nodeX + range) >> 4;
            int minChunkZ = (nodeZ - range) >> 4;
            int maxChunkZ = (nodeZ + range) >> 4;
            LongSet chunksCovered = new LongOpenHashSet();

            int dimId = node.getWorld().provider.getDimension();

            Long2ObjectMap<ReferenceSet<IChargingNode>> map = scopeNode.get(dimId);
            if (map == scopeNode.defaultReturnValue()) {
                Long2ObjectMap<ReferenceSet<IChargingNode>> newMap = new Long2ObjectOpenHashMap<>();
                newMap.defaultReturnValue(ReferenceSets.emptySet());
                scopeNode.put(dimId, map = newMap);
            }

            for (int cx = minChunkX; cx <= maxChunkX; ++cx) {
                for (int cz = minChunkZ; cz <= maxChunkZ; ++cz) {
                    long chunkCoord = Functions.mergeChunkCoords(cx, cz);
                    chunksCovered.add(chunkCoord);

                    ReferenceSet<IChargingNode> set = map.get(chunkCoord);
                    if (set == map.defaultReturnValue()) {
                        map.put(chunkCoord, set = new ReferenceOpenHashSet<>());
                    }
                    set.add(chargingNode);
                }
            }

            Object2ObjectMap<IChargingNode, LongSet> nodeScopeMap = nodeScope.get(dimId);
            if (nodeScopeMap == nodeScope.defaultReturnValue()) {
                nodeScope.put(dimId, nodeScopeMap = new Object2ObjectOpenHashMap<>());
            }
            nodeScopeMap.put(chargingNode, LongSets.unmodifiable(chunksCovered));
        }
    }

    public void removeNode(INode node) {
        if (node instanceof IChargingNode chargingNode) {
            var world = chargingNode.getWorld();
            int dimId = world.provider.getDimension();

            var nodeScopeMap = nodeScope.get(dimId);
            if (nodeScopeMap == nodeScope.defaultReturnValue()) return;

            LongSet coveredChunks = nodeScopeMap.remove(chargingNode);
            if (coveredChunks == null || coveredChunks.isEmpty()) return;

            var scopeMap = scopeNode.get(dimId);
            if (scopeMap == scopeNode.defaultReturnValue()) return;

            for (long coveredChunk : coveredChunks) {
                var set = scopeMap.get(coveredChunk);
                if (set == scopeMap.defaultReturnValue()) {
                    continue;
                }
                if (set.size() == 1) scopeMap.remove(coveredChunk);
                else set.remove(chargingNode);
            }
        }
    }

    public void onServerStop() {
        scopeNode.clear();
        nodeScope.clear();
    }
}
