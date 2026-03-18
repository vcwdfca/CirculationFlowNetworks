package com.circulation.circulation_networks.manager;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import com.circulation.circulation_networks.api.IGrid;
import com.circulation.circulation_networks.api.INodeBlockEntity;
import com.circulation.circulation_networks.api.node.IHubNode;
import com.circulation.circulation_networks.api.node.INode;
import com.circulation.circulation_networks.events.BlockEntityLifeCycleEvent;
import com.circulation.circulation_networks.network.Grid;
import com.circulation.circulation_networks.utils.Functions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMaps;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.objects.ReferenceSets;
import com.circulation.circulation_networks.packets.NodeNetworkRendering;
//? if <1.20 {
import com.github.bsideup.jabel.Desugar;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
//?} else {
/*import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
*///?}

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Queue;

@SuppressWarnings("unused")
public final class NetworkManager {

    public static final NetworkManager INSTANCE = new NetworkManager();
    private static File saveFile;
    private final ReferenceSet<INode> activeNodes = new ReferenceOpenHashSet<>();
    private final Int2ObjectMap<IGrid> grids = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Long2ReferenceMap<INode>> posNodes = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Long2ObjectMap<ReferenceSet<INode>>> scopeNode = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Object2ObjectMap<INode, LongSet>> nodeScope = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Long2ObjectMap<ReferenceSet<INode>>> nodeLocation = new Int2ObjectOpenHashMap<>();
    private final ObjectSet<IGrid> markGird = new ObjectOpenHashSet<>();
    private final Queue<IGrid> emptyGird = new ArrayDeque<>();
    private boolean init;
    private int nextGridId = 0;

    {
        posNodes.defaultReturnValue(Long2ReferenceMaps.emptyMap());
        scopeNode.defaultReturnValue(Long2ObjectMaps.emptyMap());
        nodeScope.defaultReturnValue(Object2ObjectMaps.emptyMap());
        nodeLocation.defaultReturnValue(Long2ObjectMaps.emptyMap());
    }

    public static File getSaveFile() {
        if (saveFile == null) {
            var path = getGridSavePath();
            try {
                Files.createDirectories(path);
            } catch (IOException ignored) {
            }
            saveFile = path.toFile();
        }
        return saveFile;
    }

    public ReferenceSet<INode> getActiveNodes() {
        return activeNodes;
    }

    public boolean isInit() {
        return init;
    }

    private void registerNodeIndices(int dimId, INode node) {
        BlockPos pos = node.getPos();

        var pMap = posNodes.get(dimId);
        if (pMap == posNodes.defaultReturnValue()) {
            posNodes.put(dimId, pMap = new Long2ReferenceOpenHashMap<>());
        }
        //? if <1.20 {
        pMap.put(pos.toLong(), node);
        //?} else {
        /*pMap.put(pos.asLong(), node);
        *///?}

        long ownChunkCoord = Functions.mergeChunkCoords(pos);
        var locMap = nodeLocation.get(dimId);
        if (locMap == nodeLocation.defaultReturnValue()) {
            locMap = new Long2ObjectOpenHashMap<>();
            locMap.defaultReturnValue(ReferenceSets.emptySet());
            nodeLocation.put(dimId, locMap);
        }
        var locSet = locMap.get(ownChunkCoord);
        if (locSet == locMap.defaultReturnValue()) {
            locMap.put(ownChunkCoord, locSet = new ReferenceOpenHashSet<>());
        }
        locSet.add(node);

        int range = (int) node.getLinkScope();
        int minChunkX = (pos.getX() - range) >> 4, maxChunkX = (pos.getX() + range) >> 4;
        int minChunkZ = (pos.getZ() - range) >> 4, maxChunkZ = (pos.getZ() + range) >> 4;
        LongSet chunksCovered = new LongOpenHashSet();

        var scopeMap = scopeNode.get(dimId);
        if (scopeMap == scopeNode.defaultReturnValue()) {
            scopeMap = new Long2ObjectOpenHashMap<>();
            scopeMap.defaultReturnValue(ReferenceSets.emptySet());
            scopeNode.put(dimId, scopeMap);
        }
        for (int cx = minChunkX; cx <= maxChunkX; ++cx) {
            for (int cz = minChunkZ; cz <= maxChunkZ; ++cz) {
                long chunkCoord = Functions.mergeChunkCoords(cx, cz);
                chunksCovered.add(chunkCoord);
                var sSet = scopeMap.get(chunkCoord);
                if (sSet == scopeMap.defaultReturnValue()) {
                    scopeMap.put(chunkCoord, sSet = new ReferenceOpenHashSet<>());
                }
                sSet.add(node);
            }
        }
        var nodeScopeMap = nodeScope.get(dimId);
        if (nodeScopeMap == nodeScope.defaultReturnValue()) {
            nodeScopeMap = new Object2ObjectOpenHashMap<>();
            nodeScope.put(dimId, nodeScopeMap);
        }
        nodeScopeMap.put(node, LongSets.unmodifiable(chunksCovered));
    }

