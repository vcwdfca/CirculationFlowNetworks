package com.circulation.circulation_networks.utils;

import net.minecraft.client.Minecraft;
//? if <1.20 {
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
//?} else if <1.21 {
/*import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
*///?} else {
/*import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
*///?}
//? if <1.20 {
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
//?} else {
/*import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.player.LocalPlayer;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;
import java.nio.FloatBuffer;
*///?}
import org.lwjgl.opengl.GL11;

@SuppressWarnings("unused")
//~ if >=1.20 '@SideOnly(Side.CLIENT)' -> '@OnlyIn(Dist.CLIENT)' {
@SideOnly(Side.CLIENT)
//~}
public final class RenderingUtils {

    private static final int CYLINDER_SIDES = 8;
    private static final double CYLINDER_ANGLE_STEP = 2.0 * Math.PI / CYLINDER_SIDES;

    //? if >=1.20 {
    /*private static int sphereVAO, sphereVBO, sphereVertexCount;
    private static boolean sphereVBOInitialized;
    private static int cachedSphereProgId;
    private static int locMV = -1, locProj = -1, locColor = -1;
    private static final FloatBuffer sphereMvBuf = MemoryUtil.memAllocFloat(16);
    private static final FloatBuffer sphereProjBuf = MemoryUtil.memAllocFloat(16);
    *///?}

    private RenderingUtils() {
    }

