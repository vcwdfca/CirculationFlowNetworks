package com.circulation.circulation_networks;

import com.circulation.circulation_networks.handlers.ConfigOverrideRenderingHandler;
import com.circulation.circulation_networks.handlers.ItemToolHandler;
import com.circulation.circulation_networks.handlers.NodeNetworkRenderingHandler;
import com.circulation.circulation_networks.handlers.SpoceRenderingHandler;
import com.circulation.circulation_networks.handlers.SpoceRenderingHandlerGL32L3;
import com.circulation.circulation_networks.handlers.SpoceRenderingHandlerGL46L3;
import com.circulation.circulation_networks.manager.MachineNodeBlockEntityManager;
import com.circulation.circulation_networks.utils.CI18n;
import net.minecraft.client.resources.language.I18n;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.opengl.GL11;

final class CirculationFlowNetworksClient {

    private static OpenGLLevel openGLLevel = OpenGLLevel.GL_1_1;

    private CirculationFlowNetworksClient() {
    }

    static void init() {
        openGLLevel = detectOpenGLLevel();
        SpoceRenderingHandler.INSTANCE = createSpoceHandler();
        NeoForge.EVENT_BUS.register(SpoceRenderingHandler.INSTANCE);
        NeoForge.EVENT_BUS.register(NodeNetworkRenderingHandler.INSTANCE);
        NeoForge.EVENT_BUS.register(ConfigOverrideRenderingHandler.INSTANCE);
        NeoForge.EVENT_BUS.register(ItemToolHandler.INSTANCE);
        NeoForge.EVENT_BUS.addListener(CirculationFlowNetworksClient::onClientLoggingOut);

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

    private static OpenGLLevel detectOpenGLLevel() {
        String versionStr;
        try {
            versionStr = GL11.glGetString(GL11.GL_VERSION);
        } catch (Throwable throwable) {
            CirculationFlowNetworks.LOGGER.warn("Failed to obtain OpenGL version, falling back to base Spoce renderer", throwable);
            return OpenGLLevel.GL_1_1;
        }

        if (versionStr == null) {
            CirculationFlowNetworks.LOGGER.warn("Failed to obtain OpenGL version, falling back to base Spoce renderer");
            return OpenGLLevel.GL_1_1;
        }

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

    private static SpoceRenderingHandler createSpoceHandler() {
        return switch (openGLLevel) {
            case GL_4_6 -> new SpoceRenderingHandlerGL46L3();
            case GL_3_2_PLUS -> new SpoceRenderingHandlerGL32L3();
            default -> new SpoceRenderingHandler();
        };
    }

    private static void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        MachineNodeBlockEntityManager.INSTANCE.clear();
        NodeNetworkRenderingHandler.INSTANCE.clearLinks();
        ConfigOverrideRenderingHandler.INSTANCE.clear();
        if (SpoceRenderingHandler.INSTANCE != null) {
            SpoceRenderingHandler.INSTANCE.clear();
        }
    }

    private enum OpenGLLevel {
        GL_1_1,
        GL_3_2_PLUS,
        GL_4_6
    }
}