    private void unregisterNodeIndices(int dimId, INode node) {
        //? if <1.20 {
        posNodes.get(dimId).remove(node.getPos().toLong());
        //?} else {
        /*posNodes.get(dimId).remove(node.getPos().asLong());
        *///?}

        long ownChunkCoord = Functions.mergeChunkCoords(node.getPos());
        nodeLocation.get(dimId).get(ownChunkCoord).remove(node);

        var sm = scopeNode.get(dimId);
        LongSet coveredChunks = nodeScope.get(dimId).remove(node);
        if (coveredChunks != null && sm != scopeNode.defaultReturnValue()) {
            for (long chunk : coveredChunks) {
                var set = sm.get(chunk);
                if (set == sm.defaultReturnValue()) continue;
                if (set.size() == 1) sm.remove(chunk);
                else set.remove(node);
            }
        }
    }

    //? if <1.20 {
    public @Nullable INode getNodeFromPos(World world, BlockPos pos) {
    //?} else {
    /*public @Nullable INode getNodeFromPos(Level world, BlockPos pos) {
    *///?}
        //? if <1.20 {
        return posNodes.get(getDimensionId(world)).get(pos.toLong());
        //?} else {
        /*return posNodes.get(getDimensionId(world)).get(pos.asLong());
        *///?}
    }

    public void onBlockEntityValidate(BlockEntityLifeCycleEvent.Validate event) {
        if (isClientWorld(event.getWorld())) return;
        var blockEntity = event.getBlockEntity();
        if (blockEntity instanceof INodeBlockEntity nbe) {
            addNode(nbe.getNode(), blockEntity);
        }
    }

    public Collection<IGrid> getAllGrids() {
        return grids.values();
    }

    public void onBlockEntityInvalidate(BlockEntityLifeCycleEvent.Invalidate event) {
        if (isClientWorld(event.getWorld())) return;
        removeNode(getDimensionId(event.getWorld()), event.getPos());
    }

    //? if <1.20 {
    public @Nonnull ReferenceSet<INode> getNodesCoveringPosition(World world, BlockPos pos) {
    //?} else {
    /*public @Nonnull ReferenceSet<INode> getNodesCoveringPosition(Level world, BlockPos pos) {
    *///?}
        return scopeNode.get(getDimensionId(world)).get(Functions.mergeChunkCoords(pos));
    }

    //? if <1.20 {
    public @Nonnull ReferenceSet<INode> getNodesCoveringPosition(World world, int chunkX, int chunkY) {
    //?} else {
    /*public @Nonnull ReferenceSet<INode> getNodesCoveringPosition(Level world, int chunkX, int chunkY) {
    *///?}
        return scopeNode.get(getDimensionId(world)).get(Functions.mergeChunkCoords(chunkX, chunkY));
    }

    //? if <1.20 {
    public @Nonnull ReferenceSet<INode> getNodesInChunk(World world, int chunkX, int chunkZ) {
    //?} else {
    /*public @Nonnull ReferenceSet<INode> getNodesInChunk(Level world, int chunkX, int chunkZ) {
    *///?}
        var map = nodeLocation.get(getDimensionId(world));
        return map.get(Functions.mergeChunkCoords(chunkX, chunkZ));
    }

    public void removeNode(int dim, BlockPos pos) {
        var pMap = posNodes.get(dim);
        if (pMap != null && pMap != posNodes.defaultReturnValue()) {
            //? if <1.20 {
            removeNode(pMap.get(pos.toLong()));
            //?} else {
            /*removeNode(pMap.get(pos.asLong()));
            *///?}
        }
    }

