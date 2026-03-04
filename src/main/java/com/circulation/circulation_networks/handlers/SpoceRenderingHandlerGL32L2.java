package com.circulation.circulation_networks.handlers;

import com.circulation.circulation_networks.utils.BuckyBallGeometry;
import com.circulation.circulation_networks.utils.ShaderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;

@SideOnly(Side.CLIENT)
public class SpoceRenderingHandlerGL32L2 extends SpoceRenderingHandler {

    private static final ResourceLocation SHADER_VERT = new ResourceLocation("circulation_networks", "shaders/sphere_depth.vsh");
    private static final ResourceLocation SHADER_FRAG = new ResourceLocation("circulation_networks", "shaders/sphere_depth.fsh");

    protected int sphereVAO, sphereVBO, sphereVertexCount;
    protected int buckyVAO, buckyVBO, buckyVertexCount;
    private final FloatBuffer projBuf = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer mvBuf = BufferUtils.createFloatBuffer(16);
    private int shaderProgram;
    private int depthTextureId;
    private int lastScreenWidth, lastScreenHeight;
    private boolean shaderInitialized;
    private int loc_ModelViewMatrix, loc_ProjectionMatrix;
    private int loc_DepthTexture, loc_ScreenSize;
    private int loc_Near, loc_Far;
    private int loc_SphereColor, loc_IntersectionColor;
    private int loc_IntersectionWidth;

