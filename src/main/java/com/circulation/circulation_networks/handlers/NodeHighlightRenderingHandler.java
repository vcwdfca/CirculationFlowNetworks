package com.circulation.circulation_networks.handlers;

//~ mc_imports
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
//? if <1.20 {
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
//?} else if <1.21 {
/*import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
*///?} else {
/*import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
*///?}

//~ if >=1.20 '@SideOnly(Side' -> '@OnlyIn(Dist' {
@SideOnly(Side.CLIENT)
//~}
public final class NodeHighlightRenderingHandler {

    public static final NodeHighlightRenderingHandler INSTANCE = new NodeHighlightRenderingHandler();
    private static final long HIGHLIGHT_DURATION_TICKS = 200L;
    private static final int BLINK_HALF_PERIOD = 5;
    private static final float LINE_WIDTH = 2.5F;
    private static final float BOX_R = 1.0F;
    private static final float BOX_G = 1.0F;
    private static final float BOX_B = 0.0F;
    private static final float BOX_ALPHA = 0.85F;
    private static final double EXPAND = 0.002D;
    private BlockPos targetPos;
    private int targetDimId;
    private long startTick;
    private long clientTick;

    private NodeHighlightRenderingHandler() {
    }

    public void highlight(BlockPos pos, int dimId) {
        this.targetPos = pos;
        this.targetDimId = dimId;
        this.startTick = clientTick;
    }

    public void clear() {
        targetPos = null;
    }