    public static void setupWorldRenderState() {
        //? if <1.20 {
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);
        //?} else {
        /*RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        *///?}
    }

    public static void restoreWorldRenderState() {
        //? if <1.20 {
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        //?} else {
        /*RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        *///?}
    }

    public static void setupAdditiveBlend() {
        //? if <1.20 {
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        //?} else {
        /*RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
         *///?}
    }

    //? if >=1.20 {
    /*public static void seedModelViewFromPoseStack(PoseStack poseStack) {
        if (poseStack == null) {
            return;
        }
        //? if <1.21 {
        PoseStack mvStack = RenderSystem.getModelViewStack();
        mvStack.last().pose().set(poseStack.last().pose());
        mvStack.last().normal().set(poseStack.last().normal());
        //?} else {
        /^RenderSystem.getModelViewStack().set(poseStack.last().pose());
        ^///?}
        RenderSystem.applyModelViewMatrix();
    }
    *///?}

    public static void drawFilledBox(double x0, double y0, double z0,
                                     double x1, double y1, double z1,
                                     float r, float g, float b, float a) {
        //? if <1.20 {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        GlStateManager.color(r, g, b, a);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        //?} else if <1.21 {
        /*Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();

        RenderSystem.setShaderColor(r, g, b, a);
        RenderSystem.setShader(GameRenderer::getPositionShader);
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        *///?} else {
        /*Tesselator tess = Tesselator.getInstance();

        RenderSystem.setShaderColor(r, g, b, a);
        RenderSystem.setShader(GameRenderer::getPositionShader);
        BufferBuilder buf = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        *///?}

        //? if <1.20 {
        buf.pos(x0, y0, z0).endVertex();
        buf.pos(x1, y0, z0).endVertex();
        buf.pos(x1, y0, z1).endVertex();
        buf.pos(x0, y0, z1).endVertex();
        buf.pos(x0, y1, z0).endVertex();
        buf.pos(x0, y1, z1).endVertex();
        buf.pos(x1, y1, z1).endVertex();
        buf.pos(x1, y1, z0).endVertex();
        buf.pos(x0, y0, z0).endVertex();
        buf.pos(x0, y1, z0).endVertex();
        buf.pos(x1, y1, z0).endVertex();
        buf.pos(x1, y0, z0).endVertex();
        buf.pos(x0, y0, z1).endVertex();
        buf.pos(x1, y0, z1).endVertex();
        buf.pos(x1, y1, z1).endVertex();
        buf.pos(x0, y1, z1).endVertex();
        buf.pos(x0, y0, z0).endVertex();
        buf.pos(x0, y0, z1).endVertex();
        buf.pos(x0, y1, z1).endVertex();
        buf.pos(x0, y1, z0).endVertex();
        buf.pos(x1, y0, z0).endVertex();
        buf.pos(x1, y1, z0).endVertex();
        buf.pos(x1, y1, z1).endVertex();
        buf.pos(x1, y0, z1).endVertex();
        tess.draw();
        //?} else if <1.21 {
        /*buf.vertex(x0, y0, z0).endVertex();
        buf.vertex(x1, y0, z0).endVertex();
        buf.vertex(x1, y0, z1).endVertex();
        buf.vertex(x0, y0, z1).endVertex();
        buf.vertex(x0, y1, z0).endVertex();
        buf.vertex(x0, y1, z1).endVertex();
        buf.vertex(x1, y1, z1).endVertex();
        buf.vertex(x1, y1, z0).endVertex();
        buf.vertex(x0, y0, z0).endVertex();
        buf.vertex(x0, y1, z0).endVertex();
        buf.vertex(x1, y1, z0).endVertex();
        buf.vertex(x1, y0, z0).endVertex();
        buf.vertex(x0, y0, z1).endVertex();
        buf.vertex(x1, y0, z1).endVertex();
        buf.vertex(x1, y1, z1).endVertex();
        buf.vertex(x0, y1, z1).endVertex();
        buf.vertex(x0, y0, z0).endVertex();
        buf.vertex(x0, y0, z1).endVertex();
        buf.vertex(x0, y1, z1).endVertex();
        buf.vertex(x0, y1, z0).endVertex();
        buf.vertex(x1, y0, z0).endVertex();
        buf.vertex(x1, y1, z0).endVertex();
        buf.vertex(x1, y1, z1).endVertex();
        buf.vertex(x1, y0, z1).endVertex();
        tess.end();
        *///?} else {
        /*buf.addVertex((float) x0, (float) y0, (float) z0);
        buf.addVertex((float) x1, (float) y0, (float) z0);
        buf.addVertex((float) x1, (float) y0, (float) z1);
        buf.addVertex((float) x0, (float) y0, (float) z1);
        buf.addVertex((float) x0, (float) y1, (float) z0);
        buf.addVertex((float) x0, (float) y1, (float) z1);
        buf.addVertex((float) x1, (float) y1, (float) z1);
        buf.addVertex((float) x1, (float) y1, (float) z0);
        buf.addVertex((float) x0, (float) y0, (float) z0);
        buf.addVertex((float) x0, (float) y1, (float) z0);
        buf.addVertex((float) x1, (float) y1, (float) z0);
        buf.addVertex((float) x1, (float) y0, (float) z0);
        buf.addVertex((float) x0, (float) y0, (float) z1);
        buf.addVertex((float) x1, (float) y0, (float) z1);
        buf.addVertex((float) x1, (float) y1, (float) z1);
        buf.addVertex((float) x0, (float) y1, (float) z1);
        buf.addVertex((float) x0, (float) y0, (float) z0);
        buf.addVertex((float) x0, (float) y0, (float) z1);
        buf.addVertex((float) x0, (float) y1, (float) z1);
        buf.addVertex((float) x0, (float) y1, (float) z0);
        buf.addVertex((float) x1, (float) y0, (float) z0);
        buf.addVertex((float) x1, (float) y1, (float) z0);
        buf.addVertex((float) x1, (float) y1, (float) z1);
        buf.addVertex((float) x1, (float) y0, (float) z1);
        BufferUploader.drawWithShader(buf.buildOrThrow());
        *///?}
    }

    public static void drawBoxEdges(double x0, double y0, double z0,
                                    double x1, double y1, double z1,
                                    float r, float g, float b, float a,
                                    float lineWidth) {
        //? if <1.20 {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        GlStateManager.color(r, g, b, a);
        GlStateManager.glLineWidth(lineWidth);
        buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        //?} else if <1.21 {
        /*Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();

        RenderSystem.setShaderColor(r, g, b, a);
        RenderSystem.lineWidth(lineWidth);
        RenderSystem.setShader(GameRenderer::getPositionShader);
        buf.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION);
        *///?} else {
        /*Tesselator tess = Tesselator.getInstance();

        RenderSystem.setShaderColor(r, g, b, a);
        RenderSystem.lineWidth(lineWidth);
        RenderSystem.setShader(GameRenderer::getPositionShader);
        BufferBuilder buf = tess.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION);
        *///?}

        //? if <1.20 {
        buf.pos(x0, y0, z0).endVertex();
        buf.pos(x1, y0, z0).endVertex();
        buf.pos(x1, y0, z0).endVertex();
        buf.pos(x1, y0, z1).endVertex();
        buf.pos(x1, y0, z1).endVertex();
        buf.pos(x0, y0, z1).endVertex();
        buf.pos(x0, y0, z1).endVertex();
        buf.pos(x0, y0, z0).endVertex();
        buf.pos(x0, y1, z0).endVertex();
        buf.pos(x1, y1, z0).endVertex();
        buf.pos(x1, y1, z0).endVertex();
        buf.pos(x1, y1, z1).endVertex();
        buf.pos(x1, y1, z1).endVertex();
        buf.pos(x0, y1, z1).endVertex();
        buf.pos(x0, y1, z1).endVertex();
        buf.pos(x0, y1, z0).endVertex();
        buf.pos(x0, y0, z0).endVertex();
        buf.pos(x0, y1, z0).endVertex();
        buf.pos(x1, y0, z0).endVertex();
        buf.pos(x1, y1, z0).endVertex();
        buf.pos(x1, y0, z1).endVertex();
        buf.pos(x1, y1, z1).endVertex();
        buf.pos(x0, y0, z1).endVertex();
        buf.pos(x0, y1, z1).endVertex();
        tess.draw();
        //?} else if <1.21 {
        /*buf.vertex(x0, y0, z0).endVertex();
        buf.vertex(x1, y0, z0).endVertex();
        buf.vertex(x1, y0, z0).endVertex();
        buf.vertex(x1, y0, z1).endVertex();
        buf.vertex(x1, y0, z1).endVertex();
        buf.vertex(x0, y0, z1).endVertex();
        buf.vertex(x0, y0, z1).endVertex();
        buf.vertex(x0, y0, z0).endVertex();
        buf.vertex(x0, y1, z0).endVertex();
        buf.vertex(x1, y1, z0).endVertex();
        buf.vertex(x1, y1, z0).endVertex();
        buf.vertex(x1, y1, z1).endVertex();
        buf.vertex(x1, y1, z1).endVertex();
        buf.vertex(x0, y1, z1).endVertex();
        buf.vertex(x0, y1, z1).endVertex();
        buf.vertex(x0, y1, z0).endVertex();
        buf.vertex(x0, y0, z0).endVertex();
        buf.vertex(x0, y1, z0).endVertex();
        buf.vertex(x1, y0, z0).endVertex();
        buf.vertex(x1, y1, z0).endVertex();
        buf.vertex(x1, y0, z1).endVertex();
        buf.vertex(x1, y1, z1).endVertex();
        buf.vertex(x0, y0, z1).endVertex();
        buf.vertex(x0, y1, z1).endVertex();
        tess.end();
        *///?} else {
        /*buf.addVertex((float) x0, (float) y0, (float) z0);
        buf.addVertex((float) x1, (float) y0, (float) z0);
        buf.addVertex((float) x1, (float) y0, (float) z0);
        buf.addVertex((float) x1, (float) y0, (float) z1);
        buf.addVertex((float) x1, (float) y0, (float) z1);
        buf.addVertex((float) x0, (float) y0, (float) z1);
        buf.addVertex((float) x0, (float) y0, (float) z1);
        buf.addVertex((float) x0, (float) y0, (float) z0);
        buf.addVertex((float) x0, (float) y1, (float) z0);
        buf.addVertex((float) x1, (float) y1, (float) z0);
        buf.addVertex((float) x1, (float) y1, (float) z0);
        buf.addVertex((float) x1, (float) y1, (float) z1);
        buf.addVertex((float) x1, (float) y1, (float) z1);
        buf.addVertex((float) x0, (float) y1, (float) z1);
        buf.addVertex((float) x0, (float) y1, (float) z1);
        buf.addVertex((float) x0, (float) y1, (float) z0);
        buf.addVertex((float) x0, (float) y0, (float) z0);
        buf.addVertex((float) x0, (float) y1, (float) z0);
        buf.addVertex((float) x1, (float) y0, (float) z0);
        buf.addVertex((float) x1, (float) y1, (float) z0);
        buf.addVertex((float) x1, (float) y0, (float) z1);
        buf.addVertex((float) x1, (float) y1, (float) z1);
        buf.addVertex((float) x0, (float) y0, (float) z1);
        buf.addVertex((float) x0, (float) y1, (float) z1);
        BufferUploader.drawWithShader(buf.buildOrThrow());
        *///?}
    }

    public static void drawLaserCylinder(double fromX, double fromY, double fromZ,
                                         double toX, double toY, double toZ,
                                         float radius,
                                         float r, float g, float b, float alpha) {
        double dx = toX - fromX;
        double dy = toY - fromY;
        double dz = toZ - fromZ;
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1e-6) return;

        double ax = dx / len, ay = dy / len, az = dz / len;

        double bx, by, bz;
        if (Math.abs(ax) <= Math.abs(ay) && Math.abs(ax) <= Math.abs(az)) {
            bx = 0;
            by = -az;
            bz = ay;
        } else if (Math.abs(ay) <= Math.abs(az)) {
            bx = -az;
            by = 0;
            bz = ax;
        } else {
            bx = -ay;
            by = ax;
            bz = 0;
        }
        double bLen = Math.sqrt(bx * bx + by * by + bz * bz);
        bx /= bLen;
        by /= bLen;
        bz /= bLen;

        double cx = ay * bz - az * by;
        double cy = az * bx - ax * bz;
        double cz = ax * by - ay * bx;

        //? if <1.20 {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        GlStateManager.color(r, g, b, alpha);
        buf.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION);
        //?} else if <1.21 {
        /*Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();

        RenderSystem.setShaderColor(r, g, b, alpha);
        RenderSystem.setShader(GameRenderer::getPositionShader);
        buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION);
        *///?} else {
        /*Tesselator tess = Tesselator.getInstance();

        RenderSystem.setShaderColor(r, g, b, alpha);
        RenderSystem.setShader(GameRenderer::getPositionShader);
        BufferBuilder buf = tess.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION);
        *///?}
        for (int i = 0; i <= CYLINDER_SIDES; i++) {
            double angle = CYLINDER_ANGLE_STEP * i;
            double cos = Math.cos(angle), sin = Math.sin(angle);
            double nx = radius * (cos * bx + sin * cx);
            double ny = radius * (cos * by + sin * cy);
            double nz = radius * (cos * bz + sin * cz);
            //? if <1.20 {
            buf.pos(fromX + nx, fromY + ny, fromZ + nz).endVertex();
            buf.pos(toX + nx, toY + ny, toZ + nz).endVertex();
            //?} else if <1.21 {
            /*buf.vertex(fromX + nx, fromY + ny, fromZ + nz).endVertex();
            buf.vertex(toX + nx, toY + ny, toZ + nz).endVertex();
            *///?} else {
            /*buf.addVertex((float)(fromX + nx), (float)(fromY + ny), (float)(fromZ + nz));
            buf.addVertex((float)(toX + nx), (float)(toY + ny), (float)(toZ + nz));
            *///?}
        }
        //? if <1.20 {
        tess.draw();
        //?} else if <1.21 {
        /*tess.end();
         *///?} else {
        /*BufferUploader.drawWithShader(buf.buildOrThrow());
         *///?}
    }

    public static void drawSphere(float r, float g, float b, float radius, float alpha) {
        drawSphere(r, g, b, radius, alpha, 32, 32);
    }

    public static void drawSphere(float r, float g, float b, float radius, float alpha, int slices, int stacks) {
        //? if <1.20 {
        GlStateManager.color(r, g, b, alpha);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_NORMAL);
        double phiStep = Math.PI / slices;
        double thetaStep = 2.0 * Math.PI / stacks;
        for (int i = 0; i < slices; i++) {
            double phi1 = phiStep * i;
            double phi2 = phiStep * (i + 1);
            double sinPhi1 = Math.sin(phi1), cosPhi1 = Math.cos(phi1);
            double sinPhi2 = Math.sin(phi2), cosPhi2 = Math.cos(phi2);
            for (int j = 0; j < stacks; j++) {
                double theta1 = thetaStep * j;
                double theta2 = thetaStep * (j + 1);
                double cosT1 = Math.cos(theta1), sinT1 = Math.sin(theta1);
                double cosT2 = Math.cos(theta2), sinT2 = Math.sin(theta2);
                float x00 = (float) (radius * sinPhi1 * cosT1);
                float y00 = (float) (radius * cosPhi1);
                float z00 = (float) (radius * sinPhi1 * sinT1);
                float x10 = (float) (radius * sinPhi2 * cosT1);
                float y10 = (float) (radius * cosPhi2);
                float z10 = (float) (radius * sinPhi2 * sinT1);
                float x01 = (float) (radius * sinPhi1 * cosT2);
                float y01 = (float) (radius * cosPhi1);
                float z01 = (float) (radius * sinPhi1 * sinT2);
                float x11 = (float) (radius * sinPhi2 * cosT2);
                float y11 = (float) (radius * cosPhi2);
                float z11 = (float) (radius * sinPhi2 * sinT2);
                buf.pos(x00, y00, z00).normal(x00 / radius, y00 / radius, z00 / radius).endVertex();
                buf.pos(x10, y10, z10).normal(x10 / radius, y10 / radius, z10 / radius).endVertex();
                buf.pos(x11, y11, z11).normal(x11 / radius, y11 / radius, z11 / radius).endVertex();
                buf.pos(x00, y00, z00).normal(x00 / radius, y00 / radius, z00 / radius).endVertex();
                buf.pos(x11, y11, z11).normal(x11 / radius, y11 / radius, z11 / radius).endVertex();
                buf.pos(x01, y01, z01).normal(x01 / radius, y01 / radius, z01 / radius).endVertex();
            }
        }
        tess.draw();
        //?} else {
        /*drawSphereVBO(r, g, b, radius, alpha, slices, stacks);
        *///?}
    }

    //? if >=1.20 {
    /*private static FloatBuffer buildUnitSphereData(int slices, int stacks) {
        FloatBuffer buf = MemoryUtil.memAllocFloat(slices * stacks * 6 * 3);
        for (int i = 0; i < slices; i++) {
            double p1 = Math.PI * i / slices, p2 = Math.PI * (i + 1) / slices;
            for (int j = 0; j < stacks; j++) {
                double t1 = 2.0 * Math.PI * j / stacks, t2 = 2.0 * Math.PI * (j + 1) / stacks;
                float x00 = (float) (Math.sin(p1) * Math.cos(t1)), y00 = (float) Math.cos(p1), z00 = (float) (Math.sin(p1) * Math.sin(t1));
                float x10 = (float) (Math.sin(p2) * Math.cos(t1)), y10 = (float) Math.cos(p2), z10 = (float) (Math.sin(p2) * Math.sin(t1));
                float x01 = (float) (Math.sin(p1) * Math.cos(t2)), y01 = (float) Math.cos(p1), z01 = (float) (Math.sin(p1) * Math.sin(t2));
                float x11 = (float) (Math.sin(p2) * Math.cos(t2)), y11 = (float) Math.cos(p2), z11 = (float) (Math.sin(p2) * Math.sin(t2));
                buf.put(x00).put(y00).put(z00).put(x10).put(y10).put(z10).put(x01).put(y01).put(z01);
                buf.put(x10).put(y10).put(z10).put(x11).put(y11).put(z11).put(x01).put(y01).put(z01);
            }
        }
        buf.flip();
        return buf;
    }

    private static void ensureSphereVBO(int slices, int stacks) {
        if (sphereVBOInitialized) return;
        FloatBuffer data = buildUnitSphereData(slices, stacks);
        try {
            sphereVertexCount = data.remaining() / 3;
            sphereVBO = GlStateManager._glGenBuffers();
            sphereVAO = GL30.glGenVertexArrays();
            GlStateManager._glBindVertexArray(sphereVAO);
            GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, sphereVBO);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
            GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
            GL20.glEnableVertexAttribArray(0);
            GlStateManager._glBindVertexArray(0);
            GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        } finally {
            MemoryUtil.memFree(data);
        }
        sphereVBOInitialized = true;
    }

    private static void drawSphereVBO(float r, float g, float b, float radius, float alpha, int slices, int stacks) {
        ensureSphereVBO(slices, stacks);
        int progId = GameRenderer.getPositionShader().getId();
        if (progId != cachedSphereProgId) {
            cachedSphereProgId = progId;
            locMV = GL20.glGetUniformLocation(progId, "ModelViewMat");
            locProj = GL20.glGetUniformLocation(progId, "ProjMat");
            locColor = GL20.glGetUniformLocation(progId, "ColorModulator");
        }

        //? if <1.21 {
        PoseStack mvStack = RenderSystem.getModelViewStack();
        mvStack.pushPose();
        mvStack.scale(radius, radius, radius);
        sphereMvBuf.clear();
        mvStack.last().pose().get(sphereMvBuf);
        sphereMvBuf.rewind();
        //?} else {
        /^org.joml.Matrix4fStack mvStack = RenderSystem.getModelViewStack();
        mvStack.pushMatrix();
        mvStack.scale(radius, radius, radius);
        sphereMvBuf.clear();
        mvStack.get(sphereMvBuf);
        sphereMvBuf.rewind();
        ^///?}

        sphereProjBuf.clear();
        RenderSystem.getProjectionMatrix().get(sphereProjBuf);
        sphereProjBuf.rewind();

        GlStateManager._glUseProgram(progId);
        GL20.glUniformMatrix4fv(locMV, false, sphereMvBuf);
        GL20.glUniformMatrix4fv(locProj, false, sphereProjBuf);
        GL20.glUniform4f(locColor, r, g, b, alpha);

        GlStateManager._glBindVertexArray(sphereVAO);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, sphereVertexCount);
        GlStateManager._glBindVertexArray(0);

        GlStateManager._glUseProgram(0);

        //? if <1.21 {
        mvStack.popPose();
        //?} else {
        /^mvStack.popMatrix();
        ^///?}
    }
    *///?}

    public static double getPlayerRenderX(float partialTicks) {
        //? if <1.20 {
        EntityPlayerSP p = Minecraft.getMinecraft().player;
        return p.lastTickPosX + (p.posX - p.lastTickPosX) * partialTicks;
        //?} else {
        /*LocalPlayer p = Minecraft.getInstance().player;
        return p.xOld + (p.getX() - p.xOld) * partialTicks;
        *///?}
    }

    public static double getPlayerRenderY(float partialTicks) {
        //? if <1.20 {
        EntityPlayerSP p = Minecraft.getMinecraft().player;
        return p.lastTickPosY + (p.posY - p.lastTickPosY) * partialTicks;
        //?} else {
        /*LocalPlayer p = Minecraft.getInstance().player;
        return p.yOld + (p.getY() - p.yOld) * partialTicks;
        *///?}
    }

    public static double getPlayerRenderZ(float partialTicks) {
        //? if <1.20 {
        EntityPlayerSP p = Minecraft.getMinecraft().player;
        return p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * partialTicks;
        //?} else {
        /*LocalPlayer p = Minecraft.getInstance().player;
        return p.zOld + (p.getZ() - p.zOld) * partialTicks;
        *///?}
    }

    public static boolean isWithinRenderDistance(double posX, double posY, double posZ,
                                                 double playerX, double playerY, double playerZ,
                                                 double maxDistSq) {
        double dx = posX - playerX;
        double dy = posY - playerY;
        double dz = posZ - playerZ;
        return dx * dx + dy * dy + dz * dz <= maxDistSq;
    }

    public static void drawCachedIntersection(float[] verts, float r, float g, float b, float lineWidth) {
        if (verts.length == 0) return;
        GL11.glDepthRange(0.0, 0.9998);
        //? if <1.20 {
        GlStateManager.color(r, g, b, 1.0f);
        GlStateManager.glLineWidth(lineWidth);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        for (int i = 0; i < verts.length; i += 3) {
            buf.pos(verts[i], verts[i + 1], verts[i + 2]).endVertex();
        }
        tess.draw();
        //?} else if <1.21 {
        /*RenderSystem.setShaderColor(r, g, b, 1.0f);
        RenderSystem.lineWidth(lineWidth);
        RenderSystem.setShader(GameRenderer::getPositionShader);
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();
        buf.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION);
        for (int i = 0; i < verts.length; i += 3) {
            buf.vertex(verts[i], verts[i + 1], verts[i + 2]).endVertex();
        }
        tess.end();
        *///?} else {
        /*RenderSystem.setShaderColor(r, g, b, 1.0f);
        RenderSystem.lineWidth(lineWidth);
        RenderSystem.setShader(GameRenderer::getPositionShader);
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION);
        for (int i = 0; i < verts.length; i += 3) {
            buf.addVertex(verts[i], verts[i + 1], verts[i + 2]);
        }
        BufferUploader.drawWithShader(buf.buildOrThrow());
        *///?}
        GL11.glDepthRange(0.0, 1.0);
    }

    public static void drawCachedIntersection(float[] verts, float r, float g, float b) {
        drawCachedIntersection(verts, r, g, b, 4.0f);
    }
}
