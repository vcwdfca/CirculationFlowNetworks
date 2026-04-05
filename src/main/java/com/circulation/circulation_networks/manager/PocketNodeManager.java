package com.circulation.circulation_networks.manager;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import com.circulation.circulation_networks.api.INodeBlockEntity;
import com.circulation.circulation_networks.api.node.INode;
import com.circulation.circulation_networks.api.node.NodeType;
import com.circulation.circulation_networks.packets.PocketNodeRendering;
import com.circulation.circulation_networks.pocket.PocketNodeHost;
import com.circulation.circulation_networks.pocket.PocketNodeRecord;
import com.circulation.circulation_networks.registry.NodeTypes;
import com.circulation.circulation_networks.registry.PocketNodeItems;
import com.circulation.circulation_networks.utils.Functions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
//~ mc_imports
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
//? if <1.20 {
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraft.entity.player.EntityPlayerMP;
//?} else {
/*import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.World;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.nbt.Tag;
*///?}

import javax.annotation.Nullable;
import java.io.File;

public final class PocketNodeManager {

    public static final PocketNodeManager INSTANCE = new PocketNodeManager();
    private final Int2ObjectMap<Long2ObjectMap<PocketNodeHost>> activeHosts = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Long2ObjectMap<PocketNodeRecord>> pendingHosts = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Long2ObjectMap<LongSet>> activeChunkIndex = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Long2ObjectMap<LongSet>> pendingChunkIndex = new Int2ObjectOpenHashMap<>();
    private boolean loaded;
    private boolean dirty;
    private PocketNodeManager() {
    }

    private static Long2ObjectMap<LongSet> getChunkIndex(Int2ObjectMap<Long2ObjectMap<LongSet>> index, int dimId) {
        return index.computeIfAbsent(dimId, ignored -> new Long2ObjectOpenHashMap<>());
    }

    private static void indexChunkPosition(Long2ObjectMap<LongSet> chunkMap, long chunkCoord, long posLong) {
        LongSet positions = chunkMap.get(chunkCoord);
        if (positions == null) {
            positions = new LongOpenHashSet();
            chunkMap.put(chunkCoord, positions);
        }
        positions.add(posLong);
    }

    private static void unindexChunkPosition(@Nullable Long2ObjectMap<LongSet> chunkMap, long chunkCoord, long posLong) {
        if (chunkMap == null) {
            return;
        }
        LongSet positions = chunkMap.get(chunkCoord);
        if (positions == null) {
            return;
        }
        positions.remove(posLong);
        if (positions.isEmpty()) {
            chunkMap.remove(chunkCoord);
        }
    }

    private static @Nullable LongSet getChunkPositions(@Nullable Long2ObjectMap<LongSet> chunkMap, long chunkCoord) {
        return chunkMap == null ? null : chunkMap.get(chunkCoord);
    }

    //~ if >=1.20 'World ' -> 'Level ' {
    private static boolean shouldDiscardPendingRecord(World world, PocketNodeRecord record, INode node, NetworkManager.AddNodeResult addResult) {
        if (!node.isActive()) {
            return true;
        }
        if (isHostChunkLoaded(world, record.pos()) && !hasHostBlock(world, record.pos())) {
            return true;
        }
        return addResult.getStatus() == NetworkManager.AddNodeResult.Status.HUB_CONFLICT;
    }

    private static void logDeferredActivation(PocketNodeRecord record) {
        CirculationFlowNetworks.LOGGER.debug(
            "Deferred pocket node activation type={} pos={} dim={}",
            record.nodeType().id(),
            record.pos(),
            record.dimensionId()
        );
    }
    //~}

    //~ if >=1.20 '(World ' -> '(Level ' {
    private static boolean canAdoptLoadedPocketNode(World world, PocketNodeRecord record, INode node) {
        if (node == null || !node.isActive()) {
            return false;
        }
        if (node.getNodeType() != record.nodeType()) {
            return false;
        }
        return isRecoverablePocketNode(world, node);
    }

    private static boolean isRecoverablePocketNode(World world, INode node) {
        if (world == null || node == null || !node.getNodeType().allowsPocketNode()) {
            return false;
        }
        BlockPos pos = node.getPos();
        if (!isHostChunkLoaded(world, pos) || !hasHostBlock(world, pos)) {
            return false;
        }
        return NetworkManager.INSTANCE.getNodeFromPos(world, pos) == node;
    }
    //~}

