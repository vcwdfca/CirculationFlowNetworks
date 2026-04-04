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

import java.util.UUID;
import java.nio.file.Path;

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
//~ mc_imports
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
//? if <1.20 {
import com.github.bsideup.jabel.Desugar;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.DimensionManager;
//?} else {
/*import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("unused")
public final class NetworkManager {

    public static final NetworkManager INSTANCE = new NetworkManager();
    private static final Object FILE_IO_LOCK = new Object();
    private static final ExecutorService FILE_IO_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "cfn-file-io");
        thread.setDaemon(true);
        return thread;
    });
    private static File saveFile;
    private final ReferenceSet<INode> activeNodes = new ReferenceOpenHashSet<>();
    private final Object2ObjectMap<UUID, IGrid> grids = new Object2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Long2ReferenceMap<INode>> posNodes = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Long2ObjectMap<ReferenceSet<INode>>> scopeNode = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Object2ObjectMap<INode, LongSet>> nodeScope = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Long2ObjectMap<ReferenceSet<INode>>> nodeLocation = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Long2ObjectMap<LongSet>> pendingNodeValidation = new Int2ObjectOpenHashMap<>();
    private final ObjectSet<IGrid> markGird = new ObjectOpenHashSet<>();
    private boolean init;

    {
        posNodes.defaultReturnValue(Long2ReferenceMaps.emptyMap());
        scopeNode.defaultReturnValue(Long2ObjectMaps.emptyMap());
        nodeScope.defaultReturnValue(Object2ObjectMaps.emptyMap());
        nodeLocation.defaultReturnValue(Long2ObjectMaps.emptyMap());
        pendingNodeValidation.defaultReturnValue(Long2ObjectMaps.emptyMap());
    }

    public static File getSaveFile() {
        if (saveFile == null) {
            var path = getGridSavePath();
            //? if <1.20 {
            tryCreateDirectories(path);
            //?} else {
            /*tryCreateDirectories(path, "grid save directory");
            *///?}
            saveFile = path.toFile();
        }
        return saveFile;
    }

    static void runFileIoAsync(Runnable task) {
        FILE_IO_EXECUTOR.execute(() -> {
            try {
                task.run();
            } catch (Exception e) {
                CirculationFlowNetworks.LOGGER.warn("Asynchronous file task failed", e);
            }
        });
    }

    static void deleteFileAsync(File file) {
        runFileIoAsync(() -> deleteFile(file));
    }

    static void deleteFile(File file) {
        synchronized (FILE_IO_LOCK) {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException e) {
                CirculationFlowNetworks.LOGGER.warn("Failed to delete file {}", file.getAbsolutePath(), e);
            }
        }
    }

    //? if <1.20 {
    private static Path getGridSavePath() {
        return DimensionManager.getWorld(0)
                               .getSaveHandler()
                               .getWorldDirectory()
                               .toPath()
                               .resolve("circulation_grids");
    }
    //?}

    //? if <1.20 {
    static void tryCreateDirectories(Path path) {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            CirculationFlowNetworks.LOGGER.warn("Failed to create {} at {}", "grid save directory", path, e);
        }
    }

    static @Nullable NBTTagCompound tryReadCompressedNbt(File file, String context) {
        try {
            return readCompressedNbt(file);
        } catch (IOException e) {
            CirculationFlowNetworks.LOGGER.warn("Failed to read {} from {}", context, file.getAbsolutePath(), e);
            return null;
        }
    }

    static boolean tryWriteCompressedNbt(NBTTagCompound nbt, File file, String context) {
        try {
            writeCompressedNbt(nbt, file);
            return true;
        } catch (IOException e) {
            CirculationFlowNetworks.LOGGER.warn("Failed to write {} to {}", context, file.getAbsolutePath(), e);
            return false;
        }
    }

    static NBTTagCompound readCompressedNbt(File file) throws IOException {
        return CompressedStreamTools.read(file);
    }

    static void writeCompressedNbt(NBTTagCompound nbt, File file) throws IOException {
        synchronized (FILE_IO_LOCK) {
            CompressedStreamTools.safeWrite(nbt, file);
        }
    }
    //?} else if <1.21 {
    /*static boolean tryCreateDirectories(Path path, String context) {
        try {
            Files.createDirectories(path);
            return true;
        } catch (IOException e) {
            CirculationFlowNetworks.LOGGER.warn("Failed to create {} at {}", context, path, e);
            return false;
        }
    }

    static @Nullable CompoundTag tryReadCompressedNbt(File file, String context) {
        try {
            return readCompressedNbt(file);
        } catch (IOException e) {
            CirculationFlowNetworks.LOGGER.warn("Failed to read {} from {}", context, file.getAbsolutePath(), e);
            return null;
        }
    }

    static boolean tryWriteCompressedNbt(CompoundTag nbt, File file, String context) {
        try {
            writeCompressedNbt(nbt, file);
            return true;
        } catch (IOException e) {
            CirculationFlowNetworks.LOGGER.warn("Failed to write {} to {}", context, file.getAbsolutePath(), e);
            return false;
        }
    }

    static CompoundTag readCompressedNbt(File file) throws IOException {
        return NbtIo.readCompressed(file);
    }

    static void writeCompressedNbt(CompoundTag nbt, File file) throws IOException {
        synchronized (FILE_IO_LOCK) {
            NbtIo.writeCompressed(nbt, file);
        }
    }
    *///?} else {
    /*static boolean tryCreateDirectories(Path path, String context) {
        try {
            Files.createDirectories(path);
            return true;
        } catch (IOException e) {
            CirculationFlowNetworks.LOGGER.warn("Failed to create {} at {}", context, path, e);
            return false;
        }
    }

    static @Nullable CompoundTag tryReadCompressedNbt(File file, String context) {
        try {
            return readCompressedNbt(file);
        } catch (IOException e) {
            CirculationFlowNetworks.LOGGER.warn("Failed to read {} from {}", context, file.getAbsolutePath(), e);
            return null;
        }
    }

    static boolean tryWriteCompressedNbt(CompoundTag nbt, File file, String context) {
        try {
            writeCompressedNbt(nbt, file);
            return true;
        } catch (IOException e) {
            CirculationFlowNetworks.LOGGER.warn("Failed to write {} to {}", context, file.getAbsolutePath(), e);
            return false;
        }
    }

    static CompoundTag readCompressedNbt(File file) throws IOException {
        return NbtIo.readCompressed(file.toPath(), net.minecraft.nbt.NbtAccounter.unlimitedHeap());
    }

    static void writeCompressedNbt(CompoundTag nbt, File file) throws IOException {
        synchronized (FILE_IO_LOCK) {
            NbtIo.writeCompressed(nbt, file.toPath());
        }
    }
    *///?}

    //? if <1.20 {
    private static int getDimensionId(World world) {
        return world.provider.getDimension();
    }

    private static int getDimensionId(INode node) {
        return node.getDimensionId();
    }

    private static boolean isClientWorld(World world) {
        return world.isRemote;
    }

    private static List<net.minecraft.entity.player.EntityPlayer> getPlayers(World world) {
        return world.playerEntities;
    }

    private static BlockPos blockPosFromLong(long posLong) {
        return BlockPos.fromLong(posLong);
    }

    @Nullable
    private static TileEntity getBlockEntity(World world, BlockPos pos) {
        return world.getTileEntity(pos);
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
        return node.getDimensionId();
    }

    private static boolean isClientWorld(Level world) {
        return world.isClientSide;
    }

    @SuppressWarnings("unchecked")
    private static List<Player> getPlayers(Level world) {
        return (List<Player>) (List<?>) world.players();
    }

    private static BlockPos blockPosFromLong(long posLong) {
        return BlockPos.of(posLong);
    }

    @Nullable
    private static BlockEntity getBlockEntity(Level world, BlockPos pos) {
        return world.getBlockEntity(pos);
    }

    private static void destroyBlock(Level world, BlockPos pos) {
        world.destroyBlock(pos, true, null);
    }

    private static boolean isRegisteredDimension(int dimId) {
        return true;
    }
    *///?}

    public ReferenceSet<INode> getActiveNodes() {
        return activeNodes;
    }

    public boolean isInit() {
        return init;
    }

    @Nullable
    private static Integer resolveGridDimension(IGrid grid) {
        if (grid == null || grid.getNodes().isEmpty()) {
            return null;
        }

        Integer dimId = null;
        for (INode node : grid.getNodes()) {
            if (node == null) {
                continue;
            }
            int nodeDimId = node.getDimensionId();
            if (dimId == null) {
                dimId = nodeDimId;
                continue;
            }
            if (dimId != nodeDimId) {
                return null;
            }
        }
        return dimId;
    }

    private void registerNodeIndices(int dimId, INode node) {
        BlockPos pos = node.getPos();

        var pMap = posNodes.get(dimId);
        if (pMap == posNodes.defaultReturnValue()) {
            posNodes.put(dimId, pMap = new Long2ReferenceOpenHashMap<>());
        }
        //~ if >=1.20 '.toLong()' -> '.asLong()' {
        pMap.put(pos.toLong(), node);
        //~}

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
        //~ if >=1.20 '.toLong()' -> '.asLong()' {
        posNodes.get(dimId).remove(node.getPos().toLong());
        //~}

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

    //~ if >=1.20 '(World ' -> '(Level ' {
    //~ if >=1.20 '.toLong()' -> '.asLong()' {
    public @Nullable INode getNodeFromPos(World world, BlockPos pos) {
        return posNodes.get(getDimensionId(world)).get(pos.toLong());
    }
    //~}
    //~}

    public void onBlockEntityValidate(BlockEntityLifeCycleEvent.Validate event) {
        if (isClientWorld(event.getWorld())) return;
        var blockEntity = event.getBlockEntity();
        if (blockEntity instanceof INodeBlockEntity nbe) {
            int dimId = getDimensionId(event.getWorld());
            if (!init) {
                markPendingNodeValidation(dimId, event.getPos());
                return;
            }
            INode current = getNodeFromPos(event.getWorld(), event.getPos());
            INode actual = nbe.getNode();
            if (actual == null) {
                markPendingNodeValidation(dimId, event.getPos());
                return;
            }
            if (current != null && current != actual) {
                removeNode(current);
            }
            addNode(actual, blockEntity);
            clearPendingNodeValidationIfMatched(dimId, event.getPos(), actual);
        }
    }

    public Collection<IGrid> getAllGrids() {
        return grids.values();
    }

    public void onBlockEntityInvalidate(BlockEntityLifeCycleEvent.Invalidate event) {
        if (isClientWorld(event.getWorld())) return;
        clearPendingNodeValidation(getDimensionId(event.getWorld()), event.getPos());
        removeNode(getDimensionId(event.getWorld()), event.getPos());
    }

    //~ if >=1.20 '(World ' -> '(Level ' {
    public void validatePendingNodesInChunk(World world, int chunkX, int chunkZ) {
        //~}
        if (isClientWorld(world)) return;

        int dimId = getDimensionId(world);
        long chunkCoord = Functions.mergeChunkCoords(chunkX, chunkZ);
        LongSet pending = getPendingNodeValidation(dimId, chunkCoord);
        if (pending == null || pending.isEmpty()) {
            return;
        }

        LongSet positions = new LongOpenHashSet(pending);
        for (long posLong : positions) {
            var pos = blockPosFromLong(posLong);
            INode mapped = posNodes.get(dimId).get(posLong);
            var blockEntity = getBlockEntity(world, pos);

            if (blockEntity instanceof INodeBlockEntity nbe) {
                INode actual = nbe.getNode();
                if (mapped != actual) {
                    if (mapped != null) {
                        removeNode(mapped);
                    }
                    addNode(actual, blockEntity);
                    mapped = posNodes.get(dimId).get(posLong);
                }
            }

            if (mapped != null
                && mapped.isActive()
                && pos.equals(mapped.getPos())
                && blockEntity instanceof INodeBlockEntity nbe
                && nbe.getNode() == mapped) {
                clearPendingNodeValidation(dimId, posLong);
            } else if (mapped != null) {
                removeNode(mapped);
            } else {
                clearPendingNodeValidation(dimId, posLong);
            }
        }
    }

    //~ if >=1.20 '(World ' -> '(Level ' {
    public @Nonnull ReferenceSet<INode> getNodesCoveringPosition(World world, BlockPos pos) {
        return scopeNode.get(getDimensionId(world)).get(Functions.mergeChunkCoords(pos));
    }
    //~}
    //? if >=1.20 <1.21 {
    /*private static Path getGridSavePath() {
        return net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer()
                .getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                .resolve("circulation_grids");
    }
    *///?} else if >=1.21 {
    /*private static Path getGridSavePath() {
        return net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer()
                .getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                .resolve("circulation_grids");
    }
    *///?}

    //~ if >=1.20 '(World ' -> '(Level ' {
    public @Nonnull ReferenceSet<INode> getNodesCoveringPosition(World world, int chunkX, int chunkY) {
        return scopeNode.get(getDimensionId(world)).get(Functions.mergeChunkCoords(chunkX, chunkY));
    }

    public @Nonnull ReferenceSet<INode> getNodesInChunk(World world, int chunkX, int chunkZ) {
        var map = nodeLocation.get(getDimensionId(world));
        return map.get(Functions.mergeChunkCoords(chunkX, chunkZ));
    }
    //~}

    public void removeNode(int dim, BlockPos pos) {
        var pMap = posNodes.get(dim);
        if (pMap != null && pMap != posNodes.defaultReturnValue()) {
            //~ if >=1.20 '.toLong()' -> '.asLong()' {
            removeNode(pMap.get(pos.toLong()));
            //~}
        }
    }

    public void removeNode(INode removedNode) {
        if (removedNode == null || isClientWorld(removedNode.getWorld()) || !activeNodes.remove(removedNode)) return;

        NodeEventHooks.postRemoveNodePre(removedNode);
        int dimId = getDimensionId(removedNode);
        clearPendingNodeValidation(dimId, removedNode.getPos());

        var players = NodeNetworkRendering.getPlayers(removedNode.getGrid());
        if (players != null && !players.isEmpty()) {
            for (var player : players) {
                CirculationFlowNetworks.sendToPlayer(
                    new NodeNetworkRendering(player, removedNode, NodeNetworkRendering.NODE_REMOVE), player);
            }
        }

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
        oldGrid.markSnapshotDirty();
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
                oldGrid.markSnapshotDirty();
                markGird.add(oldGrid);

                var watchingPlayers = NodeNetworkRendering.getPlayers(oldGrid);
                for (int i = 1; i < components.size(); i++) {
                    IGrid splitGrid = allocGrid();
                    for (INode n : components.get(i)) {
                        assignNodeToGrid(n, splitGrid);
                        if (n instanceof IHubNode h) {
                            splitGrid.setHubNode(h);
                        }
                    }
                    if (watchingPlayers != null) {
                        for (var player : watchingPlayers) {
                            CirculationFlowNetworks.sendToPlayer(
                                new NodeNetworkRendering(player, splitGrid), player);
                        }
                    }
                }
            }
        }

        EnergyMachineManager.INSTANCE.removeNode(removedNode);
        ChargingManager.INSTANCE.removeNode(removedNode);

        NodeEventHooks.postRemoveNodePost(removedNode);
    }

    public @Nonnull AddNodeResult addNode(INode newNode) {
        return addNode(newNode, null);
    }

    //~ if >=1.20 'TileEntity' -> 'BlockEntity' {
    public @Nonnull AddNodeResult addNode(INode newNode, TileEntity blockEntity) {
        //~}
        if (newNode == null) {
            return AddNodeResult.failure(AddNodeResult.Status.NULL_NODE);
        }
        if (isClientWorld(newNode.getWorld())) {
            return AddNodeResult.failure(AddNodeResult.Status.CLIENT_WORLD);
        }
        if (!newNode.isActive()) {
            return AddNodeResult.failure(AddNodeResult.Status.NODE_INACTIVE);
        }
        if (activeNodes.contains(newNode)) {
            return AddNodeResult.failure(AddNodeResult.Status.ALREADY_ACTIVE);
        }

        if (NodeEventHooks.postAddNodePre(newNode, blockEntity)) {
            return AddNodeResult.failure(AddNodeResult.Status.EVENT_CANCELED);
        }

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
            if (blockEntity != null) {
                destroyBlock(world, pos);
            }
            return AddNodeResult.failure(AddNodeResult.Status.HUB_CONFLICT);
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
                dst.markSnapshotDirty();
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

        var players = NodeNetworkRendering.getPlayers(newNode.getGrid());
        if (players != null && !players.isEmpty()) {
            for (var player : players) {
                CirculationFlowNetworks.sendToPlayer(
                    new NodeNetworkRendering(player, newNode, NodeNetworkRendering.NODE_ADD), player);
            }
        }
        EnergyMachineManager.INSTANCE.addNode(newNode);
        ChargingManager.INSTANCE.addNode(newNode);

        NodeEventHooks.postAddNodePost(newNode, blockEntity);
        return AddNodeResult.success(newNode.getGrid());
    }

    private void assignNodeToGrid(INode node, IGrid grid) {
        markGird.add(grid);
        grid.getNodes().add(node);
        node.setGrid(grid);
        grid.markSnapshotDirty();
    }

    private IGrid allocGrid() {
        IGrid grid = new Grid(UUID.randomUUID());
        grids.put(grid.getId(), grid);
        EnergyMachineManager.INSTANCE.getInteraction().put(grid, new EnergyMachineManager.Interaction());
        markGird.add(grid);
        return grid;
    }

    private void destroyGrid(IGrid grid) {
        grids.remove(grid.getId());
        EnergyMachineManager.INSTANCE.getInteraction().remove(grid);
        markGird.remove(grid);
        deleteFileAsync(new File(getSaveFile(), grid.getId().toString() + ".dat"));
    }

    public void onServerStop() {
        scopeNode.clear();
        nodeScope.clear();
        nodeLocation.clear();
        activeNodes.clear();
        grids.clear();
        posNodes.clear();
        pendingNodeValidation.clear();
        saveFile = null;
        init = false;
    }

    public void saveGrid() {
        File saveDir = NetworkManager.getSaveFile();
        if (!markGird.isEmpty()) {
            List<IGrid> dirtyGrids = new ObjectArrayList<>(markGird);
            markGird.clear();
            for (IGrid grid : dirtyGrids) {
                tryWriteCompressedNbt(grid.serialize(), new File(saveDir, grid.getId().toString() + ".dat"), "grid " + grid.getId());
            }
        }
    }

    public void initGrid() {
        var f = getSaveFile();
        var entries = new ObjectArrayList<GridEntry>();
        if (!f.exists() || !f.isDirectory()) {
            EnergyMachineManager.INSTANCE.initGrid(entries);
            ChargingManager.INSTANCE.initGrid(entries);
            init = true;
            return;
        }

        File[] files = f.listFiles(file -> file.isFile() && file.getName().endsWith(".dat"));
        if (files == null || files.length == 0) {
            EnergyMachineManager.INSTANCE.initGrid(entries);
            ChargingManager.INSTANCE.initGrid(entries);
            init = true;
            return;
        }

        for (File file : files) {
            var nbt = tryReadCompressedNbt(file, "grid file " + file.getName());
            if (nbt == null) {
                continue;
            }
            //? if <1.20 {
            if (!nbt.hasKey("dim")) continue;
            //?} else if <1.21 {
            /*if (!nbt.contains("dim")) continue;
            var dimLoc = new net.minecraft.resources.ResourceLocation(nbt.getString("dim"));
            var dimResKey = net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.DIMENSION, dimLoc);
            var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
            if (server == null || server.getLevel(dimResKey) == null) continue;
            int dimId = dimLoc.hashCode();
            *///?} else {
            /*if (!nbt.contains("dim")) continue;
            var dimLoc = net.minecraft.resources.ResourceLocation.parse(nbt.getString("dim"));
            var dimResKey = net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.DIMENSION, dimLoc);
            var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
            if (server == null || server.getLevel(dimResKey) == null) continue;
            int dimId = dimLoc.hashCode();
            *///?}
            var grid = Grid.deserialize(nbt);
            if (grid == null) continue;
            if (grid.getNodes().isEmpty()) {
                file.delete();
                continue;
            }

            Integer resolvedDimId = resolveGridDimension(grid);
            if (resolvedDimId == null || !isRegisteredDimension(resolvedDimId)) {
                CirculationFlowNetworks.LOGGER.warn("Skipping grid {} due to inconsistent or unavailable dimension data", file.getName());
                continue;
            }
            entries.add(new GridEntry(resolvedDimId, grid));
        }

        for (var entry : entries) {
            var grid = entry.grid();
            grids.put(grid.getId(), grid);
            EnergyMachineManager.INSTANCE.getInteraction().put(grid, new EnergyMachineManager.Interaction());

            var registered = new ObjectArrayList<INode>();
            boolean collision = false;
            for (INode node : grid.getNodes()) {
                var l = posNodes.get(entry.dimId);
                //~ if >=1.20 '.toLong()' -> '.asLong()' {
                var p = node.getPos().toLong();
                //~}
                if (l.containsKey(p)) {
                    collision = true;
                    break;
                }
                activeNodes.add(node);
                registerNodeIndices(entry.dimId, node);
                markPendingNodeValidation(entry.dimId, node.getPos());
                registered.add(node);
            }
            if (collision) {
                for (INode node : registered) {
                    activeNodes.remove(node);
                    clearPendingNodeValidation(entry.dimId, node.getPos());
                    unregisterNodeIndices(entry.dimId, node);
                }
                grids.remove(grid.getId());
                EnergyMachineManager.INSTANCE.getInteraction().remove(grid);
                grid.getNodes().clear();
                grid.setHubNode(null);
            }
        }
        EnergyMachineManager.INSTANCE.initGrid(entries);
        ChargingManager.INSTANCE.initGrid(entries);
        init = true;
    }

    private void markPendingNodeValidation(int dimId, BlockPos pos) {
        long chunkCoord = Functions.mergeChunkCoords(pos);
        var dimMap = pendingNodeValidation.get(dimId);
        if (dimMap == pendingNodeValidation.defaultReturnValue()) {
            dimMap = new Long2ObjectOpenHashMap<>();
            dimMap.defaultReturnValue(LongSets.EMPTY_SET);
            pendingNodeValidation.put(dimId, dimMap);
        }
        var positions = dimMap.get(chunkCoord);
        if (positions == dimMap.defaultReturnValue()) {
            positions = new LongOpenHashSet();
            dimMap.put(chunkCoord, positions);
        }
        //~ if >=1.20 '.toLong()' -> '.asLong()' {
        positions.add(pos.toLong());
        //~}
    }

    private void clearPendingNodeValidationIfMatched(int dimId, BlockPos pos, INode node) {
        //~ if >=1.20 '.toLong()' -> '.asLong()' {
        if (node != null && posNodes.get(dimId).get(pos.toLong()) == node) {
        //~}
            clearPendingNodeValidation(dimId, pos);
        }
    }

    private void clearPendingNodeValidation(int dimId, BlockPos pos) {
        //~ if >=1.20 '.toLong()' -> '.asLong()' {
        clearPendingNodeValidation(dimId, pos.toLong());
        //~}
    }

    private void clearPendingNodeValidation(int dimId, long posLong) {
        var dimMap = pendingNodeValidation.get(dimId);
        if (dimMap == pendingNodeValidation.defaultReturnValue()) {
            return;
        }
        //~ if >=1.20 '.fromLong(' -> '.of(' {
        BlockPos pos = BlockPos.fromLong(posLong);
        //~}
        long chunkCoord = Functions.mergeChunkCoords(pos);
        var positions = dimMap.get(chunkCoord);
        if (positions == dimMap.defaultReturnValue()) {
            return;
        }
        positions.remove(posLong);
        if (positions.isEmpty()) {
            dimMap.remove(chunkCoord);
        }
        if (dimMap.isEmpty()) {
            pendingNodeValidation.remove(dimId);
        }
    }

    @Nullable
    private LongSet getPendingNodeValidation(int dimId, long chunkCoord) {
        var dimMap = pendingNodeValidation.get(dimId);
        if (dimMap == pendingNodeValidation.defaultReturnValue()) {
            return null;
        }
        var positions = dimMap.get(chunkCoord);
        return positions == dimMap.defaultReturnValue() ? null : positions;
    }

    public static final class AddNodeResult {

        private final Status status;
        @Nullable
        private final IGrid grid;
        private AddNodeResult(Status status, @Nullable IGrid grid) {
            this.status = status;
            this.grid = grid;
        }

        private static AddNodeResult success(@Nullable IGrid grid) {
            return new AddNodeResult(Status.SUCCESS, grid);
        }

        private static AddNodeResult failure(Status status) {
            return new AddNodeResult(status, null);
        }

        public boolean isSuccess() {
            return status == Status.SUCCESS;
        }

        public @Nonnull Status getStatus() {
            return status;
        }

        public @Nullable IGrid getGrid() {
            return grid;
        }

        public enum Status {
            SUCCESS,
            NULL_NODE,
            CLIENT_WORLD,
            NODE_INACTIVE,
            ALREADY_ACTIVE,
            EVENT_CANCELED,
            HUB_CONFLICT
        }
    }

    //? if <1.20 {
    @Desugar
        //?}
    record GridEntry(int dimId, IGrid grid) {
    }
}
