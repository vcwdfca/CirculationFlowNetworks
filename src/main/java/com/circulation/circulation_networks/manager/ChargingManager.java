package com.circulation.circulation_networks.manager;

import com.circulation.circulation_networks.api.EnergyAmount;
import com.circulation.circulation_networks.api.IEnergyHandler;
import com.circulation.circulation_networks.api.IGrid;
import com.circulation.circulation_networks.api.hub.ChargingDefinition;
import com.circulation.circulation_networks.api.hub.HubPermissionLevel;
import com.circulation.circulation_networks.api.node.IChargingNode;
import com.circulation.circulation_networks.api.node.IHubNode;
import com.circulation.circulation_networks.api.node.INode;
import com.circulation.circulation_networks.items.ItemDimensionalChargingPlugin;
import com.circulation.circulation_networks.items.ItemWideAreaChargingPlugin;
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
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.objects.ReferenceSets;
//? if <1.20 {
import baubles.api.BaublesApi;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
//?} else if <1.21 {
/*import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
*///?} else {
/*import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
*///?}

import net.minecraft.server.MinecraftServer;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.circulation.circulation_networks.manager.EnergyMachineManager.transferEnergy;

public final class ChargingManager {

    public static final ChargingManager INSTANCE = new ChargingManager();

    enum ChargingPluginScope { NONE, WIDE_AREA, DIMENSIONAL }

    //? if <1.20 {
    private static final boolean loadAccessoryIntegration = Loader.isModLoaded("baubles");
    //?} else if <1.21 {
    /*private static final boolean loadAccessoryIntegration = ModList.get().isLoaded("curios");
     *///?} else {
    /*private static final boolean loadAccessoryIntegration = ModList.get().isLoaded("curios");
     *///?}

    private final Int2ObjectMap<Long2ObjectMap<ReferenceSet<IChargingNode>>> scopeNode = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Object2ObjectMap<IChargingNode, LongSet>> nodeScope = new Int2ObjectOpenHashMap<>();

    //? if <1.20 {
    @Optional.Method(modid = "baubles")
    private static void checkAccessory(Collection<IEnergyHandler> invs, EntityPlayer player) {
        //?} else {
    /*private static void checkAccessory(Collection<IEnergyHandler> invs, Player player) {
        *///?}
        //? if <1.20 {
        var h = BaublesApi.getBaublesHandler(player);
        for (var i = 0; i < h.getSlots(); i++) {
            var stack = h.getStackInSlot(i);
            var handler = IEnergyHandler.release(stack);
            if (handler == null) continue;
            if (canReceiveMore(handler)) {
                invs.add(handler);
                continue;
            }
            handler.recycle();
        }
        //?} else {
        /*CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            var equippedCurios = handler.getEquippedCurios();
            for (int i = 0; i < equippedCurios.getSlots(); i++) {
                var stack = equippedCurios.getStackInSlot(i);
                var energyHandler = IEnergyHandler.release(stack);
                if (energyHandler == null) continue;
                if (canReceiveMore(energyHandler)) {
                    invs.add(energyHandler);
                    continue;
                }
                energyHandler.recycle();
            }
        });
        *///?}
    }

    //~ if >=1.20 ' EntityPlayer player' -> ' Player player' {
    //~ if >=1.20 '.getHeldItemOffhand()' -> '.getOffhandItem()' {
    //~ if >=1.20 '.getHeldItemMainhand()' -> '.getMainHandItem()' {
    //~ if >=1.20 '.inventory.armorInventory' -> '.getInventory().armor' {
    //~ if >=1.20 '.getUniqueID()' -> '.getUUID()' {
    private static List<IEnergyHandler> collectChargeablesForGrid(IGrid grid, EntityPlayer player, PlayerChargeState state) {
        var preferences = resolveChargingPreferences(grid, player);
        if (preferences.isEmpty()) {
            return ObjectLists.emptyList();
        }

        var result = new ObjectArrayList<IEnergyHandler>();

        if (preferences.contains(ChargingDefinition.INVENTORY)) {
            collectFromSlots(result, state.cache, ChargingDefinition.INVENTORY, state.inventory, 9, state.inventory.length);
        }
        if (preferences.contains(ChargingDefinition.OFF_HAND)) {
            collectFromStackWithCache(result, state.cache, ChargingDefinition.OFF_HAND, player.getHeldItemOffhand());
        }
        if (preferences.contains(ChargingDefinition.HOTBAR)) {
            collectFromSlots(result, state.cache, ChargingDefinition.HOTBAR, state.inventory, 0, 9);
        } else {
            if (preferences.contains(ChargingDefinition.MAIN_HAND)) {
                collectFromStackWithCache(result, state.cache, ChargingDefinition.MAIN_HAND, player.getHeldItemMainhand());
            }
        }
        if (preferences.contains(ChargingDefinition.ARMOR)) {
            var armorInventory = player.inventory.armorInventory;
            collectFromSlots(result, state.cache, ChargingDefinition.ARMOR, armorInventory.toArray(new ItemStack[0]), 0, armorInventory.size());
        }
        if (loadAccessoryIntegration && preferences.contains(ChargingDefinition.ACCESSORY)) {
            checkAccessory(result, player);
        }

        return result;
    }