    //? if <1.20 {
    @Nullable
    private static net.minecraft.util.EnumFacing inferAttachmentFace(World world, BlockPos pos) {
        if (world == null || !world.isBlockLoaded(pos)) {
            return null;
        }
        for (net.minecraft.util.EnumFacing face : net.minecraft.util.EnumFacing.values()) {
            BlockPos adjacentPos = pos.offset(face);
            if (!world.isBlockLoaded(adjacentPos)) {
                continue;
            }
            var adjacentState = world.getBlockState(adjacentPos);
            if (adjacentState.getBlock().isAir(adjacentState, world, adjacentPos)) {
                return face;
            }
        }
        return net.minecraft.util.EnumFacing.UP;
    }
    //?} else {
    /*@Nullable
    private static net.minecraft.core.Direction inferAttachmentFace(Level world, BlockPos pos) {
        if (world == null || !world.isLoaded(pos)) {
            return null;
        }
        for (net.minecraft.core.Direction face : net.minecraft.core.Direction.values()) {
            BlockPos adjacentPos = pos.relative(face);
            if (!world.isLoaded(adjacentPos)) {
                continue;
            }
            var adjacentState = world.getBlockState(adjacentPos);
            if (adjacentState.isAir()) {
                return face;
            }
        }
        return net.minecraft.core.Direction.UP;
    }
    *///?}

    //? if <1.20 {
    private static @Nullable World resolveWorld(int dimId) {
        return DimensionManager.getWorld(dimId);
    }

    private static int getDimensionId(World world) {
        return world.provider.getDimension();
    }

    private static boolean isClientWorld(World world) {
        return world.isRemote;
    }

    private static boolean isHostChunkLoaded(World world, BlockPos pos) {
        return world.isBlockLoaded(pos);
    }

    private static boolean hasHostBlock(World world, BlockPos pos) {
        if (!world.isBlockLoaded(pos)) {
            return false;
        }
        var state = world.getBlockState(pos);
        return !state.getBlock().isAir(state, world, pos) && !(world.getTileEntity(pos) instanceof INodeBlockEntity);
    }
    //?} else if <1.21 {
    /*private static @Nullable Level resolveWorld(int dimId) {
        MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }
        for (Level level : server.getAllLevels()) {
            if (level.dimension().location().hashCode() == dimId) {
                return level;
            }
        }
        return null;
    }

    private static int getDimensionId(Level world) {
        return world.dimension().location().hashCode();
    }

    private static boolean isClientWorld(Level world) {
        return world.isClientSide;
    }

    private static boolean isHostChunkLoaded(Level world, BlockPos pos) {
        return world.hasChunkAt(pos);
    }

    private static boolean hasHostBlock(Level world, BlockPos pos) {
        return world.hasChunkAt(pos)
            && !world.getBlockState(pos).isAir()
            && !(world.getBlockEntity(pos) instanceof INodeBlockEntity);
    }
    *///?} else {
    /*private static @Nullable Level resolveWorld(int dimId) {
        MinecraftServer server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }
        for (Level level : server.getAllLevels()) {
            if (level.dimension().location().hashCode() == dimId) {
                return level;
            }
        }
        return null;
    }

    private static int getDimensionId(Level world) {
        return world.dimension().location().hashCode();
    }

    private static boolean isClientWorld(Level world) {
        return world.isClientSide;
    }

    private static boolean isHostChunkLoaded(Level world, BlockPos pos) {
        return world.hasChunkAt(pos);
    }

    private static boolean hasHostBlock(Level world, BlockPos pos) {
        return world.hasChunkAt(pos)
            && !world.getBlockState(pos).isAir()
            && !(world.getBlockEntity(pos) instanceof INodeBlockEntity);
    }
    *///?}

