package com.circulation.circulation_networks.gui.component.base;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
//? if <1.20 {
import com.github.bsideup.jabel.Desugar;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
//?} else if <1.21 {
/*import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
*///?} else {
/*import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
*///?}
//? if <1.21 {
import net.minecraftforge.common.MinecraftForge;
//?} else {
/*import net.neoforged.neoforge.common.NeoForge;
*///?}
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.zip.CRC32;

/**
 * Manages the single texture atlas used by all {@link Component} subclass sprites.
 *
 * <h3>Lifecycle</h3>
 * <ol>
 *   <li>{@link #startAsync(File)} — called during client init on the main thread.
 *       Discovers sprites via the Minecraft {@code ResourceManager}, then stitches them
 *       into a single {@link BufferedImage} on a background thread. The result is cached
 *       under {@code modConfigDir/atlas_<hash>.png}.</li>
 *   <li>{@link #awaitReady()} — must be called on the GL thread before rendering.
 *       Blocks until stitching is complete, then uploads the image to a GL texture.</li>
 *   <li>{@link #getRegion(String)} — looks up an {@link AtlasRegion} by sprite
 *       base-name (without extension).</li>
 * </ol>
 */
//? if <1.20 {
@SideOnly(Side.CLIENT)
//?} else {
/*@OnlyIn(Dist.CLIENT)
*///?}
public final class ComponentAtlas extends ComponentAtlasRegistry {

    public static final ComponentAtlas INSTANCE = new ComponentAtlas();

    private static final String COMPONENT_DIR = "textures/gui/component/";
    private static final String BACKGROUND_DIR = "textures/gui/background/";
    private static final String DOMAIN = CirculationFlowNetworks.MOD_ID;
    private static final int MIN_SIZE = 256;
    private static final int MAX_SIZE = 8192;
    private static final int PADDING = 1;

    private CompletableFuture<StitchResult> future;
    private int glTextureId = 0;
    private File cacheDir;

    private ComponentAtlas() {
    }

    // ── Public API ────────────────────────────────────────────────────────────

    private static StitchResult stitch(List<SpriteData> sprites) {
        List<SpriteData> sorted = new ObjectArrayList<>(sprites);
        sorted.sort(Comparator.comparingInt((SpriteData s) -> s.image.getWidth() * s.image.getHeight())
                              .reversed());

        for (int size = MIN_SIZE; size <= MAX_SIZE; size <<= 1) {
            StitchResult result = tryPack(sorted, size);
            if (result != null) {
                CirculationFlowNetworks.LOGGER.info(
                    "[ComponentAtlas] Stitched {} sprites into {}×{} atlas.",
                    sprites.size(), size, size);
                return result;
            }
        }

        CirculationFlowNetworks.LOGGER.error(
            "[ComponentAtlas] Sprites exceed maximum atlas size {}×{}! Some may be missing.",
            MAX_SIZE, MAX_SIZE);
        return tryPackForceFit(sorted, MAX_SIZE);
    }

    @Nullable
    private static StitchResult tryPack(List<SpriteData> sprites, int size) {
        BufferedImage atlas = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = atlas.createGraphics();
        g.setBackground(new Color(0, 0, 0, 0));
        g.clearRect(0, 0, size, size);

        List<AtlasRegion> regions = new ObjectArrayList<>();
        int shelfX = PADDING, shelfY = PADDING, shelfHeight = 0;

        for (SpriteData sprite : sprites) {
            int sw = sprite.image.getWidth();
            int sh = sprite.image.getHeight();

            if (sw + PADDING * 2 > size || sh + PADDING * 2 > size) {
                g.dispose();
                return null;
            }
            if (shelfX + sw + PADDING > size) {
                shelfX = PADDING;
                shelfY += shelfHeight + PADDING;
                shelfHeight = 0;
            }
            if (shelfY + sh + PADDING > size) {
                g.dispose();
                return null;
            }

            g.drawImage(sprite.image, shelfX, shelfY, null);
            regions.add(new AtlasRegion(sprite.name, shelfX, shelfY, sw, sh, size));
            shelfHeight = Math.max(shelfHeight, sh);
            shelfX += sw + PADDING;
        }

        g.dispose();
        return new StitchResult(atlas, regions);
    }