    private static EnumSet<ChargingDefinition> resolveChargingPreferences(IGrid grid, EntityPlayer player) {
        var hubNode = grid.getHubNode();
        if (hubNode == null) {
            return EnumSet.allOf(ChargingDefinition.class);
        }

        if (hubNode.getPermissionLevel(player.getUniqueID()) == HubPermissionLevel.NONE) {
            return EnumSet.noneOf(ChargingDefinition.class);
        }

        var preferences = EnumSet.noneOf(ChargingDefinition.class);
        var hubPrefs = hubNode.getChargingPreference(player.getUniqueID());
        for (var type : ChargingDefinition.values()) {
            if (hubPrefs.getPreference(type)) {
                preferences.add(type);
            }
        }
        return preferences;
    }
    //~}
    //~}
    //~}
    //~}
    //~}

    private static void collectFromSlots(List<IEnergyHandler> result,
                                         EnumMap<ChargingDefinition, List<IEnergyHandler>> cache,
                                         ChargingDefinition definition,
                                         ItemStack[] items,
                                         int startIndex, int endIndex) {
        var cached = cache.get(definition);
        if (cached != null) {
            result.addAll(cached);
            return;
        }

        var handlers = new ObjectArrayList<IEnergyHandler>();
        for (int i = startIndex; i < endIndex; i++) {
            if (i >= items.length) break;
            var stack = items[i];
            var handler = IEnergyHandler.release(stack);
            if (handler != null) {
                if (canReceiveMore(handler)) {
                    handlers.add(handler);
                    result.add(handler);
                } else {
                    handler.recycle();
                }
            }
        }
        cache.put(definition, handlers);
    }

    private static void collectFromStackWithCache(List<IEnergyHandler> result,
                                                  EnumMap<ChargingDefinition, List<IEnergyHandler>> cache,
                                                  ChargingDefinition definition,
                                                  ItemStack stack) {
        var cached = cache.get(definition);
        if (cached != null) {
            result.addAll(cached);
            return;
        }

        var handler = IEnergyHandler.release(stack);
        if (handler != null) {
            if (canReceiveMore(handler)) {
                var handlers = ObjectLists.singleton(handler);
                cache.put(definition, handlers);
                result.add(handler);
            } else {
                handler.recycle();
            }
        }
    }

    private static boolean canReceiveMore(IEnergyHandler handler) {
        EnergyAmount amount = handler.canReceiveValue();
        try {
            return amount.isPositive();
        } finally {
            amount.recycle();
        }
    }

    private static void transferEnergyToTargets(List<Reference2ObjectMap<IGrid, List<IEnergyHandler>>> playerGridMaps,
                                                Reference2ObjectMap<IGrid, EnumMap<IEnergyHandler.EnergyType, Set<IEnergyHandler>>> machineMap) {
        var processedGrids = new ReferenceOpenHashSet<IGrid>();

        for (var gridMap : playerGridMaps) {
            for (var entry : gridMap.entrySet()) {
                var grid = entry.getKey();
                if (processedGrids.contains(grid)) {
                    continue;
                }

                var chargingTargets = entry.getValue();
                transferEnergyForGrid(grid, chargingTargets, machineMap, processedGrids);
            }
        }
    }