    //~ if >=1.20 'NBTTagCompound' -> 'CompoundTag' {
    //~ if >=1.20 'NBTTagList' -> 'ListTag' {
    //~ if >=1.20 '.tagCount()' -> '.size()' {
    public void load() {
        if (loaded) {
            return;
        }

        clearState();
        loaded = true;

        File saveFile = getSaveFile();
        if (!saveFile.exists()) {
            return;
        }

        NBTTagCompound nbt = NetworkManager.tryReadCompressedNbt(saveFile, "pocket node save");
        if (nbt == null) {
            return;
        }

        //? if <1.20 {
        NBTTagList nodes = nbt.getTagList("nodes", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < nodes.tagCount(); i++) {
            PocketNodeRecord record = PocketNodeRecord.deserialize(nodes.getCompoundTagAt(i));
            //?} else {
        /*NBTTagList nodes = nbt.getList("nodes", Tag.TAG_COMPOUND);
        for (int i = 0; i < nodes.tagCount(); i++) {
            PocketNodeRecord record = PocketNodeRecord.deserialize(nodes.getCompound(i));
        *///?}
            if (record != null) {
                putPending(record);
            }
        }

        for (var dimEntry : new ObjectArrayList<>(pendingHosts.int2ObjectEntrySet())) {
            for (var record : new ObjectArrayList<>(dimEntry.getValue().values())) {
                RegisterPocketNodeResult activated = tryActivate(record, true);
                if (!activated.isSuccess()) {
                    logDeferredActivation(record);
                }
            }
        }

        recoverPocketHostsFromLoadedNodes();
    }
    //~}
    //~}
    //~}

    //~ if >=1.20 'NBTTagCompound' -> 'CompoundTag' {
    //~ if >=1.20 'NBTTagList' -> 'ListTag' {
    //~ if >=1.20 '.appendTag(' -> '.add(' {
    //~ if >=1.20 '.setTag(' -> '.put(' {
    public void save() {
        if (!loaded || (!dirty && activeHosts.isEmpty() && pendingHosts.isEmpty())) {
            return;
        }

        File saveFile = getSaveFile();
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList list = new NBTTagList();

        for (var dimEntry : activeHosts.int2ObjectEntrySet()) {
            for (var host : dimEntry.getValue().values()) {
                list.appendTag(host.toRecord().serialize());
            }
        }
        for (var dimEntry : pendingHosts.int2ObjectEntrySet()) {
            for (var record : dimEntry.getValue().values()) {
                list.appendTag(record.serialize());
            }
        }

        nbt.setTag("nodes", list);

        if (NetworkManager.tryWriteCompressedNbt(nbt, saveFile, "pocket node save")) {
            dirty = false;
        }
    }
    //~}
    //~}
    //~}
    //~}

    public void onServerStop() {
        clearState();
        loaded = false;
        dirty = false;
    }

    public void markDirty() {
        if (loaded) {
            dirty = true;
        }
    }

    //~ if >=1.20 'World ' -> 'Level ' {
    public void onChunkLoad(World world, int chunkX, int chunkZ) {
        if (!loaded || isClientWorld(world)) {
            return;
        }
        int dimId = getDimensionId(world);
        long chunkCoord = Functions.mergeChunkCoords(chunkX, chunkZ);

        LongSet activePositions = getChunkPositions(activeChunkIndex.get(dimId), chunkCoord);
        if (activePositions != null && !activePositions.isEmpty()) {
            Long2ObjectMap<PocketNodeHost> activeDimMap = activeHosts.get(dimId);
            for (long posLong : new LongOpenHashSet(activePositions)) {
                PocketNodeHost host = activeDimMap == null ? null : activeDimMap.get(posLong);
                if (host == null) {
                    continue;
                }
                if (!hasHostBlock(world, host.record().pos())) {
                    removeActiveHost(dimId, posLong);
                    NetworkManager.INSTANCE.removeNode(host.node());
                    syncRemove(dimId, host.record().pos());
                    dirty = true;
                }
            }
        }

        LongSet positions = getChunkPositions(pendingChunkIndex.get(dimId), chunkCoord);
        if (positions == null || positions.isEmpty()) {
            return;
        }
        for (long posLong : new LongOpenHashSet(positions)) {
            PocketNodeRecord record = getPendingDimMap(dimId).get(posLong);
            if (record != null) {
                tryActivate(record, true);
            }
        }
    }
    //~}

