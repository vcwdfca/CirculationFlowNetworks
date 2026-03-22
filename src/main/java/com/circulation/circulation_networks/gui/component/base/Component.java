package com.circulation.circulation_networks.gui.component.base;

import com.circulation.circulation_networks.container.ComponentSlotLayout;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
//? if <1.20 {
import net.minecraft.client.renderer.GlStateManager;
//?} else {
/*import com.mojang.blaze3d.systems.RenderSystem;
*///?}
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("unused")
public abstract class Component extends Rectangle {

    private final List<Component> children = new ObjectArrayList<>();
    private final List<ComponentSlotLayout> boundLayouts = new ObjectArrayList<>();
    @Nonnull
    private final ComponentGuiContext gui;
    protected boolean visible = true;
    protected boolean enabled = true;
    protected int zIndex = 0;
    protected boolean update;
    @Nullable
    private Component parent;
    private boolean hovered = false;

    private String[] spriteLayers = new String[0];

    protected Component(int x, int y, int width, int height, @Nonnull ComponentGuiContext gui) {
        super(x, y, width, height);
        this.gui = gui;
        update = true;
    }

    public List<Component> getChildren() {
        return children;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    public void setX(int x) {
        this.x = x;
        update = true;
    }

    public void setY(int y) {
        this.y = y;
        update = true;
    }

    public void addChild(Component child) {
        child.parent = this;
        children.add(child);
        children.sort(Comparator.comparingInt(c -> c.zIndex));
    }

    public void removeChild(Component child) {
        if (children.remove(child)) {
            child.parent = null;
        }
    }

    public int getAbsoluteX() {
        return parent != null ? parent.getAbsoluteX() + x : gui.getGuiLeft() + x;
    }

    public int getAbsoluteY() {
        return parent != null ? parent.getAbsoluteY() + y : gui.getGuiTop() + y;
    }

    public boolean contains(int mouseX, int mouseY) {
        if (!isVisible()) return false;
        int w = this.width;
        int h = this.height;
        if ((w | h) < 0) {
            return false;
        }
        int x = this.getAbsoluteX();
        int y = this.getAbsoluteX();
        if (mouseX < x || mouseY < y) {
            return false;
        }
        w += x;
        h += y;
        return ((w < x || w > mouseX) &&
            (h < y || h > mouseY));
    }

    public boolean contains(int X, int Y, int W, int H) {
        if (!isVisible()) return false;
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

        if (children.isEmpty()) return;
        //? if <1.20 {
        GlStateManager.pushMatrix();
        GlStateManager.translate(getAbsoluteX(), getAbsoluteY(), 0);
        //?} else {
        /*var modelView = RenderSystem.getModelViewStack();
        //? if <1.21 {
        modelView.pushPose();
        modelView.translate(getAbsoluteX(), getAbsoluteY(), 0);
        RenderSystem.applyModelViewMatrix();
        //?} else {
        modelView.pushMatrix();
        modelView.translate(getAbsoluteX(), getAbsoluteY(), 0);
        //?}
        *///?}
        for (Component child : children) {
            child.renderComponent(mouseX, mouseY, partialTicks);
        }
        //? if <1.20 {
        GlStateManager.popMatrix();
        //?} else {
        /*//? if <1.21 {
        modelView.popPose();
        RenderSystem.applyModelViewMatrix();
        //?} else {
        modelView.popMatrix();
        //?}
        *///?}
    }

    protected abstract void render(int mouseX, int mouseY, float partialTicks);

    protected final void setSpriteLayers(String... layers) {
        this.spriteLayers = layers != null ? layers : new String[0];
    }

    protected String[] getActiveLayers() {
        return spriteLayers;
    }

    protected void renderSpriteLayers() {
        String[] layers = getActiveLayers();
        if (layers == null || layers.length == 0) return;

        ComponentAtlas atlas = ComponentAtlas.INSTANCE;
        if (!atlas.isReady()) return;

        int ax = getAbsoluteX();
        int ay = getAbsoluteY();
        for (String name : layers) {
            if (name == null) continue;
            AtlasRegion region = atlas.getRegion(name);
            if (region == null) continue;
            AtlasRenderHelper.drawRegion(atlas, region, ax, ay, width, height);
        }
    }

    @NotNull
    public List<String> getTooltip(int mouseX, int mouseY) {
        return Collections.emptyList();
    }

    @NotNull
    public final List<String> collectTooltip(int mouseX, int mouseY) {
        if (!isVisible() || !contains(mouseX, mouseY)) return Collections.emptyList();

        for (int i = children.size() - 1; i >= 0; i--) {
            List<String> tip = children.get(i).collectTooltip(mouseX, mouseY);
            if (!tip.isEmpty()) return tip;
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

    public void setVisible(boolean visible) {
        this.visible = visible;
        update = true;
        syncSlotPositions();
    }

    public void bindLayout(ComponentSlotLayout layout) {
        boundLayouts.add(layout);
        update = true;
        syncSlotPositions();
    }

    public void syncSlotPositions() {
        if (!update && boundLayouts.isEmpty()) return;
        int ax = getAbsoluteX();
        int ay = getAbsoluteY();
        for (ComponentSlotLayout layout : boundLayouts) {
            layout.syncPositions(ax - gui.getGuiLeft(), ay - gui.getGuiTop(), isVisible());
        }
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        update = true;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setZIndex(int zIndex) {
        this.zIndex = zIndex;
        if (parent != null) {
            parent.children.sort(Comparator.comparingInt(c -> c.zIndex));
        }
    }

    @Nullable
    public Component getParent() {
        return parent;
    }

    @Override
    public Component clone() throws AssertionError {
        throw new AssertionError();
    }
}