    private static void transferEnergyForGrid(IGrid grid,
                                              List<IEnergyHandler> chargingTargets,
                                              Reference2ObjectMap<IGrid, EnumMap<IEnergyHandler.EnergyType, Set<IEnergyHandler>>> machineMap,
                                              ReferenceSet<IGrid> processedGrids) {
        processedGrids.add(grid);

        var hubNode = grid.getHubNode();
        if (hubNode != null && hubNode.getChannelId() != null) {
            var channelGrids = HubChannelManager.INSTANCE.getChannelGrids(hubNode.getChannelId());
            if (channelGrids != null && channelGrids.size() > 1) {
                var mergedSend = new ObjectLinkedOpenHashSet<IEnergyHandler>();
                var mergedStorage = new ObjectLinkedOpenHashSet<IEnergyHandler>();

                for (var channelGrid : channelGrids) {
                    processedGrids.add(channelGrid);
                    var handlers = machineMap.get(channelGrid);
                    if (handlers != null) {
                        mergedSend.addAll(handlers.getOrDefault(IEnergyHandler.EnergyType.SEND, ObjectSets.emptySet()));
                        mergedStorage.addAll(handlers.getOrDefault(IEnergyHandler.EnergyType.STORAGE, ObjectSets.emptySet()));
                    }
                }

                transferEnergy(mergedSend, chargingTargets, EnergyMachineManager.Status.EXTRACT, grid);
                transferEnergy(mergedStorage, chargingTargets, EnergyMachineManager.Status.EXTRACT, grid);
                return;
            }
        }

        var handlers = machineMap.get(grid);
        if (handlers != null) {
            var sendMachines = handlers.getOrDefault(IEnergyHandler.EnergyType.SEND, ObjectSets.emptySet());
            var storageMachines = handlers.getOrDefault(IEnergyHandler.EnergyType.STORAGE, ObjectSets.emptySet());

            transferEnergy(sendMachines, chargingTargets, EnergyMachineManager.Status.EXTRACT, grid);
            transferEnergy(storageMachines, chargingTargets, EnergyMachineManager.Status.EXTRACT, grid);
        }
    }

    //~ if >=1.20 '.getUniqueID()' -> '.getUUID()' {
    public void onServerTick(MinecraftServer server, Reference2ObjectMap<IGrid, EnumMap<IEnergyHandler.EnergyType, Set<IEnergyHandler>>> machineMap) {
        var players = server.getPlayerList().getPlayers();
        var playerGridMaps = new ObjectArrayList<Reference2ObjectMap<IGrid, List<IEnergyHandler>>>(players.size());

        for (var player : players) {
            var gridMap = collectPlayerChargeTargets(player);
            playerGridMaps.add(gridMap);
        }

        // Plugin-based remote charging: extend range beyond spatial index
        for (var grid : machineMap.keySet()) {
            var hub = grid.getHubNode();
            if (hub == null || !hub.isActive()) continue;
            var scope = getChargingPluginScope(hub);
            if (scope == ChargingPluginScope.NONE) continue;

            int hubDim = getDimensionId(hub);

            for (int i = 0; i < players.size(); i++) {
                var player = players.get(i);
                var gridMap = playerGridMaps.get(i);

                // Skip if already covered by spatial index
                if (gridMap.containsKey(grid)) continue;

                // Wide area: same dimension only; Dimensional: all dimensions
                if (scope == ChargingPluginScope.WIDE_AREA) {
                    //~ if >=1.20 'player.dimension' -> 'player.level().dimension().location().hashCode()' {
                    if (player.dimension != hubDim) continue;
                    //~}
                }

                // Permission check
                if (hub.getPermissionLevel(player.getUniqueID()) == HubPermissionLevel.NONE) continue;

                var playerState = new PlayerChargeState(player);
                try {
                    var chargeableItems = collectChargeablesForGrid(grid, player, playerState);
                    if (!chargeableItems.isEmpty()) {
                        gridMap.put(grid, chargeableItems);
                    }
                } finally {
                    playerState.clear();
                }
            }
        }

        transferEnergyToTargets(playerGridMaps, machineMap);

        for (var gridMap : playerGridMaps) {
            for (var handlers : gridMap.values()) {
                for (var handler : handlers) {
                    handler.recycle();
                }
            }
        }
    }
    //~}

    //~ if >=1.20 '(EntityPlayer player' -> '(Player player' {
    //~ if >=1.20 'player.dimension)' -> 'player.level().dimension().location().hashCode())' {
    //~ if >=1.20 '.getPosition()' -> '.blockPosition()' {
    //~ if >=1.20 '.inventory.mainInventory' -> '.getInventory().items' {
    //~ if >=1.20 '.provider.getDimension()' -> '.dimension().location().hashCode()' {
    private Reference2ObjectMap<IGrid, List<IEnergyHandler>> collectPlayerChargeTargets(EntityPlayer player) {
        Reference2ObjectMap<IGrid, List<IEnergyHandler>> gridMap = new Reference2ObjectOpenHashMap<>();
        gridMap.defaultReturnValue(ObjectLists.emptyList());

        var map = scopeNode.get(player.dimension);
        if (map == null || map.isEmpty()) {
            return gridMap;
        }

        var pos = player.getPosition();
        var nodeSet = map.get(Functions.mergeChunkCoords(pos));
        if (nodeSet == null || nodeSet.isEmpty()) {
            return gridMap;
        }

        var reachableGrids = new ObjectArrayList<IGrid>();
        for (var node : nodeSet) {
            if (node.chargingScopeCheck(pos)) {
                reachableGrids.add(node.getGrid());
            }
        }

        if (reachableGrids.isEmpty()) {
            return gridMap;
        }

        var playerState = new PlayerChargeState(player);
        try {
            for (var grid : reachableGrids) {
                var chargeableItems = collectChargeablesForGrid(grid, player, playerState);
                if (!chargeableItems.isEmpty()) {
                    gridMap.put(grid, chargeableItems);
                }
            }
        } finally {
            playerState.clear();
        }

        return gridMap;
    }

