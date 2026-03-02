package com.circulation.circulation_networks.handlers;

import com.circulation.circulation_networks.tiles.TileEntityPhaseInterrupter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.WeakHashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public final class PhaseInterrupterRenderingHandler {

    public static final PhaseInterrupterRenderingHandler INSTANCE = new PhaseInterrupterRenderingHandler();

    private static final float ORANGE_R = 1.0f;
    private static final float ORANGE_G = 0.647f;
    private static final float ORANGE_B = 0.0f;
    private static final float ALPHA = 0.5f;
    private static final float RANGE_EXPANSION = 0.01f;
    private static final float ANIMATION_DURATION = 2.0f;

    private final Map<TileEntityPhaseInterrupter, Float> animProgress = new WeakHashMap<>();

    private static float easeOutCubic(float t) {
        t = Math.min(t, 1.0f);
        return (float) (1.0 - Math.pow(1.0 - t, 3.0));
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        animProgress.forEach((tile, progress) -> {
            if (tile.isShowingRange()) {
                float newProgress = Math.min(progress + 1.0f / (ANIMATION_DURATION * 20.0f), 1.0f);
                animProgress.put(tile, newProgress);
            } else {
                animProgress.put(tile, 0.0f);
            }
        });
    }

    private void renderInterrupterRange(TileEntityPhaseInterrupter interrupter, double playerX, double playerY, double playerZ) {
        GlStateManager.pushMatrix();

        GlStateManager.translate(-playerX, -playerY, -playerZ);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        
        GlStateManager.disableCull();
        
        GlStateManager.enableDepth();
        GlStateManager.depthMask(false);

        int scope = interrupter.getScope();
        double x = interrupter.getPos().getX();
        double y = interrupter.getPos().getY();
        double z = interrupter.getPos().getZ();

        float progress = animProgress.getOrDefault(interrupter, 0.0f);
        float easedProgress = easeOutCubic(progress);
        float expandedScope = scope * easedProgress + RANGE_EXPANSION;

        drawAABBFaces(
            x - expandedScope, y - expandedScope, z - expandedScope,
            x + expandedScope + 1, y + expandedScope + 1, z + expandedScope + 1,
            ORANGE_R, ORANGE_G, ORANGE_B, ALPHA
        );

        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    private static void drawAABBFaces(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, float r, float g, float b, float alpha) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        GlStateManager.color(r, g, b, alpha);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        buf.pos(minX, minY, minZ).endVertex();
        buf.pos(maxX, minY, minZ).endVertex();
        buf.pos(maxX, minY, maxZ).endVertex();
        buf.pos(minX, minY, maxZ).endVertex();

        buf.pos(minX, maxY, minZ).endVertex();
        buf.pos(minX, maxY, maxZ).endVertex();
        buf.pos(maxX, maxY, maxZ).endVertex();
        buf.pos(maxX, maxY, minZ).endVertex();

        buf.pos(minX, minY, minZ).endVertex();
        buf.pos(minX, maxY, minZ).endVertex();
        buf.pos(maxX, maxY, minZ).endVertex();
        buf.pos(maxX, minY, minZ).endVertex();

        buf.pos(minX, minY, maxZ).endVertex();
        buf.pos(maxX, minY, maxZ).endVertex();
        buf.pos(maxX, maxY, maxZ).endVertex();
        buf.pos(minX, maxY, maxZ).endVertex();

        buf.pos(minX, minY, minZ).endVertex();
        buf.pos(minX, minY, maxZ).endVertex();
        buf.pos(minX, maxY, maxZ).endVertex();
        buf.pos(minX, maxY, minZ).endVertex();

        buf.pos(maxX, minY, minZ).endVertex();
        buf.pos(maxX, maxY, minZ).endVertex();
        buf.pos(maxX, maxY, maxZ).endVertex();
        buf.pos(maxX, minY, maxZ).endVertex();

        tess.draw();
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || mc.player == null) return;

        EntityPlayerSP player = mc.player;
        double playerX = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getPartialTicks();
        double playerY = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getPartialTicks();
        double playerZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getPartialTicks();

        for (TileEntity te : mc.world.loadedTileEntityList) {
            if (te instanceof TileEntityPhaseInterrupter interrupter) {
                if (interrupter.isShowingRange()) {
                    animProgress.putIfAbsent(interrupter, 0.0f);
                    renderInterrupterRange(interrupter, playerX, playerY, playerZ);
                }
            }
        }
    }
}
