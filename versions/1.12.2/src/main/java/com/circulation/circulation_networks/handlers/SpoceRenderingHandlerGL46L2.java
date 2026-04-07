package com.circulation.circulation_networks.handlers;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL44;

import java.nio.FloatBuffer;

@SideOnly(Side.CLIENT)
public class SpoceRenderingHandlerGL46L2 extends SpoceRenderingHandlerGL32L2 {

    @Override
    protected void initGL() {
        FloatBuffer sd = buildSphereData();
        sphereVertexCount = sd.limit() / 3;
        sphereVBO = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, sphereVBO);
        GL44.glBufferStorage(GL15.GL_ARRAY_BUFFER, sd, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        sphereVAO = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(sphereVAO);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, sphereVBO);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0L);
        GL20.glEnableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        FloatBuffer bd = buildBuckyData();
        buckyVertexCount = bd.limit() / 3;
        buckyVBO = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, buckyVBO);
        GL44.glBufferStorage(GL15.GL_ARRAY_BUFFER, bd, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        buckyVAO = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(buckyVAO);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, buckyVBO);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0L);
        GL20.glEnableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }
}