    private static StitchResult tryPackForceFit(List<SpriteData> sprites, int size) {
        BufferedImage atlas = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = atlas.createGraphics();
        g.setBackground(new Color(0, 0, 0, 0));
        g.clearRect(0, 0, size, size);

        List<AtlasRegion> regions = new ObjectArrayList<>();
        int shelfX = PADDING, shelfY = PADDING, shelfHeight = 0;

        for (SpriteData sprite : sprites) {
            int sw = sprite.image.getWidth();
            int sh = sprite.image.getHeight();

            if (shelfX + sw + PADDING > size) {
                shelfX = PADDING;
                shelfY += shelfHeight + PADDING;
                shelfHeight = 0;
            }
            if (shelfY + sh + PADDING > size) {
                CirculationFlowNetworks.LOGGER.warn(
                    "[ComponentAtlas] Sprite '{}' ({}×{}) does not fit — skipped.",
                    sprite.name, sw, sh);
                continue;
            }

            g.drawImage(sprite.image, shelfX, shelfY, null);
            regions.add(new AtlasRegion(sprite.name, shelfX, shelfY, sw, sh, size));
            shelfHeight = Math.max(shelfHeight, sh);
            shelfX += sw + PADDING;
        }

        g.dispose();
        return new StitchResult(atlas, regions);
    }

    private static StitchResult buildRegions(List<SpriteData> sprites, BufferedImage cachedImage) {
        int size = cachedImage.getWidth();
        List<SpriteData> sorted = new ObjectArrayList<>(sprites);
        sorted.sort(Comparator.comparingInt((SpriteData s) -> s.image.getWidth() * s.image.getHeight())
                              .reversed());

        List<AtlasRegion> regions = new ObjectArrayList<>();
        int shelfX = PADDING, shelfY = PADDING, shelfHeight = 0;

        for (SpriteData sprite : sorted) {
            int sw = sprite.image.getWidth();
            int sh = sprite.image.getHeight();

            if (shelfX + sw + PADDING > size) {
                shelfX = PADDING;
                shelfY += shelfHeight + PADDING;
                shelfHeight = 0;
            }
            if (shelfY + sh + PADDING > size) break;

            regions.add(new AtlasRegion(sprite.name, shelfX, shelfY, sw, sh, size));
            shelfHeight = Math.max(shelfHeight, sh);
            shelfX += sw + PADDING;
        }

        return new StitchResult(cachedImage, regions);
    }

