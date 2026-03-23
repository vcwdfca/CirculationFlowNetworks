package com.circulation.circulation_networks.manager;

import com.circulation.circulation_networks.api.IGrid;
import com.circulation.circulation_networks.api.INodeBlockEntity;
import com.circulation.circulation_networks.api.hub.PermissionMode;
import com.circulation.circulation_networks.api.node.IHubNode;
import com.circulation.circulation_networks.events.BlockEntityLifeCycleEvent;
import com.circulation.circulation_networks.network.hub.HubChannel;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceSet;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

import static com.circulation.circulation_networks.network.nodes.HubNode.EMPTY;

public final class HubChannelManager {

    public static final HubChannelManager INSTANCE = new HubChannelManager();

    private final Map<UUID, HubChannel> channels = new Object2ReferenceOpenHashMap<>();
    private final Reference2ObjectMap<IHubNode, UUID> hubChannels = new Reference2ObjectOpenHashMap<>();

    public void register(IHubNode hub, UUID channelId, String name, PermissionMode permissionMode) {
        if (channelId == null || name == null) return;
        var grid = hub.getGrid();
        if (grid == null) return;

        unregister(hub);

        var channel = channels.get(channelId);
        if (channel == null) {
            channel = new HubChannel(channelId, name, permissionMode);
            channels.put(channelId, channel);
        }
        channel.addGrid(grid);
        hubChannels.put(hub, channelId);
    }

    public void unregister(IHubNode hub) {
        var oldChannelId = hubChannels.remove(hub);
        if (oldChannelId == EMPTY) return;

        var channel = channels.get(oldChannelId);
        if (channel != null) {
            var grid = hub.getGrid();
            if (grid != null) {
                channel.removeGrid(grid);
            }
            if (channel.getGrids().isEmpty()) {
                channels.remove(oldChannelId);
            }
        }
    }

    @Nullable
    public ReferenceSet<IGrid> getChannelGrids(UUID channelId) {
        var channel = channels.get(channelId);
        return channel != null ? channel.getGrids() : null;
    }

    public void onBlockEntityValidate(BlockEntityLifeCycleEvent.Validate event) {
        if (isClientWorld(event.getWorld())) return;
        var te = event.getBlockEntity();
        if (te instanceof INodeBlockEntity hubTE) {
            var node = hubTE.getNode();
            if (node instanceof IHubNode hub) {
                var channelId = hub.getChannelId();
                var channelName = hub.getChannelName();
                var p = hub.getPermissionMode();
                if (!channelId.equals(EMPTY) && !channelName.isEmpty()) {
                    register(hub, channelId, channelName, p);
                }
            }
        }
    }

    public void onBlockEntityInvalidate(BlockEntityLifeCycleEvent.Invalidate event) {
        if (isClientWorld(event.getWorld())) return;
        var te = event.getBlockEntity();
        if (te instanceof INodeBlockEntity hubTE) {
            var node = hubTE.getNode();
            if (node instanceof IHubNode hub) {
                unregister(hub);
            }
        }
    }

    public void onServerStop() {
        channels.clear();
        hubChannels.clear();
    }

    //? if <1.20 {
    private static boolean isClientWorld(net.minecraft.world.World world) {
        return world.isRemote;
    }
    //?} else {
    /*private static boolean isClientWorld(net.minecraft.world.level.Level world) {
        return world.isClientSide;
    }
    *///?}
}