    //? if <1.20 {
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        clientTick++;
        if (targetPos != null && clientTick - startTick > HIGHLIGHT_DURATION_TICKS) {
            targetPos = null;
        }
    }
    //?} else {
    /*@SubscribeEvent
    //~ if >=1.21 'TickEvent.ClientTickEvent' -> 'ClientTickEvent.Pre' {
    public void onClientTick(TickEvent.ClientTickEvent event) {
    //~}
        //? if <1.21 {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        //?}
        clientTick++;
        if (targetPos != null && clientTick - startTick > HIGHLIGHT_DURATION_TICKS) {
            targetPos = null;
        }
    }
    *///?}

    //? if <1.20 {
    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || mc.player == null || targetPos == null) {
            return;
        }
        if (mc.player.dimension != targetDimId) {
            return;
        }
        long elapsed = clientTick - startTick;
        if (elapsed > HIGHLIGHT_DURATION_TICKS) {
            return;
        }
        int blinkPhase = (int) (elapsed % (BLINK_HALF_PERIOD * 2));
        if (blinkPhase >= BLINK_HALF_PERIOD) {
            return;
        }
        float partialTicks = event.getPartialTicks();
        double camX = mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * partialTicks;
        double camY = mc.player.lastTickPosY + (mc.player.posY - mc.player.lastTickPosY) * partialTicks;
        double camZ = mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * partialTicks;
        renderHighlight(camX, camY, camZ, targetPos);
    }

    private void renderHighlight(double camX, double camY, double camZ, BlockPos pos) {
        double x = pos.getX() - camX;
        double y = pos.getY() - camY;
        double z = pos.getZ() - camZ;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA,
            GL11.GL_ONE, GL11.GL_ZERO
        );
        GlStateManager.glLineWidth(LINE_WIDTH);
        GlStateManager.color(BOX_R, BOX_G, BOX_B, BOX_ALPHA);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);

        double minX = 0.0D - EXPAND;
        double minY = 0.0D - EXPAND;
        double minZ = 0.0D - EXPAND;
        double maxX = 1.0D + EXPAND;
        double maxY = 1.0D + EXPAND;
        double maxZ = 1.0D + EXPAND;

        // Bottom face edges
        buf.pos(minX, minY, minZ).endVertex();
        buf.pos(maxX, minY, minZ).endVertex();
        buf.pos(maxX, minY, minZ).endVertex();
        buf.pos(maxX, minY, maxZ).endVertex();
        buf.pos(maxX, minY, maxZ).endVertex();
        buf.pos(minX, minY, maxZ).endVertex();
        buf.pos(minX, minY, maxZ).endVertex();
        buf.pos(minX, minY, minZ).endVertex();
        // Top face edges
        buf.pos(minX, maxY, minZ).endVertex();
        buf.pos(maxX, maxY, minZ).endVertex();
        buf.pos(maxX, maxY, minZ).endVertex();
        buf.pos(maxX, maxY, maxZ).endVertex();
        buf.pos(maxX, maxY, maxZ).endVertex();
        buf.pos(minX, maxY, maxZ).endVertex();
        buf.pos(minX, maxY, maxZ).endVertex();
        buf.pos(minX, maxY, minZ).endVertex();
        // Vertical edges
        buf.pos(minX, minY, minZ).endVertex();
        buf.pos(minX, maxY, minZ).endVertex();
        buf.pos(maxX, minY, minZ).endVertex();
        buf.pos(maxX, maxY, minZ).endVertex();
        buf.pos(maxX, minY, maxZ).endVertex();
        buf.pos(maxX, maxY, maxZ).endVertex();
        buf.pos(minX, minY, maxZ).endVertex();
        buf.pos(minX, maxY, maxZ).endVertex();

        tess.draw();

        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
    //?} else {
    /*@SubscribeEvent
    public void onRenderWorldLast(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || targetPos == null) {
            return;
        }
        if (mc.level.dimension().location().hashCode() != targetDimId) {
            return;
        }
        long elapsed = clientTick - startTick;
        if (elapsed > HIGHLIGHT_DURATION_TICKS) {
            return;
        }
        int blinkPhase = (int) (elapsed % (BLINK_HALF_PERIOD * 2));
        if (blinkPhase >= BLINK_HALF_PERIOD) {
            return;
        }
        //? if <1.21 {
        float partialTicks = event.getPartialTick();
        //?} else {
        /^float partialTicks = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        ^///?}
        double camX = mc.player.xOld + (mc.player.getX() - mc.player.xOld) * partialTicks;
        double camY = mc.player.yOld + (mc.player.getY() - mc.player.yOld) * partialTicks;
        double camZ = mc.player.zOld + (mc.player.getZ() - mc.player.zOld) * partialTicks;
        renderHighlight(event.getPoseStack(), camX, camY, camZ, targetPos);
    }

    private void renderHighlight(PoseStack poseStack, double camX, double camY, double camZ, BlockPos pos) {
        double x = pos.getX() - camX;
        double y = pos.getY() - camY;
        double z = pos.getZ() - camZ;

        poseStack.pushPose();
        poseStack.translate(x, y, z);

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.lineWidth(LINE_WIDTH);
        RenderSystem.setShaderColor(BOX_R, BOX_G, BOX_B, BOX_ALPHA);
        //~ if >=1.21 'RenderSystem.setShader(GameRenderer::getPositionShader)' -> 'RenderSystem.setShader(GameRenderer::getPositionColorShader)' {
        RenderSystem.setShader(GameRenderer::getPositionShader);
        //~}

        Tesselator tess = Tesselator.getInstance();
        //~ if >=1.21 'BufferBuilder buf = tess.getBuilder();' -> 'BufferBuilder buf = tess.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);' {
        BufferBuilder buf = tess.getBuilder();
        //~}
        var matrix = poseStack.last().pose();

        float minX = (float) (0.0D - EXPAND);
        float minY = (float) (0.0D - EXPAND);
        float minZ = (float) (0.0D - EXPAND);
        float maxX = (float) (1.0D + EXPAND);
        float maxY = (float) (1.0D + EXPAND);
        float maxZ = (float) (1.0D + EXPAND);

        //? if <1.21 {
        buf.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION);
        buf.vertex(matrix, minX, minY, minZ).endVertex(); buf.vertex(matrix, maxX, minY, minZ).endVertex();
        buf.vertex(matrix, maxX, minY, minZ).endVertex(); buf.vertex(matrix, maxX, minY, maxZ).endVertex();
        buf.vertex(matrix, maxX, minY, maxZ).endVertex(); buf.vertex(matrix, minX, minY, maxZ).endVertex();
        buf.vertex(matrix, minX, minY, maxZ).endVertex(); buf.vertex(matrix, minX, minY, minZ).endVertex();
        buf.vertex(matrix, minX, maxY, minZ).endVertex(); buf.vertex(matrix, maxX, maxY, minZ).endVertex();
        buf.vertex(matrix, maxX, maxY, minZ).endVertex(); buf.vertex(matrix, maxX, maxY, maxZ).endVertex();
        buf.vertex(matrix, maxX, maxY, maxZ).endVertex(); buf.vertex(matrix, minX, maxY, maxZ).endVertex();
        buf.vertex(matrix, minX, maxY, maxZ).endVertex(); buf.vertex(matrix, minX, maxY, minZ).endVertex();
        buf.vertex(matrix, minX, minY, minZ).endVertex(); buf.vertex(matrix, minX, maxY, minZ).endVertex();
        buf.vertex(matrix, maxX, minY, minZ).endVertex(); buf.vertex(matrix, maxX, maxY, minZ).endVertex();
        buf.vertex(matrix, maxX, minY, maxZ).endVertex(); buf.vertex(matrix, maxX, maxY, maxZ).endVertex();
        buf.vertex(matrix, minX, minY, maxZ).endVertex(); buf.vertex(matrix, minX, maxY, maxZ).endVertex();
        tess.end();
        //?} else {
        /^int r = (int)(BOX_R * 255); int g = (int)(BOX_G * 255); int b = (int)(BOX_B * 255); int a = (int)(BOX_ALPHA * 255);
        buf.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a); buf.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a);
        buf.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a); buf.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a);
        buf.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a); buf.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a);
        buf.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a); buf.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a);
        buf.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a); buf.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a);
        buf.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a); buf.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a);
        buf.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a); buf.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a);
        buf.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a); buf.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a);
        buf.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a); buf.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a);
        buf.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a); buf.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a);
        buf.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a); buf.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a);
        buf.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a); buf.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a);
        com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(buf.buildOrThrow());
        ^///?}

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }
    *///?}
}
