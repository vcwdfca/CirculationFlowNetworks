package com.circulation.circulation_networks.handlers;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL45;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

@OnlyIn(Dist.CLIENT)
public class SpoceRenderingHandlerGL46L3 extends SpoceRenderingHandler {

    @Override
    protected void initGL() {
        FloatBuffer sd = buildSphereDataDirect();
        try {
            sphereVertexCount = sd.limit() / 3;
            sphereVBO = GL45.glCreateBuffers();
            GL45.glNamedBufferStorage(sphereVBO, sd, 0);
        } finally {
            MemoryUtil.memFree(sd);
        }
        sphereVAO = GL30.glGenVertexArrays();
        GlStateManager._glBindVertexArray(sphereVAO);
        GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, sphereVBO);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0L);
        GL20.glEnableVertexAttribArray(0);
        GlStateManager._glBindVertexArray(0);
        GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        FloatBuffer bd = buildBuckyDataDirect();
        try {
            buckyVertexCount = bd.limit() / 3;
            buckyVBO = GL45.glCreateBuffers();
            GL45.glNamedBufferStorage(buckyVBO, bd, 0);
        } finally {
            MemoryUtil.memFree(bd);
        }
        buckyVAO = GL30.glGenVertexArrays();
        GlStateManager._glBindVertexArray(buckyVAO);
        GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, buckyVBO);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0L);
        GL20.glEnableVertexAttribArray(0);
        GlStateManager._glBindVertexArray(0);
        GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }
}
