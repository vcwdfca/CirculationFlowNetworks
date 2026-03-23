package com.circulation.circulation_networks.gui.component.base;

import com.circulation.circulation_networks.container.ComponentSlotLayout;
import com.circulation.circulation_networks.gui.CFNBaseGui;
import com.circulation.circulation_networks.utils.CI18n;
import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings({"unused", "unchecked"})
public class Component extends Rectangle {

    private final List<Component> children = new ObjectArrayList<>();
    private final List<ComponentSlotLayout> boundLayouts = new ObjectArrayList<>();
    private final String[] EMPTY = new String[0];
    @Nonnull
    protected final CFNBaseGui<?> gui;
    protected boolean visible = true;
    protected boolean enabled = true;
    protected int zIndex = 0;
    protected boolean update;
    @Nullable
    private Component parent;
    private boolean hovered = false;

    private String[] spriteLayers = EMPTY;

    public Component(int x, int y, int width, int height, @Nonnull CFNBaseGui<?> gui) {
        super(x, y, width, height);
        this.gui = gui;
        update = true;
    }

    public List<Component> getChildren() {
        return children;
    }

    public boolean isVisible() {
        return parent != null ? parent.isVisible() && visible : visible;
    }

    public boolean isEnabled() {
        return parent != null ? parent.isEnabled() && enabled : enabled;
    }

    public Component setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public int getZIndex() {
        return zIndex;
    }

    public boolean isHovered() {
        return hovered;
    }

    protected static void renderAtlasSprite(String spriteName,
                                            int screenX, int screenY,
                                            int renderW, int renderH) {
        ComponentAtlas atlas = ComponentAtlas.INSTANCE;
        AtlasRegion region = atlas.getRegion(spriteName);
        if (region == null) return;
        AtlasRenderHelper.drawRegion(atlas, region, screenX, screenY, renderW, renderH);
    }

    public Component setX(int x) {
        this.x = x;
        invalidateSubtree();
        syncSlotTreePositions();
        return this;
    }

    public Component setY(int y) {
        this.y = y;
        invalidateSubtree();
        syncSlotTreePositions();
        return this;
    }

    public Component addChild(Component child) {
        children.add(child);
        children.sort(Comparator.comparingInt(c -> c.zIndex));
        child.parent = this;
        child.invalidateSubtree();
        child.syncSlotTreePositions();
        return child;
    }

    public Component addChild(Component... childs) {
        for (var child : childs) {
            child.parent = this;
            children.add(child);
            children.sort(Comparator.comparingInt(c -> c.zIndex));
            child.invalidateSubtree();
            child.syncSlotTreePositions();
        }
        return this;
    }

    public void removeChild(Component child) {
        if (children.remove(child)) {
            child.parent = null;
            child.invalidateSubtree();
            child.syncSlotTreePositions();
        }
    }

    public int getAbsoluteX() {
        return parent != null ? parent.getAbsoluteX() + x : gui.getGuiLeft() + x;
    }

    public int getAbsoluteY() {
        return parent != null ? parent.getAbsoluteY() + y : gui.getGuiTop() + y;
    }

    public boolean contains(int mouseX, int mouseY) {
        int w = this.width;
        int h = this.height;
        if ((w | h) < 0) {
            return false;
        }
        int x = this.getAbsoluteX();
        int y = this.getAbsoluteY();
        if (mouseX < x || mouseY < y) {
            return false;
        }
        w += x;
        h += y;
        return ((w < x || w > mouseX) &&
            (h < y || h > mouseY));
    }

    public boolean contains(int X, int Y, int W, int H) {
        int w = this.width;
        int h = this.height;
        if ((w | h | W | H) < 0) {
            return false;
        }
        int x = this.getAbsoluteX();
        int y = this.getAbsoluteY();
        if (X < x || Y < y) {
            return false;
        }
        w += x;
        W += X;
        if (W <= X) {
            if (w >= x || W > w) return false;
        } else {
            if (w >= x && W > w) return false;
        }
        h += y;
        H += Y;
        if (H <= Y) {
            return h < y && H <= h;
        } else {
            return h < y || H <= h;
        }
    }

