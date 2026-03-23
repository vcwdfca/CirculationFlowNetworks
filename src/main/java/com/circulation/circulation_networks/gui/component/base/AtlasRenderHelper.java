package com.circulation.circulation_networks.gui.component.base;

//? if <1.20 {
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
//?} else {
/*import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
*///?}

public final class AtlasRenderHelper {

    private AtlasRenderHelper() {
    }

    public static void drawRegion(ComponentAtlas atlas, AtlasRegion region, int screenX, int screenY, int renderW, int renderH) {
        atlas.bind();
        //? if <1.20 {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(screenX, screenY + renderH, 0).tex(region.u0(), region.v1()).endVertex();
        buf.pos(screenX + renderW, screenY + renderH, 0).tex(region.u1(), region.v1()).endVertex();
        buf.pos(screenX + renderW, screenY, 0).tex(region.u1(), region.v0()).endVertex();
        buf.pos(screenX, screenY, 0).tex(region.u0(), region.v0()).endVertex();
        tess.draw();
        //?} else {
            /*Tesselator tess = Tesselator.getInstance();
            //? if <1.21 {
            BufferBuilder buf = tess.getBuilder();
            buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            buf.vertex(screenX, screenY + renderH, 0).uv(region.u0(), region.v1()).endVertex();
            buf.vertex(screenX + renderW, screenY + renderH, 0).uv(region.u1(), region.v1()).endVertex();
            buf.vertex(screenX + renderW, screenY, 0).uv(region.u1(), region.v0()).endVertex();
            buf.vertex(screenX, screenY, 0).uv(region.u0(), region.v0()).endVertex();
            tess.end();
            //?} else {
            BufferBuilder buf = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            buf.addVertex(screenX, screenY + renderH, 0).setUv(region.u0(), region.v1());
            buf.addVertex(screenX + renderW, screenY + renderH, 0).setUv(region.u1(), region.v1());
            buf.addVertex(screenX + renderW, screenY, 0).setUv(region.u1(), region.v0());
            buf.addVertex(screenX, screenY, 0).setUv(region.u0(), region.v0());
            com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(buf.buildOrThrow());
            //?}
        *///?}
    }

    public static void drawSubRegion(ComponentAtlas atlas,
                                     AtlasRegion region,
                                     int srcX,
                                     int srcY,
                                     int srcW,
                                     int srcH,
                                     int screenX,
                                     int screenY,
                                     int renderW,
                                     int renderH) {
        if (srcW <= 0 || srcH <= 0 || renderW <= 0 || renderH <= 0) {
            return;
        }
        if (srcX < 0 || srcY < 0 || srcX + srcW > region.width || srcY + srcH > region.height) {
            return;
        }

        float u0 = (float) (region.x + srcX) / region.atlasWidth;
        float v0 = (float) (region.y + srcY) / region.atlasHeight;
        float u1 = (float) (region.x + srcX + srcW) / region.atlasWidth;
        float v1 = (float) (region.y + srcY + srcH) / region.atlasHeight;

        atlas.bind();
        //? if <1.20 {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(screenX, screenY + renderH, 0).tex(u0, v1).endVertex();
        buf.pos(screenX + renderW, screenY + renderH, 0).tex(u1, v1).endVertex();
        buf.pos(screenX + renderW, screenY, 0).tex(u1, v0).endVertex();
        buf.pos(screenX, screenY, 0).tex(u0, v0).endVertex();
        tess.draw();
        //?} else {
            /*Tesselator tess = Tesselator.getInstance();
            //? if <1.21 {
            BufferBuilder buf = tess.getBuilder();
            buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            buf.vertex(screenX, screenY + renderH, 0).uv(u0, v1).endVertex();
            buf.vertex(screenX + renderW, screenY + renderH, 0).uv(u1, v1).endVertex();
            buf.vertex(screenX + renderW, screenY, 0).uv(u1, v0).endVertex();
            buf.vertex(screenX, screenY, 0).uv(u0, v0).endVertex();
            tess.end();
            //?} else {
            BufferBuilder buf = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            buf.addVertex(screenX, screenY + renderH, 0).setUv(u0, v1);
            buf.addVertex(screenX + renderW, screenY + renderH, 0).setUv(u1, v1);
            buf.addVertex(screenX + renderW, screenY, 0).setUv(u1, v0);
            buf.addVertex(screenX, screenY, 0).setUv(u0, v0);
            com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(buf.buildOrThrow());
            //?}
        *///?}
    }
}