    public void removeNode(INode removedNode) {
        if (removedNode == null || isClientWorld(removedNode.getWorld()) || !activeNodes.remove(removedNode)) return;

        NodeEventHooks.postRemoveNodePre(removedNode);
        int dimId = getDimensionId(removedNode);

        //? if <1.20 {
        var players = NodeNetworkRendering.getPlayers(removedNode.getGrid());
        if (players != null && !players.isEmpty()) {
            for (var player : players) {
                CirculationFlowNetworks.NET_CHANNEL.sendTo(
                    new NodeNetworkRendering(player, removedNode, NodeNetworkRendering.NODE_REMOVE), player);
            }
        }
        //?}

        unregisterNodeIndices(dimId, removedNode);

        IGrid oldGrid = removedNode.getGrid();

        if (removedNode instanceof IHubNode hub) {
            if (oldGrid != null) {
                oldGrid.setHubNode(null);
            }
            HubChannelManager.INSTANCE.unregister(hub);
        }

        if (oldGrid != null) {
            for (INode node : oldGrid.getNodes()) {
                node.removeNeighbor(removedNode);
            }
        }
        removedNode.setActive(false);

        if (oldGrid == null) {
            EnergyMachineManager.INSTANCE.removeNode(removedNode);
            ChargingManager.INSTANCE.removeNode(removedNode);
            return;
        }
        oldGrid.getNodes().remove(removedNode);
        markGird.add(oldGrid);

        if (oldGrid.getNodes().isEmpty()) {
            destroyGrid(oldGrid);
        } else {
            ReferenceSet<INode> remaining = new ReferenceOpenHashSet<>(oldGrid.getNodes());
            List<ReferenceSet<INode>> components = new ObjectArrayList<>();

            while (!remaining.isEmpty()) {
                ReferenceSet<INode> component = new ReferenceOpenHashSet<>();
                Queue<INode> queue = new ArrayDeque<>();
                INode seed = remaining.iterator().next();
                queue.add(seed);
                component.add(seed);
                remaining.remove(seed);

                while (!queue.isEmpty()) {
                    INode curr = queue.poll();
                    for (INode nb : curr.getNeighbors()) {
                        if (remaining.remove(nb)) {
                            component.add(nb);
                            queue.add(nb);
                        }
                    }
                    var iter = remaining.iterator();
                    while (iter.hasNext()) {
                        INode nb = iter.next();
                        if (nb.getNeighbors().contains(curr)) {
                            iter.remove();
                            component.add(nb);
                            queue.add(nb);
                        }
                    }
                }
                components.add(component);
            }

            if (components.size() > 1) {
                components.sort((a, b) -> b.size() - a.size());

                oldGrid.getNodes().clear();
                oldGrid.setHubNode(null);
                for (INode n : components.get(0)) {
                    oldGrid.getNodes().add(n);
                    n.setGrid(oldGrid);
                    if (n instanceof IHubNode h) {
                        oldGrid.setHubNode(h);
                    }
                }
                markGird.add(oldGrid);

                //? if <1.20 {
                var watchingPlayers = NodeNetworkRendering.getPlayers(oldGrid);
                //?}
                for (int i = 1; i < components.size(); i++) {
                    IGrid splitGrid = allocGrid();
                    for (INode n : components.get(i)) {
                        assignNodeToGrid(n, splitGrid);
                        if (n instanceof IHubNode h) {
                            splitGrid.setHubNode(h);
                        }
                    }
                    //? if <1.20 {
                    if (watchingPlayers != null) {
                        for (var player : watchingPlayers) {
                            CirculationFlowNetworks.NET_CHANNEL.sendTo(
                                new NodeNetworkRendering(player, splitGrid), player);
                        }
                    }
                    //?}
                }
            }
        }

        EnergyMachineManager.INSTANCE.removeNode(removedNode);
        ChargingManager.INSTANCE.removeNode(removedNode);

        NodeEventHooks.postRemoveNodePost(removedNode);
    }

