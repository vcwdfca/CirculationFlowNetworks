package com.circulation.circulation_networks.proxy;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import com.circulation.circulation_networks.energy.manager.CEHandlerManager;
import com.circulation.circulation_networks.energy.manager.EUHandlerManager;
import com.circulation.circulation_networks.energy.manager.FEHandlerManager;
import com.circulation.circulation_networks.energy.manager.MEKHandlerManager;
import com.circulation.circulation_networks.events.BlockEntityLifeCycleEvent;
import com.circulation.circulation_networks.manager.EnergyMachineManager;
import com.circulation.circulation_networks.manager.EnergyTypeOverrideManager;
import com.circulation.circulation_networks.manager.HubChannelManager;
import com.circulation.circulation_networks.manager.MachineNodeBlockEntityManager;
import com.circulation.circulation_networks.manager.NetworkManager;
import com.circulation.circulation_networks.packets.ConfigOverrideRendering;
import com.circulation.circulation_networks.packets.ContainerProgressBar;
import com.circulation.circulation_networks.packets.ContainerValueConfig;
import com.circulation.circulation_networks.packets.NodeNetworkRendering;
import com.circulation.circulation_networks.packets.PhaseInterrupterSyncPacket;
import com.circulation.circulation_networks.packets.RenderingClear;
import com.circulation.circulation_networks.packets.SpoceRendering;
import com.circulation.circulation_networks.packets.UpdateItemModeMessage;
import com.circulation.circulation_networks.packets.UpdatePlayerChargingMode;
import com.circulation.circulation_networks.registry.RegistryBlocks;
import com.circulation.circulation_networks.registry.RegistryEnergyHandler;
import com.circulation.circulation_networks.registry.RegistryItems;
import com.circulation.circulation_networks.tiles.BaseTileEntity;
import com.circulation.circulation_networks.utils.Packet;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.jetbrains.annotations.Nullable;

import static com.circulation.circulation_networks.CirculationFlowNetworks.NET_CHANNEL;

@SuppressWarnings("unused")
public class CommonProxy implements IGuiHandler {

    private int id = 0;

    public CommonProxy() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void init() {
        RegistryEnergyHandler.registerEnergyHandler(new CEHandlerManager());
        RegistryEnergyHandler.registerEnergyHandler(new FEHandlerManager());
        if (Loader.isModLoaded("mekanism"))
            RegistryEnergyHandler.registerEnergyHandler(new MEKHandlerManager());
        if (Loader.isModLoaded("ic2")) {
            RegistryEnergyHandler.registerEnergyHandler(EUHandlerManager.INSTANCE);
            MinecraftForge.EVENT_BUS.register(EUHandlerManager.INSTANCE);
        }
    }

    public void postInit() {
        RegistryEnergyHandler.lock();
    }

    public <T extends Packet<T>> void registerMessage(Class<T> aClass, Side side) {
        NET_CHANNEL.registerMessage(aClass, aClass, id++, side);
    }

    public void preInit(FMLPreInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(CirculationFlowNetworks.instance, this);
        registerMessage(PhaseInterrupterSyncPacket.class, Side.SERVER);
        registerMessage(UpdateItemModeMessage.class, Side.SERVER);
        registerMessage(ContainerProgressBar.class, Side.SERVER);
        registerMessage(UpdatePlayerChargingMode.class, Side.SERVER);

        registerMessage(SpoceRendering.class, Side.CLIENT);
        registerMessage(NodeNetworkRendering.class, Side.CLIENT);
        registerMessage(ConfigOverrideRendering.class, Side.CLIENT);
        registerMessage(ContainerProgressBar.class, Side.CLIENT);
        registerMessage(ContainerValueConfig.class, Side.CLIENT);
        registerMessage(RenderingClear.INSTANCE, Side.CLIENT);
    }

    @SubscribeEvent
    public void registryItem(RegistryEvent.Register<Item> event) {
        RegistryItems.registerItems(event);
    }

    @SubscribeEvent
    public void registryBlock(RegistryEvent.Register<Block> event) {
        RegistryBlocks.registerBlocks(event);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            EnergyMachineManager.INSTANCE.onServerTick();
        } else {
            MachineNodeBlockEntityManager.INSTANCE.onServerTick();
        }
    }

    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save event) {
        if (!event.getWorld().isRemote && event.getWorld().provider.getDimension() == 0) {
            NetworkManager.INSTANCE.saveGrid();
            EnergyTypeOverrideManager.save();
        }
    }

    @SubscribeEvent
    public void onBlockEntityValidate(BlockEntityLifeCycleEvent.Validate event) {
        MachineNodeBlockEntityManager.INSTANCE.onBlockEntityValidate(event);
        NetworkManager.INSTANCE.onBlockEntityValidate(event);
        EnergyMachineManager.INSTANCE.onBlockEntityValidate(event);
        HubChannelManager.INSTANCE.onBlockEntityValidate(event);
    }

    public <T extends Packet<T>> void registerMessage(T aClass, Side side) {
        //noinspection unchecked
        NET_CHANNEL.registerMessage(aClass, (Class<T>) aClass.getClass(), id++, side);
    }

    @SubscribeEvent
    public void onBlockEntityInvalidate(BlockEntityLifeCycleEvent.Invalidate event) {
        MachineNodeBlockEntityManager.INSTANCE.onBlockEntityInvalidate(event);
        NetworkManager.INSTANCE.onBlockEntityInvalidate(event);
        EnergyMachineManager.INSTANCE.onBlockEntityInvalidate(event);
        HubChannelManager.INSTANCE.onBlockEntityInvalidate(event);
        var overrideManager = EnergyTypeOverrideManager.get();
        if (overrideManager != null) overrideManager.onBlockEntityInvalidate(event);
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (!event.getWorld().isRemote && event.getWorld().provider.getDimension() == 0) {
            EnergyTypeOverrideManager.get();
        }
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.player instanceof EntityPlayerMP player) {
            NET_CHANNEL.sendTo(new ConfigOverrideRendering(player.dimension), player);
            NET_CHANNEL.sendTo(RenderingClear.INSTANCE, player);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP player) {
            NET_CHANNEL.sendTo(new ConfigOverrideRendering(player.dimension), player);
        }
    }

    @Override
    public @Nullable Container getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        var tile = world.getTileEntity(new BlockPos(x, y, z));
        if (tile == null) {
            return null;
        } else if (tile instanceof BaseTileEntity te && te.hasGui()) {
            return te.getContainer(player);
        }
        return null;
    }

    @Override
    public @Nullable Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }
}