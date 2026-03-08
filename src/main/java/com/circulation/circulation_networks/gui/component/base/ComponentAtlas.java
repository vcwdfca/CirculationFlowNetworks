package com.circulation.circulation_networks.gui.component.base;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.zip.CRC32;

/**
 * Manages the single texture atlas used by all {@link Component} subclass sprites.
 *
 * <h3>Lifecycle</h3>
 * <ol>
 *   <li>{@link #startAsync(File)} — called during {@code preInit} on the main thread.
 *       Discovers sprites from every jar on the classpath, loads their bytes via the
 *       Minecraft {@code ResourceManager}, then stitches them into a single
 *       {@link BufferedImage} on a background thread. The result is cached under
 *       {@code modConfigDir/atlas_<hash>.png}.</li>
 *   <li>{@link #awaitReady()} — must be called on the GL thread before rendering.
 *       Blocks until stitching is complete, then uploads the image to a GL texture.</li>
 *   <li>{@link #getRegion(String)} — looks up an {@link AtlasRegion} by sprite
 *       base-name (without extension).</li>
 * </ol>
 *
 * <h3>Multi-mod support</h3>
 * Other mods may contribute sprites by placing {@code .png} files in
 * {@code assets/circulation_networks/textures/gui/component/} inside their own jar.
 * The classpath is scanned exhaustively via {@link ClassLoader#getResources(String)},
 * so all jars are visited. Duplicate names are de-duplicated; the winning pixel data
 * is determined by the active {@code ResourceManager} stack.
 *
 * <h3>Atlas layout</h3>
 * Sprites are shelf-packed largest-first into the smallest power-of-two square that
 * fits, with a {@value #PADDING}-pixel border between sprites.
 */
@SideOnly(Side.CLIENT)
public final class ComponentAtlas {

    public static final ComponentAtlas INSTANCE = new ComponentAtlas();

    private static final String COMPONENT_DIR = "textures/gui/component/";
    private static final String BACKGROUND_DIR = "textures/gui/background/";
    static final String BG_PREFIX = "bg/";
    private static final String DOMAIN = CirculationFlowNetworks.MOD_ID;
    private static final int MIN_SIZE = 256;
    private static final int MAX_SIZE = 8192;
    private static final int PADDING = 1;

    private final Object2ObjectOpenHashMap<String, AtlasRegion> regions = new Object2ObjectOpenHashMap<>();
    private final Set<String> registeredSprites = new ObjectLinkedOpenHashSet<>();
    private final Set<String> registeredBackgrounds = new ObjectLinkedOpenHashSet<>();

    private CompletableFuture<StitchResult> future;
    private int glTextureId = 0;
    private File cacheDir;

