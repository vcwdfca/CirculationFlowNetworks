package com.circulation.circulation_networks.api.hub;

import javax.annotation.Nullable;

public final class NodeSnapshotEntry {

    private final String blockId;
    private final int x;
    private final int y;
    private final int z;
    @Nullable
    private final String customName;

    public NodeSnapshotEntry(String blockId, int x, int y, int z, @Nullable String customName) {
        this.blockId = blockId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.customName = customName;
    }

    public String getBlockId() {
        return blockId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Nullable
    public String getCustomName() {
        return customName;
    }
}