package com.circulation.circulation_networks.api.hub;

import com.circulation.circulation_networks.api.IGrid;
import it.unimi.dsi.fastutil.objects.ReferenceSet;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

public interface IHubChannel {

    UUID getChannelId();

    ReferenceSet<IGrid> getGrids();

    String getName();

    void setName(String name);

    PermissionMode getPermissionMode();

    void setPermissionMode(PermissionMode mode);

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
