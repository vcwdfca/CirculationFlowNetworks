package com.circulation.circulation_networks;

import com.circulation.circulation_networks.gui.GuiCirculationShielder;
import com.circulation.circulation_networks.gui.GuiHub;
import com.circulation.circulation_networks.gui.component.base.ComponentAtlas;
import com.circulation.circulation_networks.handlers.CirculationShielderRenderingHandler;
import com.circulation.circulation_networks.handlers.ConfigOverrideRenderingHandler;
import com.circulation.circulation_networks.handlers.EnergyWarningRenderingHandler;
import com.circulation.circulation_networks.handlers.ItemToolHandler;
import com.circulation.circulation_networks.handlers.NodeHighlightRenderingHandler;
import com.circulation.circulation_networks.handlers.NodeNetworkRenderingHandler;
import com.circulation.circulation_networks.handlers.PocketNodeRenderingHandler;
import com.circulation.circulation_networks.handlers.SpoceRenderingHandler;
import com.circulation.circulation_networks.handlers.SpoceRenderingHandlerGL46L3;
import com.circulation.circulation_networks.manager.MachineNodeBlockEntityManager;
import com.circulation.circulation_networks.registry.CFNMenuTypes;
import com.circulation.circulation_networks.utils.CI18n;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.opengl.GL11;

final class CirculationFlowNetworksClient {

    private static OpenGLLevel openGLLevel = OpenGLLevel.GL_1_1;
    private static String openGLVersionString = "unavailable";

    private CirculationFlowNetworksClient() {
    }

    static void init(IEventBus modEventBus) {
        java.io.File modConfigDir = new java.io.File(net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get().toFile(), CirculationFlowNetworks.MOD_ID);
        ComponentAtlas.INSTANCE.configure(modConfigDir);
        // Defer GL detection to the render thread — GL context is only current on the main thread
        Minecraft.getInstance().execute(() -> {
            registerAtlasReloadListener();
            openGLLevel = detectOpenGLLevel();
            SpoceRenderingHandler.INSTANCE = createSpoceHandler();
            MinecraftForge.EVENT_BUS.register(SpoceRenderingHandler.INSTANCE);
        });
        MinecraftForge.EVENT_BUS.register(NodeNetworkRenderingHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(EnergyWarningRenderingHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(ConfigOverrideRenderingHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(PocketNodeRenderingHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(NodeHighlightRenderingHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(ItemToolHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(CirculationShielderRenderingHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.addListener(CirculationFlowNetworksClient::onClientLoggingOut);

        modEventBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            MenuScreens.register(CFNMenuTypes.HUB_MENU, GuiHub::new);
            MenuScreens.register(CFNMenuTypes.CIRCULATION_SHIELDER_MENU, GuiCirculationShielder::new);
        }));

        CI18n.setI18nInternal(new CI18n() {
            @Override
            protected String formatInternal(String key, Object... params) {
                return I18n.get(key, params);
            }

            @Override
            protected boolean hasKeyInternal(String key) {
                return I18n.exists(key);
            }
        });
    }

    private static void registerAtlasReloadListener() {
        var resourceManager = Minecraft.getInstance().getResourceManager();
        if (resourceManager instanceof ReloadableResourceManager reloadable) {
            reloadable.registerReloadListener((ResourceManagerReloadListener) ignored -> ComponentAtlas.INSTANCE.restart());
        }
        ComponentAtlas.INSTANCE.restart();
    }

    private static OpenGLLevel detectOpenGLLevel() {
        String versionStr;
        try {
            versionStr = GL11.glGetString(GL11.GL_VERSION);
        } catch (Throwable throwable) {
            openGLVersionString = "unavailable";
            CirculationFlowNetworks.LOGGER.warn("Failed to obtain OpenGL version", throwable);
            return OpenGLLevel.GL_1_1;
        }

        if (versionStr == null) {
            openGLVersionString = "unavailable";
            CirculationFlowNetworks.LOGGER.warn("Failed to obtain OpenGL version");
            return OpenGLLevel.GL_1_1;
        }
        openGLVersionString = versionStr;

        try {
            String[] parts = versionStr.split("[. ]");
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            if (major > 4 || (major == 4 && minor >= 6)) {
                return OpenGLLevel.GL_4_6;
            }
            if (major > 3 || (major == 3 && minor >= 2)) {
                return OpenGLLevel.GL_3_2_PLUS;
            }
        } catch (Exception e) {
            CirculationFlowNetworks.LOGGER.warn("Failed to parse OpenGL version: {}", versionStr, e);
        }

        return OpenGLLevel.GL_1_1;
    }

    private static SpoceRenderingHandler ensureSupportedOpenGL() {
        String message = "OpenGL version too low for Circulation Flow Networks. Detected: "
            + openGLVersionString + ". Minimum required: 3.2.";
        CirculationFlowNetworks.LOGGER.error(message);
        throw new IllegalStateException(message);
    }

    private static SpoceRenderingHandler createSpoceHandler() {
        return switch (openGLLevel) {
            case GL_4_6 -> new SpoceRenderingHandlerGL46L3();
            case GL_3_2_PLUS -> new SpoceRenderingHandler();
            default -> ensureSupportedOpenGL();
        };
    }

    private static void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        Minecraft.getInstance().execute(() -> {
            MachineNodeBlockEntityManager.INSTANCE.clear();
            NodeNetworkRenderingHandler.INSTANCE.clearLinks();
            EnergyWarningRenderingHandler.INSTANCE.clear();
            ConfigOverrideRenderingHandler.INSTANCE.clear();
            PocketNodeRenderingHandler.INSTANCE.clear();
            NodeHighlightRenderingHandler.INSTANCE.clear();
            CirculationShielderRenderingHandler.INSTANCE.clear();
            if (SpoceRenderingHandler.INSTANCE != null) {
                SpoceRenderingHandler.INSTANCE.clear();
            }
        });
    }

    private enum OpenGLLevel {
        GL_1_1,
        GL_3_2_PLUS,
        GL_4_6
    }
}
