package com.circulation.circulation_networks.manager;

import com.circulation.circulation_networks.api.EnergyAmount;
import com.circulation.circulation_networks.api.IEnergyHandler;
import com.circulation.circulation_networks.api.IGrid;
import com.circulation.circulation_networks.api.hub.ChargingDefinition;
import com.circulation.circulation_networks.api.hub.HubPermissionLevel;
import com.circulation.circulation_networks.api.node.IChargingNode;
import com.circulation.circulation_networks.api.node.IHubNode;
import com.circulation.circulation_networks.api.node.INode;
import com.circulation.circulation_networks.network.hub.HubCapabilitys;
import com.circulation.circulation_networks.network.nodes.HubNode;
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
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectSet;
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

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

import static com.circulation.circulation_networks.manager.EnergyMachineManager.transferEnergy;

public final class ChargingManager {

    public static final ChargingManager INSTANCE = new ChargingManager();
    //? if <1.20 {
    private static final boolean loadAccessoryIntegration = Loader.isModLoaded("baubles");
    //?} else {
    /*private static final boolean loadAccessoryIntegration = ModList.get().isLoaded("curios");
     *///?}
    private final Int2ObjectMap<Long2ObjectMap<ReferenceSet<IChargingNode>>> scopeNode = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Object2ObjectMap<IChargingNode, LongSet>> nodeScope = new Int2ObjectOpenHashMap<>();
    private final Reference2ObjectMap<IGrid, ObjectSet<IEnergyHandler>> tickChargeTargetsByGrid = new Reference2ObjectOpenHashMap<>();
    private final ObjectList<IGrid> activeChargeTargetGrids = new ObjectArrayList<>();
    private final ReferenceSet<IGrid> processedTransferGrids = new ReferenceOpenHashSet<>();
    private final ChannelTransferScratch channelTransferScratch = new ChannelTransferScratch();

