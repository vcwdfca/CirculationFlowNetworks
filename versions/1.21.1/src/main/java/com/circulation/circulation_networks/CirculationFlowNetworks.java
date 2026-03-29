package com.circulation.circulation_networks;

import com.circulation.circulation_networks.api.INodeBlockEntity;
import com.circulation.circulation_networks.events.BlockEntityLifeCycleEvent;
import com.circulation.circulation_networks.manager.BlockEntityLifecycleDispatcher;
import com.circulation.circulation_networks.manager.ChargingManager;
import com.circulation.circulation_networks.manager.EnergyMachineManager;
import com.circulation.circulation_networks.manager.EnergyTypeOverrideManager;
import com.circulation.circulation_networks.manager.HubChannelManager;
import com.circulation.circulation_networks.manager.MachineNodeBlockEntityManager;
import com.circulation.circulation_networks.manager.NetworkManager;
import com.circulation.circulation_networks.network.CFNNetwork;
import com.circulation.circulation_networks.packets.ConfigOverrideRendering;
import com.circulation.circulation_networks.packets.NodeNetworkRendering;
import com.circulation.circulation_networks.packets.RenderingClear;
import com.circulation.circulation_networks.utils.HubFTBServices;
import com.circulation.circulation_networks.utils.HubPlatformServices;
import com.circulation.circulation_networks.utils.Packet;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Mod(CirculationFlowNetworks.MOD_ID)
public final class CirculationFlowNetworks {

    public static final String MOD_ID = "circulation_networks";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static <T extends Packet<T>> void sendToPlayer(T packet, ServerPlayer player) {
        CFNNetwork.sendToPlayer(packet, player);
    }

    public static <T extends Packet<T>> void sendToServer(T packet) {
        CFNNetwork.sendToServer(packet);
    }

    public CirculationFlowNetworks(IEventBus modEventBus, ModContainer modContainer) {
        CFNConfig.register(modContainer);
        modEventBus.addListener(CFNConfig::onConfigLoad);
        modEventBus.addListener(CFNConfig::onConfigReload);
        modEventBus.addListener(this::onRegisterPayloadHandlers);
        if (FMLEnvironment.dist.isClient()) {
            CirculationFlowNetworksClient.init();
        }
        installPlatformServices();
        NeoForge.EVENT_BUS.addListener(this::onServerAboutToStart);
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onChunkLoad);
        NeoForge.EVENT_BUS.addListener(this::onLevelSave);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(this::onPlayerChangedDimension);
    }

    private void installPlatformServices() {
        HubPlatformServices.INSTANCE = new HubPlatformServices() {
            @Override
            public List<PlayerIdentity> getOnlinePlayers() {
                var server = ServerLifecycleHooks.getCurrentServer();
                if (server == null) {
                    return Collections.emptyList();
                }
                List<PlayerIdentity> players = new ObjectArrayList<>();
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    players.add(new PlayerIdentity(player.getUUID(), player.getGameProfile().getName()));
                }
                return players;
            }
        };

        if (ModList.get().isLoaded("ftbteams")) {
            HubFTBServices.INSTANCE = new HubFTBServices() {
                @Override
                protected boolean arePlayersInSameTeamInternal(UUID firstPlayerId, UUID secondPlayerId) {
                    var api = FTBTeamsAPI.api();
                    return api != null
                        && api.isManagerLoaded()
                        && api.getManager().arePlayersInSameTeam(firstPlayerId, secondPlayerId);
                }
            };
        }
    }

    private void onServerAboutToStart(ServerAboutToStartEvent event) {
        HubChannelManager.INSTANCE.load();
    }

    private void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        CFNNetwork.register(event);
    }

    private void onServerStarted(ServerStartedEvent event) {
        NetworkManager.INSTANCE.initGrid();
    }

    private void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide() || !(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            if (blockEntity instanceof INodeBlockEntity) {
                onBlockEntityValidate(level, blockEntity.getBlockPos(), blockEntity);
            }
        }
        NetworkManager.INSTANCE.validatePendingNodesInChunk(level, chunk.getPos().x, chunk.getPos().z);
    }

    private void onLevelSave(LevelEvent.Save event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide() || level.dimension() != Level.OVERWORLD) {
            return;
        }
        NetworkManager.INSTANCE.saveGrid();
        HubChannelManager.INSTANCE.save();
    }

    private void onServerStopping(ServerStoppingEvent event) {
        NetworkManager.INSTANCE.saveGrid();
        NetworkManager.INSTANCE.onServerStop();
        EnergyMachineManager.INSTANCE.onServerStop();
        EnergyTypeOverrideManager.onServerStop();
        ChargingManager.INSTANCE.onServerStop();
        HubChannelManager.INSTANCE.onServerStop();
        MachineNodeBlockEntityManager.INSTANCE.clear();
    }

    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        HubPlatformServices.INSTANCE.markOnlinePlayersDirty();
        if (event.getEntity() instanceof ServerPlayer player) {
            ConfigOverrideRendering.sendFullSync(player);
        }
    }

    private void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        HubPlatformServices.INSTANCE.markOnlinePlayersDirty();
        if (event.getEntity() instanceof ServerPlayer player) {
            NodeNetworkRendering.removePlayer(player);
        }
    }

    private void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            NodeNetworkRendering.removePlayer(player);
            ConfigOverrideRendering.sendFullSync(player);
            sendToPlayer(RenderingClear.INSTANCE, player);
        }
    }

    public static void onBlockEntityValidate(Level level, BlockPos pos, BlockEntity blockEntity) {
        BlockEntityLifecycleDispatcher.onValidate(new BlockEntityLifeCycleEvent.Validate(level, pos, blockEntity));
    }

    public static void onBlockEntityInvalidate(Level level, BlockPos pos, BlockEntity blockEntity) {
        BlockEntityLifecycleDispatcher.onInvalidate(new BlockEntityLifeCycleEvent.Invalidate(level, pos, blockEntity));
    }
}