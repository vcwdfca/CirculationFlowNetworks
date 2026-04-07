package com.circulation.circulation_networks.handlers;

import com.circulation.circulation_networks.math.Vec3d;
import com.circulation.circulation_networks.utils.BuckyBallGeometry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL45;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

@SideOnly(Side.CLIENT)
public class SpoceRenderingHandlerGL46L3 extends SpoceRenderingHandlerGL32L2 {

    private static FloatBuffer buildSphereDataDirect() {
        final int slices = 32, stacks = 32;
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

    private static FloatBuffer buildBuckyDataDirect() {
        FloatBuffer buf = MemoryUtil.memAllocFloat(BuckyBallGeometry.edges.size() * 2 * 3);
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
        FloatBuffer sd = buildSphereDataDirect();
        try {
            sphereVertexCount = sd.limit() / 3;
            sphereVBO = GL45.glCreateBuffers();
            GL45.glNamedBufferStorage(sphereVBO, sd, 0);
        } finally {
            MemoryUtil.memFree(sd);
        }
        sphereVAO = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(sphereVAO);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, sphereVBO);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0L);
        GL20.glEnableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        FloatBuffer bd = buildBuckyDataDirect();
        try {
            buckyVertexCount = bd.limit() / 3;
            buckyVBO = GL45.glCreateBuffers();
            GL45.glNamedBufferStorage(buckyVBO, bd, 0);
        } finally {
            MemoryUtil.memFree(bd);
        }
        buckyVAO = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(buckyVAO);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, buckyVBO);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0L);
        GL20.glEnableVertexAttribArray(0);
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }
}