    protected static FloatBuffer buildSphereData() {
        final int slices = 32, stacks = 32;
        FloatBuffer buf = BufferUtils.createFloatBuffer(slices * stacks * 6 * 3);
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

    protected static FloatBuffer buildBuckyData() {
        FloatBuffer buf = BufferUtils.createFloatBuffer(BuckyBallGeometry.edges.size() * 2 * 3);
        for (int[] e : BuckyBallGeometry.edges) {
            Vec3d v1 = BuckyBallGeometry.vertices.get(e[0]), v2 = BuckyBallGeometry.vertices.get(e[1]);
            buf.put((float) v1.x).put((float) v1.y).put((float) v1.z);
            buf.put((float) v2.x).put((float) v2.y).put((float) v2.z);
        }
        buf.flip();
        return buf;
    }

    @Override
    protected void initGL() {
        FloatBuffer sd = buildSphereData();
        sphereVertexCount = sd.limit() / 3;
        sphereVBO = GL15.glGenBuffers();
        sphereVAO = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(sphereVAO);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, sphereVBO);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, sd, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0L);
        GL20.glEnableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        FloatBuffer bd = buildBuckyData();
        buckyVertexCount = bd.limit() / 3;
        buckyVBO = GL15.glGenBuffers();
        buckyVAO = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(buckyVAO);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, buckyVBO);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, bd, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0L);
        GL20.glEnableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    @Override
    protected void cleanupGL() {
        if (sphereVBO != 0) {
            try {
                GL15.glDeleteBuffers(sphereVBO);
            } catch (Exception ignored) {
            }
            sphereVBO = 0;
        }
        if (sphereVAO != 0) {
            try {
                GL30.glDeleteVertexArrays(sphereVAO);
            } catch (Exception ignored) {
            }
            sphereVAO = 0;
        }
        if (buckyVBO != 0) {
            try {
                GL15.glDeleteBuffers(buckyVBO);
            } catch (Exception ignored) {
            }
            buckyVBO = 0;
        }
        if (buckyVAO != 0) {
            try {
                GL30.glDeleteVertexArrays(buckyVAO);
            } catch (Exception ignored) {
            }
            buckyVAO = 0;
        }
        cleanupShaderResources();
    }

    @Override
    public void clear() {
        super.clear();
        sphereVertexCount = 0;
        buckyVertexCount = 0;
        lastScreenWidth = 0;
        lastScreenHeight = 0;
    }

    @Override
    protected void onPreRender() {
        if (!shaderInitialized) {
            initShaderResources();
            shaderInitialized = true;
        }
        if (shaderProgram > 0) {
            captureSceneDepth();
        }
    }

    @Override
    protected boolean usesShaderIntersection() {
        return shaderProgram > 0;
    }

    protected void drawSphere(float r, float g, float b, float radius, float alpha) {
        if (shaderProgram <= 0) {
            GlStateManager.color(r, g, b, alpha);
            GlStateManager.pushMatrix();
            GlStateManager.scale(radius, radius, radius);
            GL30.glBindVertexArray(sphereVAO);
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, sphereVertexCount);
            GL30.glBindVertexArray(0);
            GlStateManager.popMatrix();
            return;
        }

        GL20.glUseProgram(shaderProgram);

        GlStateManager.pushMatrix();
        GlStateManager.scale(radius, radius, radius);

        projBuf.clear();
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projBuf);
        projBuf.rewind();
        mvBuf.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, mvBuf);
        mvBuf.rewind();

        GL20.glUniformMatrix4(loc_ProjectionMatrix, false, projBuf);
        GL20.glUniformMatrix4(loc_ModelViewMatrix, false, mvBuf);

        float A = projBuf.get(10);
        float B = projBuf.get(14);
        float near = B / (A - 1.0f);
        float far = B / (A + 1.0f);
        GL20.glUniform1f(loc_Near, near);
        GL20.glUniform1f(loc_Far, far);

        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTextureId);
        GL20.glUniform1i(loc_DepthTexture, 2);

        GL20.glUniform2f(loc_ScreenSize, lastScreenWidth, lastScreenHeight);

        GL20.glUniform4f(loc_SphereColor, r, g, b, alpha);
        GL20.glUniform4f(loc_IntersectionColor,
            pendingIntersectionR, pendingIntersectionG, pendingIntersectionB, 1.0f);
        GL20.glUniform1f(loc_IntersectionWidth, 0.04f);

        GL30.glBindVertexArray(sphereVAO);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, sphereVertexCount);
        GL30.glBindVertexArray(0);

        GlStateManager.popMatrix();

        GL20.glUseProgram(0);
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    @Override
    protected void drawBuckyBallWireframe(float r, float g, float b, float radius, float alpha) {
        GlStateManager.color(r, g, b, alpha);
        GlStateManager.glLineWidth(2.0f);
        GlStateManager.pushMatrix();
        GlStateManager.scale(radius, radius, radius);
        GL30.glBindVertexArray(buckyVAO);
        GL11.glDrawArrays(GL11.GL_LINES, 0, buckyVertexCount);
        GL30.glBindVertexArray(0);
        GlStateManager.popMatrix();
    }

    private void initShaderResources() {
        shaderProgram = ShaderHelper.loadShader(SHADER_VERT, SHADER_FRAG, "inPosition");
        if (shaderProgram == 0) return;

        loc_ModelViewMatrix = GL20.glGetUniformLocation(shaderProgram, "u_ModelViewMatrix");
        loc_ProjectionMatrix = GL20.glGetUniformLocation(shaderProgram, "u_ProjectionMatrix");
        loc_DepthTexture = GL20.glGetUniformLocation(shaderProgram, "u_DepthTexture");
        loc_ScreenSize = GL20.glGetUniformLocation(shaderProgram, "u_ScreenSize");
        loc_Near = GL20.glGetUniformLocation(shaderProgram, "u_Near");
        loc_Far = GL20.glGetUniformLocation(shaderProgram, "u_Far");
        loc_SphereColor = GL20.glGetUniformLocation(shaderProgram, "u_SphereColor");
        loc_IntersectionColor = GL20.glGetUniformLocation(shaderProgram, "u_IntersectionColor");
        loc_IntersectionWidth = GL20.glGetUniformLocation(shaderProgram, "u_IntersectionWidth");

        depthTextureId = GL11.glGenTextures();
    }

    private void captureSceneDepth() {
        Minecraft mc = Minecraft.getMinecraft();
        int w = mc.displayWidth;
        int h = mc.displayHeight;
        
        if (w <= 0 || h <= 0) return;

        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTextureId);

        if (w != lastScreenWidth || h != lastScreenHeight) {
            lastScreenWidth = w;
            lastScreenHeight = h;
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_DEPTH_COMPONENT32F,
                w, h, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (FloatBuffer) null);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        }

        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, w, h);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
    }

    private void cleanupShaderResources() {
        if (shaderProgram != 0) {
            try {
                ShaderHelper.deleteProgram(shaderProgram);
            } catch (Exception ignored) {
            }
            shaderProgram = 0;
        }
        if (depthTextureId != 0) {
            try {
                GL11.glDeleteTextures(depthTextureId);
            } catch (Exception ignored) {
            }
            depthTextureId = 0;
        }
        shaderInitialized = false;
    }
}
