package com.circulation.circulation_networks.handlers;

import com.circulation.circulation_networks.items.InspectionToolModeModel.InspectionMode;
import com.circulation.circulation_networks.items.InspectionToolModeModel.ToolFunction;
import com.circulation.circulation_networks.items.InspectionToolState;
import com.circulation.circulation_networks.math.Vec3d;
import com.circulation.circulation_networks.registry.CFNItems;
import com.circulation.circulation_networks.utils.RenderingUtils;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.client.Minecraft;
//? if <1.20 {
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
//?} else if <1.21 {
/*import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
*///?} else {
/*import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
*///?}
//? if <1.20 {
import com.github.bsideup.jabel.Desugar;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderWorldLastEvent;
//?} else {
/*import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
*///?}
//? if >=1.20 {
/*//~ neo_imports
import net.minecraftforge.client.event.RenderLevelStageEvent;
*///?}
import org.lwjgl.opengl.GL11;

//~ if >=1.20 '@SideOnly(Side.CLIENT)' -> '@OnlyIn(Dist.CLIENT)' {
@SideOnly(Side.CLIENT)
//~}
public final class NodeNetworkRenderingHandler {

    public static final NodeNetworkRenderingHandler INSTANCE = new NodeNetworkRenderingHandler();

    private static final float CORE_RADIUS = 0.04f;
    private static final float GLOW_RADIUS = 0.10f;

    private static final float SPHERE_CORE_RADIUS = 0.12f;
    private static final float SPHERE_GLOW_RADIUS = 0.28f;
    private static int sphereDisplayList = -1;
    private final ObjectSet<Line> nodeLinks = new ObjectLinkedOpenHashSet<>();
    private final ObjectSet<Line> machineLinks = new ObjectLinkedOpenHashSet<>();
    private final Multiset<Pos> nodePoss = HashMultiset.create();
    private final Multiset<Pos> machinePoss = HashMultiset.create();

    private static void ensureSphereDisplayList() {
        if (sphereDisplayList >= 0) return;
        sphereDisplayList = GL11.glGenLists(1);
        GL11.glNewList(sphereDisplayList, GL11.GL_COMPILE);
        final int slices = 32, stacks = 32;
        for (int i = 0; i < slices; i++) {
            double phi1 = Math.PI * i / slices;
            double phi2 = Math.PI * (i + 1) / slices;
            GL11.glBegin(GL11.GL_QUAD_STRIP);
            for (int j = 0; j <= stacks; j++) {
                double theta = 2.0 * Math.PI * j / stacks;
                GL11.glVertex3f(
                    (float) (Math.sin(phi1) * Math.cos(theta)),
                    (float) Math.cos(phi1),
                    (float) (Math.sin(phi1) * Math.sin(theta))
                );
                GL11.glVertex3f(
                    (float) (Math.sin(phi2) * Math.cos(theta)),
                    (float) Math.cos(phi2),
                    (float) (Math.sin(phi2) * Math.sin(theta))
                );
            }
            GL11.glEnd();
        }
        GL11.glEndList();
    }

    private static void drawSphere(float r, float g, float b, float radius, float alpha) {
        ensureSphereDisplayList();
        //? if <1.20 {
        GlStateManager.color(r, g, b, alpha);
        GlStateManager.pushMatrix();
        GlStateManager.scale(radius, radius, radius);
        //?} else if <1.21 {
        /*RenderSystem.setShaderColor(r, g, b, alpha);
        RenderSystem.getModelViewStack().pushPose();
        RenderSystem.getModelViewStack().scale(radius, radius, radius);
        RenderSystem.applyModelViewMatrix();
        *///?} else {
        /*RenderSystem.setShaderColor(r, g, b, alpha);
        RenderSystem.getModelViewStack().pushMatrix();
        RenderSystem.getModelViewStack().scale(radius, radius, radius);
        *///?}
        GL11.glCallList(sphereDisplayList);
        //? if <1.20 {
        GlStateManager.popMatrix();
        //?} else if <1.21 {
        /*RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();
        *///?} else {
        /*RenderSystem.getModelViewStack().popMatrix();
         *///?}
    }

    public void addNodeLink(long a, long b) {
        var l = Line.create(a, b);
        nodeLinks.add(l);
        nodePoss.add(l.from);
        nodePoss.add(l.to);
    }

    public void addMachineLink(long a, long b) {
        var l = Line.create(a, b);
        machineLinks.add(l);
        machinePoss.add(l.from);
        machinePoss.add(l.to);
    }

