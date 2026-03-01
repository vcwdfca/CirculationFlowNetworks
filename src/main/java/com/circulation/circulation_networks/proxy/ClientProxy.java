package com.circulation.circulation_networks.proxy;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import com.circulation.circulation_networks.handlers.InspectionToolHandler;
import com.circulation.circulation_networks.handlers.NodeNetworkRenderingHandler;
import com.circulation.circulation_networks.handlers.SpoceRenderingHandler;
import com.circulation.circulation_networks.handlers.SpoceRenderingHandlerGL32L2;
import com.circulation.circulation_networks.handlers.SpoceRenderingHandlerGL32L3;
import com.circulation.circulation_networks.handlers.SpoceRenderingHandlerGL46L2;
import com.circulation.circulation_networks.handlers.SpoceRenderingHandlerGL46L3;
import com.circulation.circulation_networks.manager.MachineNodeTEManager;
import com.circulation.circulation_networks.registry.RegistryBlocks;
import com.circulation.circulation_networks.registry.RegistryItems;
import com.circulation.circulation_networks.tiles.BaseTileEntity;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    public static OpenGLLevel openGLLevel = OpenGLLevel.GL_1_1;
    public static boolean isLWJGL3 = false;

    static {
        try {
            Class.forName("org.lwjgl.system.MemoryStack");
            isLWJGL3 = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    public static OpenGLLevel detectOpenGLLevel() {
        String versionStr = GL11.glGetString(GL11.GL_VERSION);
        if (versionStr == null) {
            CirculationFlowNetworks.LOGGER.warn("Failed to obtain OpenGL version, defaulting to GL_1_1");
            return OpenGLLevel.GL_1_1;
        }
        try {
            String[] parts = versionStr.split("[. ]");
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            if (major > 4 || (major == 4 && minor >= 6)) {
                return OpenGLLevel.GL_4_6;
            } else if (major > 3 || (major == 3 && minor >= 2)) {
                return OpenGLLevel.GL_3_2_PLUS;
            } else {
                return OpenGLLevel.GL_1_1;
            }
        } catch (Exception e) {
            CirculationFlowNetworks.LOGGER.warn("Failed to parse OpenGL version: {}", versionStr);
            return OpenGLLevel.GL_1_1;
        }
    }

    private static SpoceRenderingHandler createSpoceHandler() {
        return switch (openGLLevel) {
            case GL_4_6 -> isLWJGL3 ? new SpoceRenderingHandlerGL46L3() : new SpoceRenderingHandlerGL46L2();
            case GL_3_2_PLUS -> isLWJGL3 ? new SpoceRenderingHandlerGL32L3() : new SpoceRenderingHandlerGL32L2();
            default -> new SpoceRenderingHandler();
        };
    }

    public void preInit() {
        super.preInit();
    }

    public void init() {
        super.init();
        openGLLevel = detectOpenGLLevel();
        SpoceRenderingHandler.INSTANCE = createSpoceHandler();
    }

    public void postInit() {
        super.postInit();
        MinecraftForge.EVENT_BUS.register(SpoceRenderingHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(NodeNetworkRenderingHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(InspectionToolHandler.INSTANCE);
    }

    @SubscribeEvent
    public void onModelRegister(ModelRegistryEvent event) {
        RegistryBlocks.registerBlockModels();
        RegistryItems.registerItemModels();
    }

    @Override
    public @Nullable GuiContainer getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        var tile = world.getTileEntity(new BlockPos(x, y, z));
        if (tile == null) {
            return null;
        } else if (tile instanceof BaseTileEntity te && te.hasGui()) {
            return te.getGui(player);
        }
        return null;
    }

    @SubscribeEvent
    public void onClientStop(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        MachineNodeTEManager.INSTANCE.clear();
        NodeNetworkRenderingHandler.INSTANCE.clearLinks();
        SpoceRenderingHandler.INSTANCE.clear();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {

        } else {
            MachineNodeTEManager.INSTANCE.onClientTick();
        }
    }

    public enum OpenGLLevel {
        GL_1_1, GL_3_2_PLUS, GL_4_6
    }
}