    public final void renderComponent(int mouseX, int mouseY, float partialTicks) {
        syncSlotPositions();
        if (!isVisible()) return;

        boolean nowHovered = contains(mouseX, mouseY);
        if (nowHovered && !hovered) onMouseEnter();
        else if (!nowHovered && hovered) onMouseLeave();
        hovered = nowHovered;

        renderSpriteLayers();
        render(mouseX, mouseY, partialTicks);
        renderBoundLayouts(mouseX, mouseY);

        if (children.isEmpty()) return;
        for (Component child : children) {
            child.renderComponent(mouseX, mouseY, partialTicks);
        }
    }

    protected void render(int mouseX, int mouseY, float partialTicks) {

    }

    protected void renderBoundLayouts(int mouseX, int mouseY) {
        if (boundLayouts.isEmpty()) return;

        Minecraft mc = Minecraft.getMinecraft();
        RenderItem renderItem = mc.getRenderItem();
        int localMouseX = mouseX - gui.getGuiLeft();
        int localMouseY = mouseY - gui.getGuiTop();
        boolean topComponent = gui.isTopComponent(this, mouseX, mouseY);

        restoreGuiRenderState();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) gui.getGuiLeft(), (float) gui.getGuiTop(), 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        RenderHelper.enableGUIStandardItemLighting();

        for (ComponentSlotLayout layout : boundLayouts) {
            for (Slot slot : layout.getSlots()) {
                if (!slot.isEnabled()) continue;

                int sx = slot.xPos;
                int sy = slot.yPos;
                ItemStack stack = slot.getStack();

                float prevZLevel = renderItem.zLevel;
                renderItem.zLevel = prevZLevel;
                if (!stack.isEmpty()) {
                    renderItem.renderItemAndEffectIntoGUI(mc.player, stack, sx, sy);
                    renderItem.renderItemOverlayIntoGUI(mc.fontRenderer, stack, sx, sy, null);
                }
                renderItem.zLevel = prevZLevel;

                if (topComponent && isMouseOverSlot(localMouseX, localMouseY, sx, sy)) {
                    gui.setHoveredSlot(slot);
                    GlStateManager.disableLighting();
                    GlStateManager.disableDepth();
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(
                        org.lwjgl.opengl.GL11.GL_SRC_ALPHA,
                        org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA,
                        org.lwjgl.opengl.GL11.GL_ONE,
                        org.lwjgl.opengl.GL11.GL_ZERO
                    );
                    GlStateManager.colorMask(true, true, true, false);
                    Gui.drawRect(sx, sy, sx + 16, sy + 16, -2130706433);
                    GlStateManager.colorMask(true, true, true, true);
                    GlStateManager.enableBlend();
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                }

                restoreGuiRenderState();
                GlStateManager.enableRescaleNormal();
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
                GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
                RenderHelper.enableGUIStandardItemLighting();
            }
        }

