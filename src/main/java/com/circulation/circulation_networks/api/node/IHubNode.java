package com.circulation.circulation_networks.api.node;

import com.circulation.circulation_networks.api.hub.ChargingDefinition;
import com.circulation.circulation_networks.api.hub.ChargingPreference;
import com.circulation.circulation_networks.api.hub.HubPermissionLevel;
import com.circulation.circulation_networks.api.hub.PermissionMode;
//? if <1.21 {
import net.minecraftforge.items.IItemHandler;
//?} else {
/*import net.neoforged.neoforge.items.IItemHandler;
*///?}

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

/**
 * 中枢节点接口，一个网络中只能存在一个中枢节点。
 * 中枢节点同时具有能量供应和玩家充能能力。
 * 实际上是唯一单例而不是API接口
 * <p>
 * Hub node interface. Only one hub node can exist per network.
 * Hub nodes have both energy supply and player charging capabilities.
 */
@SuppressWarnings("unused")
public interface IHubNode extends IEnergySupplyNode, IChargingNode {

    int PLUGIN_SLOTS = 5;

    PermissionMode getPermissionMode();

    void setPermissionMode(PermissionMode mode);

    IItemHandler getPlugins();

    @Nonnull
    UUID getChannelId();

    void setChannelId(@Nonnull UUID channelId);

    @Nonnull
    String getChannelName();

    void setChannelName(@Nonnull String channelName);

    @Nonnull
    ChargingPreference getChargingPreference(UUID playerId);

    void setChargingPreference(UUID playerId, ChargingPreference preference);

    boolean getChargingState(UUID playerId, ChargingDefinition preference);

    void setChargingState(UUID playerId, ChargingDefinition preference, boolean value);

    @Nullable
    UUID getOwner();

    void setOwner(@Nullable UUID owner);

    @Nullable
    HubPermissionLevel getExplicitPermission(UUID playerId);

    Map<UUID, HubPermissionLevel> getExplicitPermissions();

    void setExplicitPermission(UUID playerId, HubPermissionLevel permissionLevel);

    void removeExplicitPermission(UUID playerId);

    HubPermissionLevel getPermissionLevel(UUID playerId);

    boolean canEditPermissions(UUID playerId);
}