    private ComponentAtlas() {
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Shelf-packs sprites into the smallest power-of-two square atlas that fits.
     */
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

    /**
     * Recomputes region coordinates from a cached atlas image.
     * The layout is deterministic given the name-sorted sprite list.
     */
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

    /**
     * Starts the background stitching task. Call once during
     * {@code FMLPreInitializationEvent} and again (via {@link #restart()}) after
     * each resource-pack reload.
     *
     * <p>Sprite names are taken exclusively from names registered via
     * {@code ResourceManager} so resource-pack overrides are honored.
     * Background sprites (registered via
     * {@link RegisterComponentSpritesEvent#registerBackground}) are loaded from
     * {@value #BACKGROUND_DIR} and stored in the same atlas under the
     * {@value #BG_PREFIX} name prefix.
     * Decoding and stitching run on a background thread.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void startAsync(File modConfigDir) {
        MinecraftForge.EVENT_BUS.post(new RegisterComponentSpritesEvent());

        cacheDir = modConfigDir;
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        Minecraft mc = Minecraft.getMinecraft();
        String[] names = registeredSpriteNames();
        String[] bgNames = registeredBackgroundNames();

        List<RawSprite> rawSprites = new ObjectArrayList<>();
        for (String bgName : bgNames) {
            ResourceLocation loc = new ResourceLocation(DOMAIN, BACKGROUND_DIR + bgName + ".png");
            try (InputStream is = mc.getResourceManager().getResource(loc).getInputStream()) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] chunk = new byte[8192];
                int n;
                while ((n = is.read(chunk)) != -1) baos.write(chunk, 0, n);
                rawSprites.add(new RawSprite(BG_PREFIX + bgName, baos.toByteArray()));
            } catch (Exception e) {
                CirculationFlowNetworks.LOGGER.warn(
                    "[ComponentAtlas] Could not load background '{}': {}", bgName, e.getMessage());
            }
        }
        for (String name : names) {
            ResourceLocation loc = new ResourceLocation(DOMAIN, COMPONENT_DIR + name + ".png");
            try (InputStream is = mc.getResourceManager().getResource(loc).getInputStream()) {
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
     * Call from {@code TextureStitchEvent.Post}.
     */
    public void restart() {
        if (cacheDir != null) {
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
            regions.clear();
            for (AtlasRegion r : result.regions) {
                regions.put(r.name, r);
            }
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

    /**
     * Returns the {@link AtlasRegion} for the given sprite base-name, or {@code null}
     * if not present. Populated after the first successful {@link #awaitReady()} call.
     */
    @Nullable
    public AtlasRegion getRegion(String name) {
        return regions.get(name);
    }

    // ── GL helpers ────────────────────────────────────────────────────────────

    /**
     * Releases the GL texture and resets all state. Call on resource-manager reload or shutdown.
     */
    public void dispose() {
        if (glTextureId != 0) {
            GL11.glDeleteTextures(glTextureId);
            glTextureId = 0;
        }
        regions.clear();
        future = null;
    }

    /**
     * Called exclusively by {@link RegisterComponentSpritesEvent#register}.
     */
    void addSprite(String name) {
        registeredSprites.add(name);
    }

    /**
     * Registers a background sprite to be stitched from {@value #BACKGROUND_DIR}.
     * Called exclusively by {@link RegisterComponentSpritesEvent#registerBackground}.
     */
    void addBackground(String name) {
        registeredBackgrounds.add(name);
    }

    /**
     * Returns the {@link AtlasRegion} for the given background base-name, or
     * {@code null} if not present. Background names are stored internally with
     * the {@value #BG_PREFIX} prefix; this method handles the lookup transparently.
     *
     * <p>Populated after the first successful {@link #awaitReady()} call.
     */
    @Nullable
    public AtlasRegion getBackground(String name) {
        return regions.get(BG_PREFIX + name);
    }

    // ── Hashing ───────────────────────────────────────────────────────────────

    private String[] registeredSpriteNames() {
        return registeredSprites.toArray(new String[0]);
    }

    private String[] registeredBackgroundNames() {
        return registeredBackgrounds.toArray(new String[0]);
    }

    // ── Inner types ───────────────────────────────────────────────────────────

    /**
     * PNG bytes loaded from {@code ResourceManager} on the main thread.
     */
    private static final class RawSprite {
        final String name;
        final byte[] bytes;

        RawSprite(String name, byte[] bytes) {
            this.name = name;
            this.bytes = bytes;
        }
    }

    /**
     * Decoded sprite image ready for stitching.
     */
    private static final class SpriteData {
        final String name;
        final BufferedImage image;

        SpriteData(String name, BufferedImage image) {
            this.name = name;
            this.image = image;
        }
    }

    /**
     * Result produced by the background stitching thread.
     */
    static final class StitchResult {
        static final StitchResult EMPTY = new StitchResult(null, Collections.emptyList());

        final BufferedImage image;
        final List<AtlasRegion> regions;

        StitchResult(BufferedImage image, List<AtlasRegion> regions) {
            this.image = image;
            this.regions = regions;
        }
    }
}
