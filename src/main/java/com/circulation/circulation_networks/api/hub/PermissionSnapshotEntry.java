package com.circulation.circulation_networks.api.hub;

import java.util.UUID;

public final class PermissionSnapshotEntry {

    private final UUID id;
    private final String name;
    private final HubPermissionLevel permission;

    public PermissionSnapshotEntry(UUID id, String name, HubPermissionLevel permission) {
        this.id = id;
        this.name = name;
        this.permission = permission;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public HubPermissionLevel getPermission() {
        return permission;
    }
}