    public void addNode(INode node) {
        if (!(node instanceof IChargingNode chargingNode)) {
            return;
        }

        int nodeX = chargingNode.getPos().getX();
        int nodeZ = chargingNode.getPos().getZ();
        int range = (int) chargingNode.getChargingScope();
        int minChunkX = (nodeX - range) >> 4;
        int maxChunkX = (nodeX + range) >> 4;
        int minChunkZ = (nodeZ - range) >> 4;
        int maxChunkZ = (nodeZ + range) >> 4;

        int dimId = getDimensionId(node);

        Long2ObjectMap<ReferenceSet<IChargingNode>> dimScopeMap = scopeNode.get(dimId);
        if (dimScopeMap == null) {
            dimScopeMap = new Long2ObjectOpenHashMap<>();
            dimScopeMap.defaultReturnValue(ReferenceSets.emptySet());
            scopeNode.put(dimId, dimScopeMap);
        }

        LongSet coveredChunks = new LongOpenHashSet();
        for (int cx = minChunkX; cx <= maxChunkX; ++cx) {
            for (int cz = minChunkZ; cz <= maxChunkZ; ++cz) {
                long chunkCoord = Functions.mergeChunkCoords(cx, cz);
                coveredChunks.add(chunkCoord);

                ReferenceSet<IChargingNode> chunkNodeSet = dimScopeMap.get(chunkCoord);
                if (chunkNodeSet == dimScopeMap.defaultReturnValue()) {
                    chunkNodeSet = new ReferenceOpenHashSet<>();
                    dimScopeMap.put(chunkCoord, chunkNodeSet);
                }
                chunkNodeSet.add(chargingNode);
            }
        }

        Object2ObjectMap<IChargingNode, LongSet> dimNodeScopeMap = nodeScope.get(dimId);
        if (dimNodeScopeMap == null) {
            dimNodeScopeMap = new Object2ObjectOpenHashMap<>();
            nodeScope.put(dimId, dimNodeScopeMap);
        }
        dimNodeScopeMap.put(chargingNode, LongSets.unmodifiable(coveredChunks));
    }

    public void removeNode(INode node) {
        if (!(node instanceof IChargingNode chargingNode)) {
            return;
        }

        int dimId = getDimensionId(node);

        Object2ObjectMap<IChargingNode, LongSet> dimNodeScopeMap = nodeScope.get(dimId);
        if (dimNodeScopeMap == null) {
            return;
        }

        LongSet coveredChunks = dimNodeScopeMap.remove(chargingNode);
        if (coveredChunks == null || coveredChunks.isEmpty()) {
            return;
        }

        Long2ObjectMap<ReferenceSet<IChargingNode>> dimScopeMap = scopeNode.get(dimId);
        if (dimScopeMap == null) {
            return;
        }

        for (long chunkCoord : coveredChunks) {
            ReferenceSet<IChargingNode> chunkNodeSet = dimScopeMap.get(chunkCoord);
            if (chunkNodeSet == dimScopeMap.defaultReturnValue()) {
                continue;
            }
            if (chunkNodeSet.size() == 1) {
                dimScopeMap.remove(chunkCoord);
            } else {
                chunkNodeSet.remove(chargingNode);
            }
        }
    }

    public void onServerStop() {
        scopeNode.clear();
        nodeScope.clear();
    }

    static ChargingPluginScope getChargingPluginScope(IHubNode hub) {
        var plugins = hub.getPlugins();
        boolean wide = false;
        for (int i = 0; i < IHubNode.PLUGIN_SLOTS; i++) {
            var stack = plugins.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof ItemDimensionalChargingPlugin) {
                return ChargingPluginScope.DIMENSIONAL;
            }
            if (stack.getItem() instanceof ItemWideAreaChargingPlugin) {
                wide = true;
            }
        }
        return wide ? ChargingPluginScope.WIDE_AREA : ChargingPluginScope.NONE;
    }

    private static int getDimensionId(INode node) {
        return node.getWorld().provider.getDimension();
    }

    private static final class PlayerChargeState {
        final EnumMap<ChargingDefinition, List<IEnergyHandler>> cache = new EnumMap<>(ChargingDefinition.class);
        final ItemStack[] inventory;

        PlayerChargeState(EntityPlayer player) {
            this.inventory = player.inventory.mainInventory.toArray(new ItemStack[0]);
        }

        void clear() {
            cache.clear();
        }
    }
    //~}
    //~}
    //~}
    //~}
    //~}
}