    //? if <1.20 {
    public void addNode(INode newNode, TileEntity blockEntity) {
    //?} else {
    /*public void addNode(INode newNode, BlockEntity blockEntity) {
    *///?}
        if (newNode == null || isClientWorld(newNode.getWorld()) || !newNode.isActive() || activeNodes.contains(newNode))
            return;

        if (NodeEventHooks.postAddNodePre(newNode, blockEntity)) return;

        int dimId = getDimensionId(newNode);
        activeNodes.add(newNode);
        registerNodeIndices(dimId, newNode);

        ReferenceSet<INode> candidates = new ReferenceOpenHashSet<>();
        var scopeMap = scopeNode.get(dimId);
        for (long chunkCoord : nodeScope.get(dimId).get(newNode)) {
            candidates.addAll(scopeMap.get(chunkCoord));
        }
        candidates.remove(newNode);

        ReferenceSet<IGrid> linkedGrids = new ReferenceOpenHashSet<>();
        for (INode existing : candidates) {
            if (!existing.isActive()) continue;
            if (newNode.linkScopeCheck(existing) == INode.LinkType.DISCONNECT) continue;
            if (existing.getGrid() != null) {
                linkedGrids.add(existing.getGrid());
            }
        }

        boolean hubConflict = false;
        {
            int hubCount = (newNode instanceof IHubNode) ? 1 : 0;
            for (IGrid g : linkedGrids) {
                var h = g.getHubNode();
                if (h != null && h.isActive()) {
                    hubCount++;
                    if (hubCount > 1) {
                        hubConflict = true;
                        break;
                    }
                }
            }
        }

        if (hubConflict) {
            activeNodes.remove(newNode);
            unregisterNodeIndices(dimId, newNode);
            newNode.setActive(false);
            var world = newNode.getWorld();
            var pos = newNode.getPos();
            for (var player : getPlayers(world)) {
                //? if <1.20 {
                if (player.getDistanceSq(pos) < 36) {
                //?} else {
                /*if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 36) {
                *///?}
                    //? if <1.20 {
                    player.sendMessage(new TextComponentTranslation("message.circulation_networks.hub_conflict"));
                    //?} else {
                    /*player.displayClientMessage(Component.translatable("message.circulation_networks.hub_conflict"), false);
                    *///?}
                }
            }
            destroyBlock(world, pos);
            return;
        }

        for (INode existing : candidates) {
            if (!existing.isActive()) continue;
            var linkType = newNode.linkScopeCheck(existing);
            if (linkType == INode.LinkType.DISCONNECT) continue;
            switch (linkType) {
                case DOUBLY -> {
                    newNode.addNeighbor(existing);
                    existing.addNeighbor(newNode);
                }
                case A_TO_B -> newNode.addNeighbor(existing);
                case B_TO_A -> existing.addNeighbor(newNode);
            }
            IGrid existingGrid = existing.getGrid();
            IGrid currentGrid = newNode.getGrid();
            if (currentGrid == null) {
                assignNodeToGrid(newNode, existingGrid);
            } else if (existingGrid != null && existingGrid != currentGrid) {
                IGrid dst = currentGrid.getNodes().size() > existingGrid.getNodes().size() ? currentGrid : existingGrid;
                IGrid src = dst == currentGrid ? existingGrid : currentGrid;
                if (src.getHubNode() != null) {
                    dst.setHubNode(src.getHubNode());
                }
                for (INode n : src.getNodes()) {
                    dst.getNodes().add(n);
                    n.setGrid(dst);
                }
                src.getNodes().clear();
                src.setHubNode(null);
                destroyGrid(src);
                markGird.add(dst);
            }
        }

        if (newNode.getGrid() == null) {
            assignNodeToGrid(newNode, allocGrid());
        }

        if (newNode instanceof IHubNode hub) {
            newNode.getGrid().setHubNode(hub);
        }

        //? if <1.20 {
        var players = NodeNetworkRendering.getPlayers(newNode.getGrid());
        if (players != null && !players.isEmpty()) {
            for (var player : players) {
                CirculationFlowNetworks.NET_CHANNEL.sendTo(
                    new NodeNetworkRendering(player, newNode, NodeNetworkRendering.NODE_ADD), player);
            }
        }
        //?}
        EnergyMachineManager.INSTANCE.addNode(newNode);
        ChargingManager.INSTANCE.addNode(newNode);

        NodeEventHooks.postAddNodePost(newNode, blockEntity);
    }

    private void assignNodeToGrid(INode node, IGrid grid) {
        markGird.add(grid);
        grid.getNodes().add(node);
        node.setGrid(grid);
    }

    private IGrid allocGrid() {
        IGrid grid;
        if (!emptyGird.isEmpty()) {
            grid = emptyGird.poll();
            grid.getNodes().clear();
        } else {
            grid = new Grid(nextGridId++);
        }
        grids.put(grid.getId(), grid);
        EnergyMachineManager.INSTANCE.getInteraction().put(grid, new EnergyMachineManager.Interaction());
        markGird.add(grid);
        return grid;
    }

    private void destroyGrid(IGrid grid) {
        grids.remove(grid.getId());
        EnergyMachineManager.INSTANCE.getInteraction().remove(grid);
        markGird.add(grid);
        emptyGird.add(grid);
    }

    public void onServerStop() {
        scopeNode.clear();
        nodeScope.clear();
        nodeLocation.clear();
        activeNodes.clear();
        markGird.clear();
        emptyGird.clear();
        grids.clear();
        posNodes.clear();
        nextGridId = 0;
        saveFile = null;
        init = false;
    }

    public void saveGrid() {
        if (markGird.isEmpty()) return;
        File saveDir = NetworkManager.getSaveFile();
        for (IGrid grid : markGird) {
            try {
                writeCompressedNbt(grid.serialize(), new File(saveDir, grid.getId() + ".dat"));
            } catch (IOException ignored) {
            }
        }
        markGird.clear();
    }