    //~ if >=1.20 'net.minecraft.util.EnumFacing' -> 'net.minecraft.core.Direction' {
    //~ if >=1.20 'World ' -> 'Level ' {
    //~ if >=1.20 '.toLong()' -> '.asLong()' {
    public RegisterPocketNodeResult registerPocketNodeDetailed(World world, BlockPos pos, NodeType<?> nodeType, @Nullable net.minecraft.util.EnumFacing attachmentFace, @Nullable String customName) {
        if (isClientWorld(world) || nodeType == null || !nodeType.allowsPocketNode() || NodeTypes.getById(nodeType.id()) != nodeType) {
            return RegisterPocketNodeResult.FAILED;
        }

        int dimId = getDimensionId(world);
        long posLong = pos.toLong();
        if (getActiveDimMap(dimId).containsKey(posLong) || getPendingDimMap(dimId).containsKey(posLong)) {
            return RegisterPocketNodeResult.OCCUPIED;
        }
        if (NetworkManager.INSTANCE.getNodeFromPos(world, pos) != null) {
            return RegisterPocketNodeResult.OCCUPIED;
        }
        if (!isHostChunkLoaded(world, pos) || !hasHostBlock(world, pos)) {
            return RegisterPocketNodeResult.FAILED;
        }

        PocketNodeRecord record = new PocketNodeRecord(dimId, pos, nodeType, attachmentFace, customName);
        RegisterPocketNodeResult activated = tryActivate(record, false);
        if (activated.isSuccess()) {
            dirty = true;
            syncAdd(record);
        }
        return activated;
    }
    //~}
    //~}
    //~}

    //~ if >=1.20 'World ' -> 'Level ' {
    //~ if >=1.20 '.toLong()' -> '.asLong()' {
    public boolean removePocketNode(World world, BlockPos pos, boolean dropItem) {
        if (!loaded) {
            return false;
        }
        int dimId = getDimensionId(world);
        long posLong = pos.toLong();
        PocketNodeHost activeHost = removeActiveHost(dimId, posLong);
        if (activeHost != null) {
            NetworkManager.INSTANCE.removeNode(activeHost.node());
            if (dropItem) {
                dropItem(world, activeHost.record());
            }
            syncRemove(dimId, pos);
            dirty = true;
            return true;
        }
        PocketNodeRecord pendingRecord = removePending(dimId, posLong);
        if (pendingRecord != null) {
            if (dropItem) {
                dropItem(world, pendingRecord);
            }
            syncRemove(dimId, pos);
            dirty = true;
            return true;
        }
        return false;
    }
    //~}
    //~}

    //~ if >=1.20 'World ' -> 'Level ' {
    public void onHostBlockBroken(World world, BlockPos pos) {
        if (!loaded || isClientWorld(world)) {
            return;
        }
        removePocketNode(world, pos, true);
    }

    public ObjectList<PocketNodeRecord> getActiveRecords(int dimId) {
        ObjectList<PocketNodeRecord> result = new ObjectArrayList<>();
        Long2ObjectMap<PocketNodeHost> dimMap = activeHosts.get(dimId);
        if (dimMap == null) {
            return result;
        }
        for (var host : dimMap.values()) {
            result.add(host.toRecord());
        }
        return result;
    }
    //~}

    //~ if >=1.20 'World ' -> 'Level ' {
    //~ if >=1.20 '.toLong()' -> '.asLong()' {
    public boolean isActivePocketNode(World world, BlockPos pos, @Nullable NodeType<?> nodeType) {
        if (!loaded || world == null || isClientWorld(world)) {
            return false;
        }
        return isActivePocketNode(getDimensionId(world), pos, nodeType);
    }

    public boolean isActivePocketNode(int dimId, BlockPos pos, @Nullable NodeType<?> nodeType) {
        if (!loaded || pos == null) {
            return false;
        }
        Long2ObjectMap<PocketNodeHost> dimMap = activeHosts.get(dimId);
        if (dimMap == null) {
            return false;
        }
        PocketNodeHost host = dimMap.get(pos.toLong());
        if (host == null) {
            return false;
        }
        return nodeType == null || host.record().nodeType() == nodeType;
    }
    //~}
    //~}

