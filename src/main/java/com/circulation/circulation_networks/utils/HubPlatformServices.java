package com.circulation.circulation_networks.utils;

//? if <1.20 {
import com.github.bsideup.jabel.Desugar;
//?}

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public abstract class HubPlatformServices {

    private long onlinePlayersVersion = 1L;

    public static HubPlatformServices INSTANCE = new HubPlatformServices() {
        @Override
        public List<PlayerIdentity> getOnlinePlayers() {
            return Collections.emptyList();
        }
    };

    public abstract List<PlayerIdentity> getOnlinePlayers();

    public long getOnlinePlayersVersion() {
        return onlinePlayersVersion;
    }

    public void markOnlinePlayersDirty() {
        onlinePlayersVersion++;
    }

    //? if <1.20 {
    @Desugar
    //?}
    public record PlayerIdentity(UUID id, String name) {

    }
}