    //? if <1.20 {
    @Optional.Method(modid = "baubles")
    private static void checkAccessory(Collection<IEnergyHandler> invs, EntityPlayer player, @Nullable HubNode.HubMetadata hubMetadata) {
        //?} else {
        /*private static void checkAccessory(Collection<IEnergyHandler> invs, Player player) {
         *///?}
        //? if <1.20 {
        var h = BaublesApi.getBaublesHandler(player);
        for (var i = 0; i < h.getSlots(); i++) {
            var stack = h.getStackInSlot(i);
            var handler = IEnergyHandler.release(stack, null);
            if (handler == null) continue;
            if (canReceiveMore(handler, hubMetadata)) {
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
                var energyHandler = IEnergyHandler.release(stack, null);
                if (energyHandler == null) continue;
                if (canReceiveMore(energyHandler, null)) {
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
    private static void collectChargeablesForGrid(IGrid grid,
                                                  EntityPlayer player,
                                                  PlayerChargeState state,
                                                  Collection<IEnergyHandler> result) {
        var preferences = resolveChargingPreferences(grid, player);
        if (preferences.isEmpty()) {
            return;
        }

        HubNode.HubMetadata hubMetadata = getHubMetadata(grid);

        if (preferences.contains(ChargingDefinition.INVENTORY)) {
            collectFromSlots(result, state.cache, ChargingDefinition.INVENTORY, state.inventory, 9, state.inventory.length, hubMetadata);
        }
        if (preferences.contains(ChargingDefinition.OFF_HAND)) {
            collectFromStackWithCache(result, state.cache, ChargingDefinition.OFF_HAND, player.getHeldItemOffhand(), hubMetadata);
        }
        if (preferences.contains(ChargingDefinition.HOTBAR)) {
            collectFromSlots(result, state.cache, ChargingDefinition.HOTBAR, state.inventory, 0, 9, hubMetadata);
        } else {
            if (preferences.contains(ChargingDefinition.MAIN_HAND)) {
                collectFromStackWithCache(result, state.cache, ChargingDefinition.MAIN_HAND, player.getHeldItemMainhand(), hubMetadata);
            }
        }
        if (preferences.contains(ChargingDefinition.ARMOR)) {
            collectFromSlots(result, state.cache, ChargingDefinition.ARMOR, state.armor, 0, state.armor.length, hubMetadata);
        }
        if (loadAccessoryIntegration && preferences.contains(ChargingDefinition.ACCESSORY)) {
            //? if <1.20 {
            checkAccessory(result, player, hubMetadata);
            //?} else {
            /*checkAccessory(result, player);
            *///?}
        }
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

    private static void collectFromSlots(Collection<IEnergyHandler> result,
                                         EnumMap<ChargingDefinition, List<IEnergyHandler>> cache,
                                         ChargingDefinition definition,
                                         ItemStack[] items,
                                         int startIndex, int endIndex,
                                         @Nullable HubNode.HubMetadata hubMetadata) {
        var cached = cache.get(definition);
        if (cached != null) {
            result.addAll(cached);
            return;
        }

        var handlers = new ObjectArrayList<IEnergyHandler>();
        for (int i = startIndex; i < endIndex; i++) {
            if (i >= items.length) break;
            var stack = items[i];
            var handler = IEnergyHandler.release(stack, null);
            if (handler != null) {
                if (canReceiveMore(handler, hubMetadata)) {
                    handlers.add(handler);
                    result.add(handler);
                } else {
                    handler.recycle();
                }
            }
        }
        cache.put(definition, handlers);
    }
    //~}
    //~}
    //~}
    //~}
    //~}

    private static void collectFromStackWithCache(Collection<IEnergyHandler> result,
                                                  EnumMap<ChargingDefinition, List<IEnergyHandler>> cache,
                                                  ChargingDefinition definition,
                                                  ItemStack stack,
                                                  @Nullable HubNode.HubMetadata hubMetadata) {
        var cached = cache.get(definition);
        if (cached != null) {
            result.addAll(cached);
            return;
        }

        var handler = IEnergyHandler.release(stack, null);
        if (handler != null) {
            if (canReceiveMore(handler, hubMetadata)) {
                var handlers = ObjectLists.singleton(handler);
                cache.put(definition, handlers);
                result.add(handler);
            } else {
                handler.recycle();
            }
        }
    }

    private static boolean canReceiveMore(IEnergyHandler handler, @Nullable HubNode.HubMetadata hubMetadata) {
        EnergyAmount amount = handler.canReceiveValue(hubMetadata);
        try {
            return amount.isPositive();
        } finally {
            amount.recycle();
        }
    }

    private static void transferEnergyToTargets(Reference2ObjectMap<IGrid, ObjectSet<IEnergyHandler>> chargeTargetsByGrid,
                                                Reference2ObjectMap<IGrid, EnergyMachineManager.GridTickData> machineMap) {
        for (var entry : chargeTargetsByGrid.entrySet()) {
            var grid = entry.getKey();
            if (INSTANCE.processedTransferGrids.contains(grid)) {
                continue;
            }
            transferEnergyForGrid(grid, chargeTargetsByGrid, machineMap, INSTANCE.processedTransferGrids);
        }
    }

    private static void transferEnergyForGrid(IGrid grid,
                                              Reference2ObjectMap<IGrid, ObjectSet<IEnergyHandler>> chargeTargetsByGrid,
                                              Reference2ObjectMap<IGrid, EnergyMachineManager.GridTickData> machineMap,
                                              ReferenceSet<IGrid> processedGrids) {
        processedGrids.add(grid);
        var chargingTargets = chargeTargetsByGrid.getOrDefault(grid, ObjectSets.emptySet());

        var hubNode = grid.getHubNode();
        if (hubNode != null && !hubNode.getChannelId().equals(HubNode.EMPTY)) {
            var channelGrids = HubChannelManager.INSTANCE.getChannelGrids(hubNode.getChannelId());
            if (channelGrids != null && channelGrids.size() > 1) {
                var merged = INSTANCE.channelTransferScratch.prepare();

                for (var channelGrid : channelGrids) {
                    processedGrids.add(channelGrid);
                    merged.timedGrids.add(channelGrid);
                    var handlers = machineMap.get(channelGrid);
                    if (handlers != null && handlers.activeThisTick) {
                        merged.send.addAll(handlers.send);
                        merged.storage.addAll(handlers.storage);
                    }
                    merged.targets.addAll(chargeTargetsByGrid.getOrDefault(channelGrid, ObjectSets.emptySet()));
                }

                if (merged.targets.isEmpty()) {
                    return;
                }

                long startNanos = System.nanoTime();
                transferEnergy(merged.send, merged.targets, EnergyMachineManager.Status.EXTRACT, grid, false);
                transferEnergy(merged.storage, merged.targets, EnergyMachineManager.Status.EXTRACT, grid, false);
                EnergyMachineManager.recordDistributedGridTickTimeNanos(merged.timedGrids, System.nanoTime() - startNanos);
                return;
            }
        }

        if (chargingTargets.isEmpty()) {
            return;
        }

        var handlers = machineMap.get(grid);
        if (handlers != null && handlers.activeThisTick) {
            long startNanos = System.nanoTime();
            transferEnergy(handlers.send, chargingTargets, EnergyMachineManager.Status.EXTRACT, grid, false);
            transferEnergy(handlers.storage, chargingTargets, EnergyMachineManager.Status.EXTRACT, grid, false);
            EnergyMachineManager.recordGridTickTimeNanos(grid, System.nanoTime() - startNanos);
        }
    }

    static ChargingPluginScope getChargingPluginScope(IHubNode hub) {
        Boolean dimensional = hub.getPluginCapabilityData(HubCapabilitys.CHARGE_CAPABILITY);
        if (dimensional == null) {
            return ChargingPluginScope.NONE;
        }
        return dimensional ? ChargingPluginScope.DIMENSIONAL : ChargingPluginScope.WIDE_AREA;
    }

    //? if <1.20 {
    private static int getDimensionId(INode node) {
        return node.getDimensionId();
    }
    //?} else {
    /*private static int getDimensionId(INode node) {
        return node.getWorld().dimension().location().hashCode();
    }
    *///?}

    @Nullable
    private static HubNode.HubMetadata getHubMetadata(@Nullable IGrid grid) {
        if (grid == null) {
            return null;
        }
        IHubNode hubNode = grid.getHubNode();
        return hubNode != null ? hubNode.getHubData() : null;
    }

    //~ if >=1.20 '.getUniqueID()' -> '.getUUID()' {
    void onServerTick(MinecraftServer server, Reference2ObjectMap<IGrid, EnergyMachineManager.GridTickData> machineMap) {
        var players = server.getPlayerList().getPlayers();
        prepareChargeTargetScratch();
        processedTransferGrids.clear();
        var coveredGridsByPlayer = new ObjectArrayList<ReferenceSet<IGrid>>(players.size());
        var playerStates = new ObjectArrayList<PlayerChargeState>(players.size());

        for (var player : players) {
            var playerState = new PlayerChargeState(player);
            playerStates.add(playerState);
            coveredGridsByPlayer.add(collectPlayerChargeTargets(player, playerState));
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
                if (coveredGridsByPlayer.get(i).contains(grid)) continue;

                // Wide area: same dimension only; Dimensional: all dimensions
                if (scope == ChargingPluginScope.WIDE_AREA) {
                    //~ if >=1.20 'player.dimension' -> 'player.level().dimension().location().hashCode()' {
                    if (player.dimension != hubDim) continue;
                    //~}
                }

                // Permission check
                if (hub.getPermissionLevel(player.getUniqueID()) == HubPermissionLevel.NONE) continue;

                var playerState = playerStates.get(i);
                long startNanos = System.nanoTime();
                playerState.scratch.clear();
                collectChargeablesForGrid(grid, player, playerState, playerState.scratch);
                EnergyMachineManager.recordGridTickTimeNanos(grid, System.nanoTime() - startNanos);
                if (!playerState.scratch.isEmpty()) {
                    getChargeTargets(grid).addAll(playerState.scratch);
                }
            }
        }

        transferEnergyToTargets(tickChargeTargetsByGrid, machineMap);

        for (var grid : activeChargeTargetGrids) {
            var handlers = tickChargeTargetsByGrid.get(grid);
            for (var handler : handlers) {
                handler.recycle();
            }
            handlers.clear();
        }
        activeChargeTargetGrids.clear();
        for (var playerState : playerStates) {
            playerState.clear();
        }
    }
    //~}

    //~ if >=1.20 '(EntityPlayer player' -> '(Player player' {
    //~ if >=1.20 'player.dimension)' -> 'player.level().dimension().location().hashCode())' {
    //~ if >=1.20 '.getPosition()' -> '.blockPosition()' {
    //~ if >=1.20 '.inventory.mainInventory' -> '.getInventory().items' {
    //~ if >=1.20 '.provider.getDimension()' -> '.dimension().location().hashCode()' {
    private ReferenceSet<IGrid> collectPlayerChargeTargets(EntityPlayer player,
                                                           PlayerChargeState playerState) {
        ReferenceSet<IGrid> coveredGrids = new ReferenceOpenHashSet<>();
        var map = scopeNode.get(player.dimension);
        if (map == null || map.isEmpty()) {
            return coveredGrids;
        }

        var pos = player.getPosition();
        var nodeSet = map.get(Functions.mergeChunkCoords(pos));
        if (nodeSet == null || nodeSet.isEmpty()) {
            return coveredGrids;
        }

        ReferenceSet<IGrid> reachableGrids = new ReferenceOpenHashSet<>();
        for (var node : nodeSet) {
            if (!node.chargingScopeCheck(pos)) {
                continue;
            }
            var grid = node.getGrid();
            if (grid != null) {
                reachableGrids.add(grid);
            }
        }

        if (reachableGrids.isEmpty()) {
            return coveredGrids;
        }

        for (var grid : reachableGrids) {
            long startNanos = System.nanoTime();
            playerState.scratch.clear();
            collectChargeablesForGrid(grid, player, playerState, playerState.scratch);
            EnergyMachineManager.recordGridTickTimeNanos(grid, System.nanoTime() - startNanos);
            if (!playerState.scratch.isEmpty()) {
                getChargeTargets(grid).addAll(playerState.scratch);
                coveredGrids.add(grid);
            }
        }
        return coveredGrids;
    }

    private void prepareChargeTargetScratch() {
        for (var grid : activeChargeTargetGrids) {
            tickChargeTargetsByGrid.get(grid).clear();
        }
        activeChargeTargetGrids.clear();
    }

    private ObjectSet<IEnergyHandler> getChargeTargets(IGrid grid) {
        ObjectSet<IEnergyHandler> targets = tickChargeTargetsByGrid.computeIfAbsent(grid, ignored -> new ObjectLinkedOpenHashSet<>());
        if (targets.isEmpty()) {
            activeChargeTargetGrids.add(grid);
        }
        return targets;
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

    void initGrid(Collection<NetworkManager.GridEntry> entries) {
        for (var entry : entries) {
            if (entry.grid().getNodes().isEmpty()) continue;
            for (INode node : entry.grid().getNodes()) {
                addNode(node);
            }
        }
    }

    public void onServerStop() {
        scopeNode.clear();
        nodeScope.clear();
        tickChargeTargetsByGrid.clear();
        activeChargeTargetGrids.clear();
        processedTransferGrids.clear();
    }

    enum ChargingPluginScope {NONE, WIDE_AREA, DIMENSIONAL}

    private static final class ChannelTransferScratch {
        final ObjectSet<IEnergyHandler> send = new ObjectLinkedOpenHashSet<>();
        final ObjectSet<IEnergyHandler> storage = new ObjectLinkedOpenHashSet<>();
        final ObjectSet<IEnergyHandler> targets = new ObjectLinkedOpenHashSet<>();
        final ReferenceSet<IGrid> timedGrids = new ReferenceOpenHashSet<>();

        ChannelTransferScratch prepare() {
            send.clear();
            storage.clear();
            targets.clear();
            timedGrids.clear();
            return this;
        }
    }

    private static final class PlayerChargeState {
        final EnumMap<ChargingDefinition, List<IEnergyHandler>> cache = new EnumMap<>(ChargingDefinition.class);
        final ItemStack[] inventory;
        final ItemStack[] armor;
        final ObjectArrayList<IEnergyHandler> scratch = new ObjectArrayList<>();

        PlayerChargeState(EntityPlayer player) {
            this.inventory = player.inventory.mainInventory.toArray(new ItemStack[0]);
            this.armor = snapshotArmor(player);
        }

        void clear() {
            cache.clear();
            scratch.clear();
        }
    }

    //~ if >=1.20 '(EntityPlayer player' -> '(Player player' {
    //~ if >=1.20 '.inventory.armorInventory' -> '.getInventory().armor' {
    private static ItemStack[] snapshotArmor(EntityPlayer player) {
        return player.inventory.armorInventory.toArray(new ItemStack[0]);
    }
    //~}
    //~}
    //~}
    //~}
    //~}
    //~}
    //~}
}