    //~ if >=1.20 '.toLong()' -> '.asLong()' {
    private RegisterPocketNodeResult tryActivate(PocketNodeRecord record, boolean dropItemOnConflict) {
        //~ if >=1.20 'World ' -> 'Level ' {
        World world = resolveWorld(record.dimensionId());
        //~}
        if (world == null) {
            return RegisterPocketNodeResult.FAILED;
        }

        int dimId = record.dimensionId();
        long posLong = record.pos().toLong();

        if (isHostChunkLoaded(world, record.pos()) && !hasHostBlock(world, record.pos())) {
            removePending(dimId, posLong);
            dirty = true;
            return RegisterPocketNodeResult.FAILED;
        }

        INode mappedNode = NetworkManager.INSTANCE.getNodeFromPos(world, record.pos());
        if (mappedNode != null) {
            if (canAdoptLoadedPocketNode(world, record, mappedNode)) {
                removePending(dimId, posLong);
                putActive(new PocketNodeHost(record, mappedNode));
                dirty = true;
                return RegisterPocketNodeResult.SUCCESS;
            }
            removePending(dimId, posLong);
            dirty = true;
            return RegisterPocketNodeResult.OCCUPIED;
        }

        INode node;
        try {
            node = Functions.createNode(record.nodeType(), record.createNodeContext(world));
        } catch (IllegalArgumentException ex) {
            removePending(dimId, posLong);
            dirty = true;
            CirculationFlowNetworks.LOGGER.warn(
                "Skipping legacy pocket node record with unsupported type={} pos={} dim={}",
                record.nodeType().id(),
                record.pos(),
                record.dimensionId(),
                ex
            );
            return RegisterPocketNodeResult.FAILED;
        }
        if (record.customName() != null) {
            node.setCustomName(record.customName());
        }
        node.setActive(true);
        NetworkManager.AddNodeResult addResult = NetworkManager.INSTANCE.addNode(node);
        if (!addResult.isSuccess()) {
            node.setActive(false);
            if (shouldDiscardPendingRecord(world, record, node, addResult)) {
                removePending(dimId, posLong);
                if (dropItemOnConflict
                    && addResult.getStatus() == NetworkManager.AddNodeResult.Status.HUB_CONFLICT
                    && isHostChunkLoaded(world, record.pos())) {
                    dropItem(world, record);
                }
                dirty = true;
            }
            CirculationFlowNetworks.LOGGER.warn(
                "Failed to activate pocket node type={} pos={} dim={} status={}",
                record.nodeType().id(),
                record.pos(),
                record.dimensionId(),
                addResult.getStatus()
            );
            return addResult.getStatus() == NetworkManager.AddNodeResult.Status.HUB_CONFLICT
                ? RegisterPocketNodeResult.HUB_CONFLICT
                : RegisterPocketNodeResult.FAILED;
        }
        removePending(dimId, posLong);
        putActive(new PocketNodeHost(record, node));
        return RegisterPocketNodeResult.SUCCESS;
    }
    //~}

    //~ if >=1.20 '.toLong()' -> '.asLong()' {
    private void putActive(PocketNodeHost host) {
        int dimId = host.record().dimensionId();
        long posLong = host.record().pos().toLong();
        long chunkCoord = Functions.mergeChunkCoords(host.record().pos());
        getActiveDimMap(dimId).put(posLong, host);
        indexChunkPosition(getChunkIndex(activeChunkIndex, dimId), chunkCoord, posLong);
    }

    private @org.jetbrains.annotations.Nullable PocketNodeHost removeActiveHost(int dimId, long posLong) {
        Long2ObjectMap<PocketNodeHost> dimMap = activeHosts.get(dimId);
        if (dimMap == null) {
            return null;
        }
        PocketNodeHost removed = dimMap.remove(posLong);
        if (removed != null) {
            unindexChunkPosition(activeChunkIndex.get(dimId), Functions.mergeChunkCoords(removed.record().pos()), posLong);
            if (dimMap.isEmpty()) {
                activeHosts.remove(dimId);
                activeChunkIndex.remove(dimId);
            }
        }
        return removed;
    }
    //~}

    //~ if >=1.20 '.toLong()' -> '.asLong()' {
    private void putPending(PocketNodeRecord record) {
        int dimId = record.dimensionId();
        long posLong = record.pos().toLong();
        long chunkCoord = Functions.mergeChunkCoords(record.pos());
        getPendingDimMap(dimId).put(posLong, record);
        indexChunkPosition(getChunkIndex(pendingChunkIndex, dimId), chunkCoord, posLong);
    }

    private @org.jetbrains.annotations.Nullable PocketNodeRecord removePending(int dimId, long posLong) {
        Long2ObjectMap<PocketNodeRecord> dimMap = pendingHosts.get(dimId);
        if (dimMap == null) {
            return null;
        }
        PocketNodeRecord removed = dimMap.remove(posLong);
        if (removed != null) {
            unindexChunkPosition(pendingChunkIndex.get(dimId), Functions.mergeChunkCoords(removed.pos()), posLong);
            if (dimMap.isEmpty()) {
                pendingHosts.remove(dimId);
                pendingChunkIndex.remove(dimId);
            }
        }
        return removed;
    }