    public void initGrid() {
        var f = getSaveFile();
        if (!f.exists() || !f.isDirectory()) return;

        File[] files = f.listFiles(file -> file.isFile() && file.getName().endsWith(".dat"));
        if (files == null || files.length == 0) return;

        var entries = new ObjectArrayList<GridEntry>();
        for (File file : files) {
            try {
                var nbt = readCompressedNbt(file);
                if (nbt == null) continue;
                //? if <1.20 {
                int dimId = nbt.getInteger("dim");
                //?} else {
                /*int dimId = nbt.getInt("dim");
                *///?}
                if (!isRegisteredDimension(dimId)) continue;
                var grid = Grid.deserialize(nbt);
                if (grid == null) continue;
                entries.add(new GridEntry(dimId, grid));
            } catch (IOException ignored) {
            }
        }

        int maxId = 0;
        for (var entry : entries) {
            var grid = entry.grid();
            int gridId = grid.getId();
            if (gridId > maxId) maxId = gridId;
            grids.put(gridId, grid);
            EnergyMachineManager.INSTANCE.getInteraction().put(grid, new EnergyMachineManager.Interaction());

            if (grid.getNodes().isEmpty()) {
                emptyGird.add(grid);
            } else {
                for (INode node : grid.getNodes()) {
                    activeNodes.add(node);
                    registerNodeIndices(entry.dimId(), node);
                }
            }
        }
        nextGridId = maxId + 1;
        EnergyMachineManager.INSTANCE.initGrid(entries);
        init = true;
    }

    //? if <1.20 {
    private static java.nio.file.Path getGridSavePath() {
        return DimensionManager.getWorld(0)
                               .getSaveHandler()
                               .getWorldDirectory()
                               .toPath()
                               .resolve("circulation_grids");
    }
    //?} else if <1.21 {
    /*private static java.nio.file.Path getGridSavePath() {
        return net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer()
                .getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                .resolve("circulation_grids");
    }
    *///?} else {
    /*private static java.nio.file.Path getGridSavePath() {
        return net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer()
                .getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                .resolve("circulation_grids");
    }
    *///?}

    //? if <1.20 {
    static NBTTagCompound readCompressedNbt(File file) throws IOException {
        return CompressedStreamTools.read(file);
    }

    static void writeCompressedNbt(NBTTagCompound nbt, File file) throws IOException {
        CompressedStreamTools.safeWrite(nbt, file);
    }
    //?} else if <1.21 {
    /*static CompoundTag readCompressedNbt(File file) throws IOException {
        return NbtIo.readCompressed(file);
    }

    static void writeCompressedNbt(CompoundTag nbt, File file) throws IOException {
        NbtIo.writeCompressed(nbt, file);
    }
    *///?} else {
    /*static CompoundTag readCompressedNbt(File file) throws IOException {
        return NbtIo.readCompressed(file.toPath(), net.minecraft.nbt.NbtAccounter.unlimitedHeap());
    }

    static void writeCompressedNbt(CompoundTag nbt, File file) throws IOException {
        NbtIo.writeCompressed(nbt, file.toPath());
    }
    *///?}

    //? if <1.20 {
    private static int getDimensionId(World world) {
        return world.provider.getDimension();
    }

    private static int getDimensionId(INode node) {
        return getDimensionId(node.getWorld());
    }

    private static boolean isClientWorld(World world) {
        return world.isRemote;
    }

    private static List<net.minecraft.entity.player.EntityPlayer> getPlayers(World world) {
        return world.playerEntities;
    }

    private static void destroyBlock(World world, BlockPos pos) {
        world.destroyBlock(pos, true);
    }

    private static boolean isRegisteredDimension(int dimId) {
        return DimensionManager.isDimensionRegistered(dimId);
    }
    //?} else {
    /*private static int getDimensionId(Level world) {
        return world.dimension().location().hashCode();
    }

    private static int getDimensionId(INode node) {
        return getDimensionId(node.getWorld());
    }

    private static boolean isClientWorld(Level world) {
        return world.isClientSide;
    }

    @SuppressWarnings("unchecked")
    private static List<Player> getPlayers(Level world) {
        return (List<Player>) (List<?>) world.players();
    }

    private static void destroyBlock(Level world, BlockPos pos) {
        world.destroyBlock(pos, true, null);
    }

    private static boolean isRegisteredDimension(int dimId) {
        return true;
    }
    *///?}

    //? if <1.20 {
    @Desugar
    //?}
    record GridEntry(int dimId, IGrid grid) {
    }
}