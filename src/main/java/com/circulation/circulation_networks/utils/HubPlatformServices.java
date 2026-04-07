package com.circulation.circulation_networks.utils;

//? if <1.20 {

import com.github.bsideup.jabel.Desugar;
import net.minecraft.entity.player.EntityPlayer;
//?}
//? if >=1.20 {
/*import net.minecraft.world.entity.player.Player;
 *///?}

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public abstract class HubPlatformServices {

    public static HubPlatformServices INSTANCE = new HubPlatformServices() {
        @Override
        public List<PlayerIdentity> getOnlinePlayers() {
            return Collections.emptyList();
        }
    };
    private long onlinePlayersVersion = 1L;

    public abstract List<PlayerIdentity> getOnlinePlayers();

    public boolean hasChannelManagementOverride(
        //? if <1.20 {
        EntityPlayer player
        //?} else {
        /*Player player
        *///?}
    ) {
        return false;
    }

    public long getOnlinePlayersVersion() {
        return onlinePlayersVersion;
    }

    public void markOnlinePlayersDirty() {
        onlinePlayersVersion++;
    }

    //? if <1.20
    @Desugar
    public record PlayerIdentity(UUID id, String name) {

    }
}