    private Long2ObjectMap<PocketNodeHost> getActiveDimMap(int dimId) {
        return activeHosts.computeIfAbsent(dimId, ignored -> new Long2ObjectOpenHashMap<>());
    }

    private Long2ObjectMap<PocketNodeRecord> getPendingDimMap(int dimId) {
        return pendingHosts.computeIfAbsent(dimId, ignored -> new Long2ObjectOpenHashMap<>());
    }
    //~}

    //~ if >=1.20 'World ' -> 'Level ' {
    private void dropItem(World world, PocketNodeRecord record) {
        ItemStack stack = PocketNodeItems.createStack(record.nodeType());
        if (stack.isEmpty()) {
            return;
        }
        double x = record.pos().getX() + 0.5D;
        double y = record.pos().getY() + 0.5D;
        double z = record.pos().getZ() + 0.5D;
        //? if <1.20 {
        world.spawnEntity(new EntityItem(world, x, y, z, stack));
        //?} else {
        /*world.addFreshEntity(new ItemEntity(world, x, y, z, stack));
         *///?}
    }
    //~}

    private void syncAdd(PocketNodeRecord record) {
        for (var player : getPlayers(record.dimensionId())) {
            CirculationFlowNetworks.sendToPlayer(new PocketNodeRendering(record), player);
        }
    }

    private void syncRemove(int dimId, BlockPos pos) {
        for (var player : getPlayers(dimId)) {
            CirculationFlowNetworks.sendToPlayer(new PocketNodeRendering(dimId, pos), player);
        }
    }

    //~ if >=1.20 'EntityPlayerMP' -> 'ServerPlayer' {
    //~ if >=1.20 'World ' -> 'Level ' {
    private ObjectList<EntityPlayerMP> getPlayers(int dimId) {
        ObjectList<EntityPlayerMP> players = new ObjectArrayList<>();
        World world = resolveWorld(dimId);
        if (world == null) {
            return players;
        }
        //? if <1.20 {
        for (var player : world.playerEntities) {
            if (player instanceof EntityPlayerMP serverPlayer) {
                players.add(serverPlayer);
            }
        }
        //?} else {
        /*for (var player : world.players()) {
            if (player instanceof EntityPlayerMP serverPlayer) {
                players.add(serverPlayer);
            }
        }
        *///?}
        return players;
    }
    //~}
    //~}

    //~ if >=1.20 'World ' -> 'Level ' {
    //~ if >=1.20 '.toLong()' -> '.asLong()' {
    private void recoverPocketHostsFromLoadedNodes() {
        for (INode node : new ObjectArrayList<>(NetworkManager.INSTANCE.getActiveNodes())) {
            World world = node.getWorld();
            if (world == null || isClientWorld(world) || !isRecoverablePocketNode(world, node)) {
                continue;
            }
            int dimId = getDimensionId(world);
            long posLong = node.getPos().toLong();
            if (getActiveDimMap(dimId).containsKey(posLong) || getPendingDimMap(dimId).containsKey(posLong)) {
                continue;
            }
            PocketNodeRecord record = new PocketNodeRecord(
                dimId,
                node.getPos(),
                node.getNodeType(),
                inferAttachmentFace(world, node.getPos()),
                node.getCustomName()
            );
            putActive(new PocketNodeHost(record, node));
            dirty = true;
            CirculationFlowNetworks.LOGGER.warn(
                "Recovered pocket node state from loaded grid node type={} pos={} dim={}",
                node.getNodeType().id(),
                node.getPos(),
                dimId
            );
        }
    }
    //~}
    //~}

    private void clearState() {
        activeHosts.clear();
        pendingHosts.clear();
        activeChunkIndex.clear();
        pendingChunkIndex.clear();
    }

    private File getSaveFile() {
        return new File(NetworkManager.getSaveFile(), "PocketNodes.dat");
    }

    public enum RegisterPocketNodeResult {
        SUCCESS,
        OCCUPIED,
        HUB_CONFLICT,
        FAILED;

        public boolean isSuccess() {
            return this == SUCCESS;
        }
    }
}
