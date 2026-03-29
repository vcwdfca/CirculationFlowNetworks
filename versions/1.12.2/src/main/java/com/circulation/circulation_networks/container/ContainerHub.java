package com.circulation.circulation_networks.container;

import com.circulation.circulation_networks.api.hub.NodeSnapshotList;
import com.circulation.circulation_networks.api.hub.PermissionSnapshotList;
import com.circulation.circulation_networks.api.hub.ChargingPreference;
import com.circulation.circulation_networks.manager.EnergyMachineManager;
import com.circulation.circulation_networks.manager.HubChannelManager;
import com.circulation.circulation_networks.tiles.nodes.TileEntityHub;
import com.circulation.circulation_networks.utils.GuiSync;
import com.circulation.circulation_networks.utils.HubPlatformServices;
import net.minecraft.entity.player.EntityPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

import static com.circulation.circulation_networks.network.nodes.HubNode.EMPTY;

public class ContainerHub extends CFNBaseContainer {

    private static final long ENERGY_REFRESH_INTERVAL = 10L;
    private static final byte[] EMPTY_PERMISSION_SNAPSHOT = PermissionSnapshotList.EMPTY.toBytes();
    private static final byte[] EMPTY_NODE_SNAPSHOT = NodeSnapshotList.EMPTY.toBytes();

    public final ComponentSlotLayout playerInvLayout;
    public final ComponentSlotLayout[] slots = new ComponentSlotLayout[5];
    public final TileEntityHub te;
    @GuiSync(0)
    public String input;
    @GuiSync(1)
    public String output;
    public UUID channelId = EMPTY;
    @GuiSync(2)
    public String channelIdString = EMPTY.toString();
    @GuiSync(3)
    public String channelName = "";
    @GuiSync(4)
    public byte chargingModeByte = (byte) 0b111111;
    @GuiSync(5)
    public byte[] availableChannelsSnapshot;
    @GuiSync(6)
    public byte[] onlinePlayersSnapshot;
    @GuiSync(7)
    public byte[] nodesSnapshot;
    public PermissionSnapshotList availableChannels;
    public PermissionSnapshotList onlinePlayers;
    public NodeSnapshotList nodes;
    public ChargingPreference chargingMode = ChargingPreference.defaultAll();
    private long availableChannelsVersion = Long.MIN_VALUE;
    private long onlinePlayersChannelVersion = Long.MIN_VALUE;
    private long onlinePlayersPlatformVersion = Long.MIN_VALUE;
    private long nodeSnapshotVersion = Long.MIN_VALUE;
    private UUID nodeGridId;

    public ContainerHub(EntityPlayer player, TileEntityHub te) {
        super(player);
        this.te = te;

        playerInvLayout = ComponentSlotLayout.playerInventory(player.inventory).build(this);
        for (int i = 0; i < 5; i++) {
            slots[i] = new ComponentSlotLayout().addSlot(te.getPlugins(), i, 0, 0).build(this);
        }
        if (te.getWorld().isRemote) {
            input = "0";
            output = "0";
            chargingMode  = ChargingPreference.defaultAll();
            availableChannels = PermissionSnapshotList.EMPTY;
            onlinePlayers = PermissionSnapshotList.EMPTY;
            nodes = NodeSnapshotList.EMPTY;
            onlinePlayersSnapshot = EMPTY_PERMISSION_SNAPSHOT.clone();
            availableChannelsSnapshot = EMPTY_PERMISSION_SNAPSHOT.clone();
            nodesSnapshot = EMPTY_NODE_SNAPSHOT.clone();
        } else {
            var energy = EnergyMachineManager.INSTANCE.getInteraction().get(te.getNode().getGrid());
            input = energy.getInput().toString();
            output = energy.getOutput().toString();
            chargingMode = te.getNode().getChargingPreference(player.getUniqueID());
            syncChannelState();
            refreshSnapshots(true);
        }
    }

