package com.circulation.circulation_networks.api.hub;

public enum HubPermissionLevel {
    NONE,
    MEMBER,
    ADMIN,
    OWNER;

    public static HubPermissionLevel fromId(int id) {
        return HubPermissionLevel.values()[Math.floorMod(id, HubPermissionLevel.values().length)];
    }

    public int getId() {
        return ordinal();
    }

    public boolean canEditPermissions() {
        return this == OWNER || this == ADMIN;
    }
}