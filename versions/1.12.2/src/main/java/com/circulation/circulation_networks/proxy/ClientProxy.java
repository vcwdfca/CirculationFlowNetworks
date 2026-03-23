package com.circulation.circulation_networks.proxy;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import com.circulation.circulation_networks.gui.component.base.ComponentAtlas;
import com.circulation.circulation_networks.handlers.ConfigOverrideRenderingHandler;
import com.circulation.circulation_networks.handlers.ItemToolHandler;
import com.circulation.circulation_networks.handlers.NodeNetworkRenderingHandler;
import com.circulation.circulation_networks.handlers.PhaseInterrupterRenderingHandler;
import com.circulation.circulation_networks.handlers.SpoceRenderingHandler;
import com.circulation.circulation_networks.handlers.SpoceRenderingHandlerGL32L2;
import com.circulation.circulation_networks.handlers.SpoceRenderingHandlerGL32L3;
import com.circulation.circulation_networks.handlers.SpoceRenderingHandlerGL46L2;
import com.circulation.circulation_networks.handlers.SpoceRenderingHandlerGL46L3;
import com.circulation.circulation_networks.manager.MachineNodeBlockEntityManager;
import com.circulation.circulation_networks.registry.RegistryBlocks;
import com.circulation.circulation_networks.registry.RegistryItems;
import com.circulation.circulation_networks.tiles.BaseTileEntity;
import com.circulation.circulation_networks.utils.CI18n;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.io.File;

@SideOnly(Side.CLIENT)
public final class ClientProxy extends CommonProxy {

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

    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        MinecraftForge.EVENT_BUS.register(ComponentAtlas.INSTANCE);
        CI18n.INSTANCE = new CI18n() {
            @Override
            public String format(String key, Object... params) {
                return I18n.format(key, params);
            }

            @Override
            public boolean hasKey(String key) {
                return I18n.hasKey(key);
            }
        };
    }

    public void init() {
        super.init();
        File modConfigDir = new File(Loader.instance().getConfigDir(), CirculationFlowNetworks.MOD_ID);
        ComponentAtlas.INSTANCE.startAsync(modConfigDir);
        openGLLevel = detectOpenGLLevel();
        SpoceRenderingHandler.INSTANCE = createSpoceHandler();
    }

    public void postInit() {
        super.postInit();
        MinecraftForge.EVENT_BUS.register(SpoceRenderingHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(NodeNetworkRenderingHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(ConfigOverrideRenderingHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(ItemToolHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(PhaseInterrupterRenderingHandler.INSTANCE);
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
    public void onTextureReloadPre(TextureStitchEvent.Pre event) {
        ComponentAtlas.INSTANCE.dispose();
    }

    @SubscribeEvent
    public void onTextureReloadPost(TextureStitchEvent.Post event) {
        ComponentAtlas.INSTANCE.restart();
    }

    @SubscribeEvent
    public void onClientStop(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        MachineNodeBlockEntityManager.INSTANCE.clear();
        NodeNetworkRenderingHandler.INSTANCE.clearLinks();
        ConfigOverrideRenderingHandler.INSTANCE.clear();
        SpoceRenderingHandler.INSTANCE.clear();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            MachineNodeBlockEntityManager.INSTANCE.onClientTick();
        }
    }

    public enum OpenGLLevel {
        GL_1_1, GL_3_2_PLUS, GL_4_6
    }
}
