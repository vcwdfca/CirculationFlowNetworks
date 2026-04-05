package com.circulation.circulation_networks;

import com.circulation.circulation_networks.api.ICirculationShielderBlockEntity;
import com.circulation.circulation_networks.api.INodeBlockEntity;
import com.circulation.circulation_networks.energy.manager.FEHandlerManager;
import com.circulation.circulation_networks.energy.manager.MEKHandlerManager;
import com.circulation.circulation_networks.events.BlockEntityLifeCycleEvent;
import com.circulation.circulation_networks.manager.BlockEntityLifecycleDispatcher;
import com.circulation.circulation_networks.manager.ChargingManager;
import com.circulation.circulation_networks.manager.EnergyMachineManager;
import com.circulation.circulation_networks.manager.EnergyTypeOverrideManager;
import com.circulation.circulation_networks.manager.HubChannelManager;
import com.circulation.circulation_networks.manager.MachineNodeBlockEntityManager;
import com.circulation.circulation_networks.manager.NetworkManager;
import com.circulation.circulation_networks.manager.PocketNodeManager;
import com.circulation.circulation_networks.network.CFNNetwork;
import com.circulation.circulation_networks.packets.ConfigOverrideRendering;
import com.circulation.circulation_networks.packets.NodeNetworkRendering;
import com.circulation.circulation_networks.packets.PocketNodeRendering;
import com.circulation.circulation_networks.packets.RenderingClear;
import com.circulation.circulation_networks.registry.RegistryBlocks;
import com.circulation.circulation_networks.registry.RegistryEnergyHandler;
import com.circulation.circulation_networks.registry.RegistryItems;
import com.circulation.circulation_networks.utils.HubPlatformServices;
import com.circulation.circulation_networks.utils.HubTeamServices;
import com.circulation.circulation_networks.utils.Packet;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Mod(CirculationFlowNetworks.MOD_ID)
public final class CirculationFlowNetworks {

    public static final String MOD_ID = "circulation_networks";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public CirculationFlowNetworks() {
        CFNNetwork.register();
        CFNConfig.register();
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        RegistryItems.register(modEventBus);
        RegistryBlocks.register(modEventBus);
        registerEnergyHandlers();
        modEventBus.addListener(CFNConfig::onConfigLoad);
        modEventBus.addListener(CFNConfig::onConfigReload);
        modEventBus.addListener(this::onLoadComplete);
        if (FMLEnvironment.dist.isClient()) {
            CirculationFlowNetworksClient.init();
        }
        installPlatformServices();
        MinecraftForge.EVENT_BUS.addListener(this::onServerAboutToStart);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarted);
        MinecraftForge.EVENT_BUS.addListener(this::onChunkLoad);
        MinecraftForge.EVENT_BUS.addListener(this::onBlockBreak);
        MinecraftForge.EVENT_BUS.addListener(this::onLevelSave);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStopping);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLoggedOut);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerChangedDimension);
        MinecraftForge.EVENT_BUS.addListener(this::onServerTick);
    }

    public static <T extends Packet<T>> void sendToPlayer(T packet, ServerPlayer player) {
        CFNNetwork.sendToPlayer(packet, player);
    }

    public static <T extends Packet<T>> void sendToServer(T packet) {
        CFNNetwork.sendToServer(packet);
    }

    public static void onBlockEntityValidate(Level level, BlockPos pos, BlockEntity blockEntity) {
        var event = new BlockEntityLifeCycleEvent.Validate(level, pos, blockEntity);
        BlockEntityLifecycleDispatcher.onValidate(event);
        MinecraftForge.EVENT_BUS.post(event);
    }

    public static void onBlockEntityInvalidate(Level level, BlockPos pos, BlockEntity blockEntity) {
        var event = new BlockEntityLifeCycleEvent.Invalidate(level, pos, blockEntity);
        BlockEntityLifecycleDispatcher.onInvalidate(event);
        MinecraftForge.EVENT_BUS.post(event);
    }

    private void registerEnergyHandlers() {
        RegistryEnergyHandler.registerEnergyHandler(new FEHandlerManager());
        if (ModList.get().isLoaded("mekanism")) {
            RegistryEnergyHandler.registerEnergyHandler(new MEKHandlerManager());
        }
    }

    private void onLoadComplete(FMLLoadCompleteEvent event) {
        RegistryEnergyHandler.lock();
    }

    private void installPlatformServices() {
        HubPlatformServices.INSTANCE = new HubPlatformServices() {
            @Override
            public List<PlayerIdentity> getOnlinePlayers() {
                var server = ServerLifecycleHooks.getCurrentServer();
                if (server == null) {
                    return Collections.emptyList();
                }
                List<PlayerIdentity> players = new ArrayList<>();
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    players.add(new PlayerIdentity(player.getUUID(), player.getGameProfile().getName()));
                }
                return players;
            }
        };

        if (ModList.get().isLoaded("ftbteams")) {
            HubTeamServices.INSTANCE = new HubTeamServices() {
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

    private void onServerStarted(ServerStartedEvent event) {
        NetworkManager.INSTANCE.initGrid();
        PocketNodeManager.INSTANCE.load();
    }

    private void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            EnergyMachineManager.INSTANCE.onServerTick();
        } else {
            MachineNodeBlockEntityManager.INSTANCE.onServerTick();
        }
    }

    private void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide() || !(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
            if (blockEntity instanceof INodeBlockEntity || blockEntity instanceof ICirculationShielderBlockEntity) {
                onBlockEntityValidate(level, blockEntity.getBlockPos(), blockEntity);
            }
        }
        NetworkManager.INSTANCE.validatePendingNodesInChunk(level, chunk.getPos().x, chunk.getPos().z);
        PocketNodeManager.INSTANCE.onChunkLoad(level, chunk.getPos().x, chunk.getPos().z);
    }

    private void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            PocketNodeManager.INSTANCE.onHostBlockBroken(level, event.getPos());
        }
    }

    private void onLevelSave(LevelEvent.Save event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide() || level.dimension() != Level.OVERWORLD) {
            return;
        }
        NetworkManager.INSTANCE.saveGrid();
        PocketNodeManager.INSTANCE.save();
        HubChannelManager.INSTANCE.save();
    }

    private void onServerStopping(ServerStoppingEvent event) {
        NetworkManager.INSTANCE.saveGrid();
        PocketNodeManager.INSTANCE.save();
        NetworkManager.INSTANCE.onServerStop();
        PocketNodeManager.INSTANCE.onServerStop();
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
            sendToPlayer(new PocketNodeRendering(player), player);
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
            sendToPlayer(new PocketNodeRendering(player), player);
        }
    }
}