    public void removeNodeLink(long a, long b) {
        var l = Line.create(a, b);
        nodeLinks.remove(l);
        nodePoss.remove(l.from);
        nodePoss.remove(l.to);
    }

    public void removeMachineLink(long a, long b) {
        var l = Line.create(a, b);
        machineLinks.remove(l);
        machinePoss.remove(l.from);
        machinePoss.remove(l.to);
    }

    public void clearLinks() {
        nodeLinks.clear();
        machineLinks.clear();
        nodePoss.clear();
        machinePoss.clear();
    }

    @SubscribeEvent
        //? if <1.20 {
    public void renderWorldLastEvent(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP p = mc.player;
        //?} else {
    /*public void renderWorldLastEvent(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
    *///?}

        //? if <1.20 {
        var stack = p.getHeldItemMainhand();
        //?} else {
        /*var stack = p.getMainHandItem();
         *///?}
        if (!(stack.getItem() == CFNItems.inspectionTool
            && InspectionToolState.getFunction(stack) == ToolFunction.INSPECTION
            && InspectionMode.fromID(InspectionToolState.getSubMode(stack)).isLinkMode()))
            return;

        InspectionMode currentMode = InspectionMode.fromID(InspectionToolState.getSubMode(stack));
        boolean showNodes = currentMode.showNodeLinks();
        boolean showMachines = currentMode.showMachineLinks();

        //? if <1.20 {
        double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * event.getPartialTicks();
        double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * event.getPartialTicks();
        double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * event.getPartialTicks();
        //?} else if <1.21 {
        /*double doubleX = p.xOld + (p.getX() - p.xOld) * event.getPartialTick();
        double doubleY = p.yOld + (p.getY() - p.yOld) * event.getPartialTick();
        double doubleZ = p.zOld + (p.getZ() - p.zOld) * event.getPartialTick();
        *///?} else {
        /*float _partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        double doubleX = p.xOld + (p.getX() - p.xOld) * _partialTick;
        double doubleY = p.yOld + (p.getY() - p.yOld) * _partialTick;
        double doubleZ = p.zOld + (p.getZ() - p.zOld) * _partialTick;
        *///?}

        //? if <1.20 {
        GlStateManager.pushMatrix();
        GlStateManager.translate(-doubleX, -doubleY, -doubleZ);
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        //?} else if <1.21 {
        /*PoseStack mvStack = RenderSystem.getModelViewStack();
        mvStack.pushPose();
        mvStack.translate(-doubleX, -doubleY, -doubleZ);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        *///?} else {
        /*var mvStack = RenderSystem.getModelViewStack();
        mvStack.pushMatrix();
        mvStack.translate((float) -doubleX, (float) -doubleY, (float) -doubleZ);
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        *///?}

        if (showNodes) {
            for (var link : nodeLinks) {
                RenderingUtils.drawLaserCylinder(link.from.x, link.from.y, link.from.z, link.to.x, link.to.y, link.to.z, GLOW_RADIUS, 0.3f, 0.3f, 1.0f, 0.25f);
                RenderingUtils.drawLaserCylinder(link.from.x, link.from.y, link.from.z, link.to.x, link.to.y, link.to.z, CORE_RADIUS, 0.3f, 0.3f, 1.0f, 1.0f);
            }
        }
        if (showMachines) {
            for (var link : machineLinks) {
                RenderingUtils.drawLaserCylinder(link.from.x, link.from.y, link.from.z, link.to.x, link.to.y, link.to.z, GLOW_RADIUS, 1.0f, 0.3f, 0.3f, 0.25f);
                RenderingUtils.drawLaserCylinder(link.from.x, link.from.y, link.from.z, link.to.x, link.to.y, link.to.z, CORE_RADIUS, 1.0f, 0.3f, 0.3f, 1.0f);
            }
        }

        if (showNodes) {
            for (var pos : nodePoss.elementSet()) {
                boolean alsoMachine = showMachines && machinePoss.contains(pos);
                //? if <1.20 {
                GlStateManager.pushMatrix();
                GlStateManager.translate(pos.x, pos.y, pos.z);
                //?} else if <1.21 {
                /*RenderSystem.getModelViewStack().pushPose();
                RenderSystem.getModelViewStack().translate(pos.x, pos.y, pos.z);
                RenderSystem.applyModelViewMatrix();
                *///?} else {
                /*RenderSystem.getModelViewStack().pushMatrix();
                RenderSystem.getModelViewStack().translate((float) pos.x, (float) pos.y, (float) pos.z);
                *///?}
                if (alsoMachine) {
                    drawSphere(1.0f, 0.0f, 1.0f, SPHERE_GLOW_RADIUS, 0.3f);
                    drawSphere(1.0f, 0.0f, 1.0f, SPHERE_CORE_RADIUS, 0.9f);
                } else {
                    drawSphere(0.0f, 0.0f, 1.0f, SPHERE_GLOW_RADIUS, 0.3f);
                    drawSphere(0.0f, 0.0f, 1.0f, SPHERE_CORE_RADIUS, 0.9f);
                }
                //? if <1.20 {
                GlStateManager.popMatrix();
                //?} else if <1.21 {
                /*RenderSystem.getModelViewStack().popPose();
                RenderSystem.applyModelViewMatrix();
                *///?} else {
                /*RenderSystem.getModelViewStack().popMatrix();
                 *///?}
            }
        }
        if (showMachines) {
            for (var pos : machinePoss.elementSet()) {
                if (showNodes && nodePoss.contains(pos)) continue;
                //? if <1.20 {
                GlStateManager.pushMatrix();
                GlStateManager.translate(pos.x, pos.y, pos.z);
                //?} else if <1.21 {
                /*RenderSystem.getModelViewStack().pushPose();
                RenderSystem.getModelViewStack().translate(pos.x, pos.y, pos.z);
                RenderSystem.applyModelViewMatrix();
                *///?} else {
                /*RenderSystem.getModelViewStack().pushMatrix();
                RenderSystem.getModelViewStack().translate((float) pos.x, (float) pos.y, (float) pos.z);
                *///?}
                drawSphere(1.0f, 0.0f, 0.0f, SPHERE_GLOW_RADIUS, 0.3f);
                drawSphere(1.0f, 0.0f, 0.0f, SPHERE_CORE_RADIUS, 0.9f);
                //? if <1.20 {
                GlStateManager.popMatrix();
                //?} else if <1.21 {
                /*RenderSystem.getModelViewStack().popPose();
                RenderSystem.applyModelViewMatrix();
                *///?} else {
                /*RenderSystem.getModelViewStack().popMatrix();
                 *///?}
            }
        }

        //? if <1.20 {
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        //?} else if <1.21 {
        /*RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();
        *///?} else {
        /*RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.getModelViewStack().popMatrix();
        *///?}
    }

