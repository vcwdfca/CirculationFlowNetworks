package com.circulation.circulation_networks.utils;

import java.util.UUID;

public abstract class HubFTBServices {

    public static HubFTBServices INSTANCE;

    public static boolean isLoaded() {
        return INSTANCE != null;
    }

    public static boolean arePlayersInSameTeam(UUID firstPlayerId, UUID secondPlayerId) {
        return INSTANCE != null && INSTANCE.arePlayersInSameTeamInternal(firstPlayerId, secondPlayerId);
    }

    protected abstract boolean arePlayersInSameTeamInternal(UUID firstPlayerId, UUID secondPlayerId);
}