    private static int uploadImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        ByteBuffer buf = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder());
        for (int pixel : pixels) {
            buf.put((byte) ((pixel >> 16) & 0xFF));
            buf.put((byte) ((pixel >> 8) & 0xFF));
            buf.put((byte) (pixel & 0xFF));
            buf.put((byte) ((pixel >> 24) & 0xFF));
        }
        buf.flip();

        int texId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height,
            0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        CirculationFlowNetworks.LOGGER.info(
            "[ComponentAtlas] Uploaded atlas {}×{} to GL texture #{}", width, height, texId);
        return texId;
    }

    private static BufferedImage createFallback() {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, 0xFFFFFFFF);
        return img;
    }

    private static String computeHash(List<SpriteData> sortedSprites) {
        CRC32 crc = new CRC32();
        for (SpriteData s : sortedSprites) {
            crc.update(s.name.getBytes(StandardCharsets.UTF_8));
            int w = s.image.getWidth();
            int h = s.image.getHeight();
            int[] pixels = new int[w * h];
            s.image.getRGB(0, 0, w, h, pixels, 0, w);
            ByteBuffer buf = ByteBuffer.allocate(pixels.length * 4);
            for (int p : pixels) buf.putInt(p);
            crc.update(buf.array());
        }
        return String.format("%016x", crc.getValue());
    }

    // ── Discovery ─────────────────────────────────────────────────────────────

    private boolean init;

    @SubscribeEvent
    public void onRegisterSprites(RegisterComponentSpritesEvent event) {
        event.register("inventory");
        event.register("slot");

        event.registerBackground("gui_center");
    }

    /**
     * Starts the background stitching task. Call once during client init and again
     * (via {@link #restart()}) after each resource-pack reload.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void startAsync(File modConfigDir) {
        if (init) return;
        init = true;
        RegisterComponentSpritesEvent event = new RegisterComponentSpritesEvent();
        //? if <1.21 {
        MinecraftForge.EVENT_BUS.post(event);
        //?} else {
        /*NeoForge.EVENT_BUS.post(event);
         *///?}
        for (String name : event.getSprites()) addSprite(name);
        for (String name : event.getBackgrounds()) addBackground(name);

        cacheDir = modConfigDir;
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        //? if <1.20 {
        Minecraft mc = Minecraft.getMinecraft();
        //?} else {
        /*Minecraft mc = Minecraft.getInstance();
         *///?}
        String[] names = registeredSpriteNames();
        String[] bgNames = registeredBackgroundNames();

        List<RawSprite> rawSprites = new ObjectArrayList<>();
        for (String bgName : bgNames) {
            //? if <1.20 {
            ResourceLocation loc = new ResourceLocation(DOMAIN, BACKGROUND_DIR + bgName + ".png");
            try (InputStream is = mc.getResourceManager().getResource(loc).getInputStream()) {
                //?} else {
            /*ResourceLocation loc =
            //? if <1.21 {
            new ResourceLocation(DOMAIN, BACKGROUND_DIR + bgName + ".png");
            //?} else {
            ResourceLocation.fromNamespaceAndPath(DOMAIN, BACKGROUND_DIR + bgName + ".png");
            //?}
            try (InputStream is = mc.getResourceManager().getResourceOrThrow(loc).open()) {
            *///?}
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] chunk = new byte[8192];
                int n;
                while ((n = is.read(chunk)) != -1) baos.write(chunk, 0, n);
                rawSprites.add(new RawSprite(backgroundName(bgName), baos.toByteArray()));
            } catch (Exception e) {
                CirculationFlowNetworks.LOGGER.warn(
                    "[ComponentAtlas] Could not load background '{}': {}", bgName, e.getMessage());
            }
        }
        for (String name : names) {
            //? if <1.20 {
            ResourceLocation loc = new ResourceLocation(DOMAIN, COMPONENT_DIR + name + ".png");
            try (InputStream is = mc.getResourceManager().getResource(loc).getInputStream()) {
                //?} else {
            /*ResourceLocation loc =
            //? if <1.21 {
            new ResourceLocation(DOMAIN, COMPONENT_DIR + name + ".png");
            //?} else {
            ResourceLocation.fromNamespaceAndPath(DOMAIN, COMPONENT_DIR + name + ".png");
            //?}
            try (InputStream is = mc.getResourceManager().getResourceOrThrow(loc).open()) {
            *///?}
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] chunk = new byte[8192];
                int n;
                while ((n = is.read(chunk)) != -1) baos.write(chunk, 0, n);
                rawSprites.add(new RawSprite(name, baos.toByteArray()));
            } catch (Exception e) {
                CirculationFlowNetworks.LOGGER.warn(
                    "[ComponentAtlas] Could not load sprite '{}': {}", name, e.getMessage());
            }
        }

        if (rawSprites.isEmpty()) {
            CirculationFlowNetworks.LOGGER.warn("[ComponentAtlas] No sprites found in resource manager!");
            future = CompletableFuture.completedFuture(StitchResult.EMPTY);
            return;
        }

        future = CompletableFuture.supplyAsync(() -> {
            try {
                List<SpriteData> sprites = new ObjectArrayList<>();
                for (RawSprite raw : rawSprites) {
                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(raw.bytes));
                    if (img != null) sprites.add(new SpriteData(raw.name, img));
                }
                if (sprites.isEmpty()) return StitchResult.EMPTY;

                sprites.sort(Comparator.comparing(s -> s.name));

                String hash = computeHash(sprites);
                File cacheFile = new File(cacheDir, "atlas_" + hash + ".png");

                if (cacheFile.exists()) {
                    BufferedImage cached = ImageIO.read(cacheFile);
                    if (cached != null) {
                        CirculationFlowNetworks.LOGGER.info(
                            "[ComponentAtlas] Atlas cache hit (hash {})", hash);
                        return buildRegions(sprites, cached);
                    }
                }

                CirculationFlowNetworks.LOGGER.info(
                    "[ComponentAtlas] Stitching atlas for {} sprites (hash {})…",
                    sprites.size(), hash);
                StitchResult result = stitch(sprites);

                File[] old = cacheDir.listFiles(f ->
                    f.isFile() && f.getName().startsWith("atlas_") && f.getName().endsWith(".png"));
                if (old != null) {
                    for (File f : old) f.delete();
                }
                try {
                    ImageIO.write(result.image, "PNG", cacheFile);
                    CirculationFlowNetworks.LOGGER.info(
                        "[ComponentAtlas] Atlas cached at {}", cacheFile.getAbsolutePath());
                } catch (IOException e) {
                    CirculationFlowNetworks.LOGGER.warn(
                        "[ComponentAtlas] Could not write atlas cache: {}", e.getMessage());
                }
                return result;

            } catch (Exception e) {
                CirculationFlowNetworks.LOGGER.error("[ComponentAtlas] Stitching failed", e);
                return StitchResult.EMPTY;
            }
        });
    }

    /**
     * Rebuilds the atlas after a resource-pack reload.
     */
    public void restart() {
        if (cacheDir != null) {
            init = false;
            startAsync(cacheDir);
        }
    }

    // ── Stitching ─────────────────────────────────────────────────────────────

    /**
     * Blocks until the background task completes, then uploads the atlas to the GPU.
     * Must be called on the GL thread. Safe to call multiple times (no-op after first upload).
     */
    public void awaitReady() {
        if (future == null || glTextureId != 0) return;

        StitchResult result;
        try {
            result = future.join();
        } catch (Exception e) {
            CirculationFlowNetworks.LOGGER.error("[ComponentAtlas] Failed to obtain atlas", e);
            return;
        }

        if (result == StitchResult.EMPTY || result.image == null) {
            glTextureId = uploadImage(createFallback());
        } else {
            glTextureId = uploadImage(result.image);
            replaceRegions(result.regions);
        }
    }

    /**
     * Binds the atlas GL texture for subsequent draw calls. No-op if not yet uploaded.
     */
    public void bind() {
        if (glTextureId != 0) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, glTextureId);
        }
    }

    /**
     * Returns {@code true} once the atlas has been uploaded to the GPU.
     */
    public boolean isReady() {
        return glTextureId != 0;
    }

    // ── GL helpers ────────────────────────────────────────────────────────────

    /**
     * Releases the GL texture and resets all state.
     */
    public void dispose() {
        if (glTextureId != 0) {
            GL11.glDeleteTextures(glTextureId);
            glTextureId = 0;
        }
        clearRegisteredRegions();
        future = null;
    }

    // ── Inner types ───────────────────────────────────────────────────────────

    //? if =1.12.2 {
    @Desugar
        //?}
    private record RawSprite(String name, byte[] bytes) {
    }

    //? if =1.12.2 {
    @Desugar
        //?}
    private record SpriteData(String name, BufferedImage image) {
    }

    //? if =1.12.2 {
    @Desugar
        //?}
    record StitchResult(BufferedImage image, List<AtlasRegion> regions) {
        static final StitchResult EMPTY = new StitchResult(null, Collections.emptyList());
    }
}