    @Override
    public boolean canInteractWith(@NotNull EntityPlayer playerIn) {
        return playerIn.getDistanceSq(te.getPos().getX() + 0.5D, te.getPos().getY() + 0.5D, te.getPos().getZ() + 0.5D) <= 128;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (te.getWorld().isRemote) return;
        boolean channelChanged = syncChannelState();
        chargingMode = te.getNode().getChargingPreference(player.getUniqueID());
        chargingModeByte = chargingMode.toByte();
        refreshSnapshots(channelChanged);
        if (te.getWorld().getTotalWorldTime() % ENERGY_REFRESH_INTERVAL != 0) return;
        var energy = EnergyMachineManager.INSTANCE.getInteraction().get(te.getNode().getGrid());
        input = energy.getInput().toString();
        output = energy.getOutput().toString();
    }

    private void refreshSnapshots(boolean force) {
        long channelSnapshotVersion = HubChannelManager.INSTANCE.getSnapshotVersion();
        if (force || availableChannelsVersion != channelSnapshotVersion) {
            availableChannels = HubChannelManager.INSTANCE.getAccessibleChannelSnapshots(player.getUniqueID());
            availableChannelsSnapshot = availableChannels.toBytes();
            availableChannelsVersion = channelSnapshotVersion;
        }

        if (channelId.equals(EMPTY)) {
            if (force || onlinePlayers != PermissionSnapshotList.EMPTY || onlinePlayersSnapshot != EMPTY_PERMISSION_SNAPSHOT) {
                onlinePlayers = PermissionSnapshotList.EMPTY;
                onlinePlayersSnapshot = EMPTY_PERMISSION_SNAPSHOT.clone();
            }
            onlinePlayersChannelVersion = channelSnapshotVersion;
            onlinePlayersPlatformVersion = HubPlatformServices.INSTANCE.getOnlinePlayersVersion();
        } else {
            long platformVersion = HubPlatformServices.INSTANCE.getOnlinePlayersVersion();
            if (force || onlinePlayersChannelVersion != channelSnapshotVersion || onlinePlayersPlatformVersion != platformVersion) {
                onlinePlayers = HubChannelManager.INSTANCE.getOnlinePlayerSnapshots(channelId);
                onlinePlayersSnapshot = onlinePlayers.toBytes();
                onlinePlayersChannelVersion = channelSnapshotVersion;
                onlinePlayersPlatformVersion = platformVersion;
            }
        }

        var grid = te.getNode().getGrid();
        UUID currentGridId = grid != null ? grid.getId() : null;
        long currentNodeSnapshotVersion = grid != null ? grid.getSnapshotVersion() : 0L;
        if (force || nodeSnapshotVersion != currentNodeSnapshotVersion || !Objects.equals(nodeGridId, currentGridId)) {
            nodes = NodeSnapshotList.fromGrid(grid);
            nodesSnapshot = nodes.toBytes();
            nodeSnapshotVersion = currentNodeSnapshotVersion;
            nodeGridId = currentGridId;
        }
    }

    private boolean syncChannelState() {
        UUID currentChannelId = te.getNode().getChannelId();
        if (Objects.equals(channelId, currentChannelId)) {
            return false;
        }
        channelId = currentChannelId;
        channelIdString = channelId.toString();
        channelName = te.getNode().getChannelName();
        return true;
    }

    @Override
    public void onUpdate(String field, Object oldValue, Object newValue) {
        if ("channelIdString".equals(field)) {
            channelId = UUID.fromString(newValue.toString());
        } else if ("chargingModeByte".equals(field)) {
            chargingMode.setPrefs((byte) newValue);
        } else if ("availableChannelsSnapshot".equals(field)) {
            availableChannels = PermissionSnapshotList.fromBytes((byte[]) newValue);
        } else if ("onlinePlayersSnapshot".equals(field)) {
            onlinePlayers = PermissionSnapshotList.fromBytes((byte[]) newValue);
        } else if ("nodesSnapshot".equals(field)) {
            nodes = NodeSnapshotList.fromBytes((byte[]) newValue);
        }
    }

    public boolean hasChannel() {
        return channelId != null && !channelId.equals(EMPTY);
    }
}