    //? if <1.20 {
    @Desugar
        //?}
    private record Line(Pos from, Pos to, int hash) {

        private static Line create(long from, long to) {
            var fromP = Pos.fromLong(from);
            var toP = Pos.fromLong(to);
            int h1 = fromP.hashCode();
            int h2 = toP.hashCode();
            int mixedHash = (h1 < h2) ? (31 * h1 + h2) : (31 * h2 + h1);
            return new Line(fromP, toP, mixedHash);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Line line = (Line) o;
            if (this.hash != line.hash) return false;
            return (from.equals(line.from) && to.equals(line.to)) || (from.equals(line.to) && to.equals(line.from));
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    private static class Pos extends Vec3d {

        //? if <1.20 {
        private static final int NUM_X_BITS = 1 + MathHelper.log2(MathHelper.smallestEncompassingPowerOfTwo(30000000));
        //?} else {
        /*private static final int NUM_X_BITS = 1 + Mth.log2(Mth.smallestEncompassingPowerOfTwo(30000000));
         *///?}
        private static final int NUM_Z_BITS = NUM_X_BITS;
        private static final int NUM_Y_BITS = 64 - NUM_X_BITS - NUM_Z_BITS;
        private static final int Y_SHIFT = NUM_Z_BITS;
        private static final int X_SHIFT = Y_SHIFT + NUM_Y_BITS;
        private final int hash;

        public Pos(int xIn, int yIn, int zIn) {
            this(xIn + 0.5, yIn + 0.5, zIn + 0.5);
        }

        public Pos(double xIn, double yIn, double zIn) {
            super(xIn, yIn, zIn);
            hash = super.hashCode();
        }

        public static Pos fromLong(long serialized) {
            int i = (int) (serialized << 64 - X_SHIFT - NUM_X_BITS >> 64 - NUM_X_BITS);
            int j = (int) (serialized << 64 - Y_SHIFT - NUM_Y_BITS >> 64 - NUM_Y_BITS);
            int k = (int) (serialized << 64 - NUM_Z_BITS >> 64 - NUM_Z_BITS);
            return new Pos(i, j, k);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            var pos = (Pos) o;
            return this.x == pos.x && this.y == pos.y && this.z == pos.z;
        }
    }
}