        GlStateManager.popMatrix();
        restoreGuiRenderState();
    }

    protected final boolean isMouseOverSlot(int localMouseX, int localMouseY, int slotX, int slotY) {
        return localMouseX >= slotX && localMouseX < slotX + 16
            && localMouseY >= slotY && localMouseY < slotY + 16;
    }

    public final Component setSpriteLayers(String... layers) {
        this.spriteLayers = layers != null ? layers : EMPTY;
        return this;
    }

    protected String[] getActiveLayers() {
        return spriteLayers;
    }

    protected void renderSpriteLayers() {
        String[] layers = getActiveLayers();
        if (layers == null || layers.length == 0) return;

        ComponentAtlas atlas = ComponentAtlas.INSTANCE;
        if (!atlas.isReady()) return;

        restoreGuiRenderState();

        int ax = getAbsoluteX();
        int ay = getAbsoluteY();
        for (String name : layers) {
            if (name == null) continue;
            AtlasRegion region = atlas.getRegion(name);
            if (region == null) continue;
            AtlasRenderHelper.drawRegion(atlas, region, ax, ay, width, height);
        }
    }

    protected List<LocalizedComponent> tooltips = new ObjectArrayList<>();

    //? if <1.20{
    @Desugar
        //?}
    public record Composite(String key, Supplier<Object[]> supplier) implements LocalizedComponent {
        @Override
        public String get() {
            return CI18n.INSTANCE.format(key, supplier.get());
        }
    }

    public interface LocalizedComponent extends Supplier<String> {
        String get();
    }

    @NotNull
    protected List<LocalizedComponent> getTooltip(int mouseX, int mouseY) {
        List<LocalizedComponent> slotTooltip = collectSlotTooltip(mouseX, mouseY);
        if (!slotTooltip.isEmpty()) {
            return slotTooltip;
        }
        return tooltips;
    }

    @NotNull
    protected List<LocalizedComponent> collectSlotTooltip(int mouseX, int mouseY) {
        if (boundLayouts.isEmpty()) return Collections.emptyList();

        int localMouseX = mouseX - gui.getGuiLeft();
        int localMouseY = mouseY - gui.getGuiTop();
        Minecraft mc = Minecraft.getMinecraft();

        for (ComponentSlotLayout layout : boundLayouts) {
            for (Slot slot : layout.getSlots()) {
                if (!slot.isEnabled()) continue;

                int sx = slot.xPos;
                int sy = slot.yPos;
                if (!isMouseOverSlot(localMouseX, localMouseY, sx, sy)) continue;

                ItemStack stack = slot.getStack();
                if (stack.isEmpty()) {
                    return Collections.emptyList();
                }

                List<String> lines = stack.getTooltip(mc.player,
                    mc.gameSettings.advancedItemTooltips
                        ? ITooltipFlag.TooltipFlags.ADVANCED
                        : ITooltipFlag.TooltipFlags.NORMAL);
                List<LocalizedComponent> tips = new ObjectArrayList<>(lines.size());
                for (String line : lines) {
                    tips.add(() -> line);
                }
                return tips;
            }
        }

        return Collections.emptyList();
    }

    protected final boolean hasBoundSlotAt(int mouseX, int mouseY) {
        if (boundLayouts.isEmpty()) return false;

        int localMouseX = mouseX - gui.getGuiLeft();
        int localMouseY = mouseY - gui.getGuiTop();

        for (ComponentSlotLayout layout : boundLayouts) {
            for (Slot slot : layout.getSlots()) {
                if (!slot.isEnabled()) continue;
                if (isMouseOverSlot(localMouseX, localMouseY, slot.xPos, slot.yPos)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected final boolean hasAnyBoundSlotAt(int mouseX, int mouseY) {
        if (hasBoundSlotAt(mouseX, mouseY)) {
            return true;
        }

        for (int i = children.size() - 1; i >= 0; i--) {
            Component child = children.get(i);
            if (!child.isVisible()) continue;
            if (child.hasAnyBoundSlotAt(mouseX, mouseY)) {
                return true;
            }
        }

        return false;
    }

    public <T extends Component> T addTooltip(String s) {
        tooltips.add(() -> s);
        return (T) this;
    }

    public <T extends Component> T addTooltip(LocalizedComponent s) {
        tooltips.add(s);
        return (T) this;
    }

    public <T extends Component> T addTooltip(String key, Supplier<Object[]> supplier) {
        tooltips.add(new Composite(key, supplier));
        return (T) this;
    }

    @NotNull
    public final List<LocalizedComponent> collectTooltip(int mouseX, int mouseY) {
        if (!isVisible() || !contains(mouseX, mouseY)) return Collections.emptyList();

        for (int i = children.size() - 1; i >= 0; i--) {
            Component child = children.get(i);
            if (!child.isVisible() || !child.contains(mouseX, mouseY)) continue;
            return child.collectTooltip(mouseX, mouseY);
        }

        return getTooltip(mouseX, mouseY);
    }

    public final boolean dispatchMouseClicked(int mouseX, int mouseY, int button) {
        if (!isVisible() || !isEnabled() || !contains(mouseX, mouseY)) return false;

        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).dispatchMouseClicked(mouseX, mouseY, button)) return true;
        }

        return onMouseClicked(mouseX, mouseY, button);
    }

    public final boolean dispatchMouseReleased(int mouseX, int mouseY, int button) {
        if (!isVisible() || !isEnabled() || !contains(mouseX, mouseY)) return false;

        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).dispatchMouseReleased(mouseX, mouseY, button)) return true;
        }

        return onMouseReleased(mouseX, mouseY, button);
    }

    public final boolean dispatchMouseScrolled(int mouseX, int mouseY, int delta) {
        if (!isVisible() || !isEnabled() || !contains(mouseX, mouseY)) return false;

        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).dispatchMouseScrolled(mouseX, mouseY, delta)) return true;
        }

        return onMouseScrolled(mouseX, mouseY, delta);
    }

    public final boolean dispatchKeyTyped(char typedChar, int keyCode) {
        if (!isVisible() || !isEnabled()) return false;

        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).dispatchKeyTyped(typedChar, keyCode)) return true;
        }

        return onKeyTyped(typedChar, keyCode);
    }

    protected boolean onMouseClicked(int mouseX, int mouseY, int button) {
        return false;
    }

    protected boolean onMouseReleased(int mouseX, int mouseY, int button) {
        return false;
    }

    protected boolean onMouseScrolled(int mouseX, int mouseY, int delta) {
        return false;
    }

    protected boolean onKeyTyped(char typedChar, int keyCode) {
        return false;
    }

    protected void onMouseEnter() {
    }

    protected void onMouseLeave() {
    }

    public void update() {
        for (Component child : children) {
            child.update();
        }
    }

    public Component setVisible(boolean visible) {
        this.visible = visible;
        invalidateSubtree();
        syncSlotTreePositions();
        return this;
    }

    public Component bindLayout(ComponentSlotLayout... layout) {
        Collections.addAll(boundLayouts, layout);
        invalidateSubtree();
        syncSlotPositions();
        return this;
    }

    public void syncSlotPositions() {
        if (!update || boundLayouts.isEmpty()) return;
        int ax = getAbsoluteX();
        int ay = getAbsoluteY();
        for (ComponentSlotLayout layout : boundLayouts) {
            layout.syncPositions(ax - gui.getGuiLeft(), ay - gui.getGuiTop(), isVisible());
        }
    }

    public Component setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        invalidateSubtree();
        syncSlotTreePositions();
        return this;
    }

    private void invalidateSubtree() {
        update = true;
        for (Component child : children) {
            child.invalidateSubtree();
        }
    }

    private void syncSlotTreePositions() {
        syncSlotPositions();
        for (Component child : children) {
            child.syncSlotTreePositions();
        }
    }

    protected final void restoreGuiRenderState() {
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableRescaleNormal();
        GlStateManager.depthMask(true);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GL11.GL_SRC_ALPHA,
            GL11.GL_ONE_MINUS_SRC_ALPHA,
            GL11.GL_ONE,
            GL11.GL_ZERO
        );
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.loadIdentity();
        GlStateManager.disableTexture2D();

        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Component setZIndex(int zIndex) {
        this.zIndex = zIndex;
        if (parent != null) {
            parent.children.sort(Comparator.comparingInt(c -> c.zIndex));
        }
        return this;
    }

    @Nullable
    public Component getParent() {
        return parent;
    }

    @Override
    public Rectangle clone() throws AssertionError {
        throw new AssertionError();
    }
}