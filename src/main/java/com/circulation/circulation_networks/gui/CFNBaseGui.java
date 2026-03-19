package com.circulation.circulation_networks.gui;

import com.circulation.circulation_networks.container.CFNBaseContainer;
import com.circulation.circulation_networks.gui.component.base.AtlasRegion;
import com.circulation.circulation_networks.gui.component.base.AtlasRenderHelper;
import com.circulation.circulation_networks.gui.component.base.Component;
import com.circulation.circulation_networks.gui.component.base.ComponentAtlas;
import com.circulation.circulation_networks.gui.component.base.ComponentGuiContext;
import com.circulation.circulation_networks.gui.component.base.ComponentScreenController;
import com.circulation.circulation_networks.gui.component.base.RenderPhase;
//? if <1.20 {
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
*///?}

import javax.annotation.Nonnull;
//? if <1.20 {
import java.io.IOException;
//?}
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for all CFN GUI screens that need interactive components.
 *
 * <p>Rendering layers (bottom → top):
 * <ol>
 *   <li>Background texture — implement {@link #drawBG}.</li>
 *   <li>Slots &amp; item stacks — handled automatically by the parent screen.</li>
 *   <li>Foreground labels — implement {@link #drawFG}.</li>
 *   <li>Component layer — all {@link Component} instances registered via
 *       {@link #buildComponents}, sorted ascending by z-index.</li>
 *   <li>Component tooltip — collected from the topmost hovered component.</li>
 *   <li>Slot tooltip — vanilla hover-text for inventory slots (rendered last).</li>
 * </ol>
 *
 * <p>Event routing: Mouse and key events are delivered to components in descending
 * z-index order (highest z-index first). The first component that returns {@code true}
 * from its handler consumes the event; remaining components and the default
 * behavior are skipped.
 */
@SuppressWarnings("unused")
//? if <1.20 {
@SideOnly(Side.CLIENT)
public abstract class CFNBaseGui extends GuiContainer implements ComponentGuiContext {
//?} else {
/*@OnlyIn(Dist.CLIENT)
public abstract class CFNBaseGui extends AbstractContainerScreen<CFNBaseContainer> implements ComponentGuiContext {
*///?}

    private final ComponentScreenController componentController = new ComponentScreenController();

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    //? if <1.20 {
    protected CFNBaseGui(@Nonnull CFNBaseContainer container) {
        super(container);
    }
    //?} else {
    /*protected CFNBaseGui(@Nonnull CFNBaseContainer container, Inventory playerInventory, net.minecraft.network.chat.Component title) {
        super(container, playerInventory, title);
    }
    *///?}

    /**
     * Renders a UI background sprite from the shared {@link ComponentAtlas}.
     */
    protected static void drawAtlasBackground(String name, int screenX, int screenY, int w, int h) {
        ComponentAtlas atlas = ComponentAtlas.INSTANCE;
        AtlasRegion region = atlas.getBackground(name);
        if (region == null) return;
        AtlasRenderHelper.drawRegion(atlas, region, screenX, screenY, w, h);
    }

    @Override
    public int getGuiLeft() {
        //? if <1.20 {
        return guiLeft;
        //?} else {
        /*return leftPos;
        *///?}
    }

    @Override
    public int getGuiTop() {
        //? if <1.20 {
        return guiTop;
        //?} else {
        /*return topPos;
        *///?}
    }

    // -------------------------------------------------------------------------
    // Component registration
    // -------------------------------------------------------------------------

    /**
     * Override this method to register all root-level components for this GUI.
     * Add components into the map under the appropriate {@link RenderPhase}.
     */
    protected void buildComponents(Map<RenderPhase, List<Component>> components) {
    }

    // -------------------------------------------------------------------------
    // GUI lifecycle
    // -------------------------------------------------------------------------

    @Override
    //? if <1.20 {
    public void initGui() {
        super.initGui();
    //?} else {
    /*protected void init() {
        super.init();
    *///?}
        Map<RenderPhase, List<Component>> phaseMap = new EnumMap<>(RenderPhase.class);
        buildComponents(phaseMap);
        componentController.initializeComponents(phaseMap);
        ComponentAtlas.INSTANCE.awaitReady();
    }

    // -------------------------------------------------------------------------
    // Abstract rendering hooks
    // -------------------------------------------------------------------------

    /**
     * Draw the GUI background texture.
     */
    public abstract void drawBG(int offsetX, int offsetY, int mouseX, int mouseY);

    /**
     * Draw GUI foreground elements (labels, overlays, etc.).
     */
    public abstract void drawFG(int offsetX, int offsetY, int mouseX, int mouseY);

    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    private void resetColor() {
        //? if <1.20 {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        //?} else {
        /*RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         *///?}
    }

    private void renderBGPhase(int mouseX, int mouseY, float partialTicks) {
        resetColor();
        componentController.renderPhase(RenderPhase.BACKGROUND, mouseX, mouseY, partialTicks);
    }

    private void renderComponentTooltip(int mouseX, int mouseY) {
        List<String> componentTooltip = componentController.collectTooltip(mouseX, mouseY);
        if (componentTooltip == null || componentTooltip.isEmpty()) return;
        //? if <1.20 {
        drawHoveringText(componentTooltip, mouseX, mouseY);
        //?} else {
        /*List<net.minecraft.network.chat.Component> mcTooltip = new java.util.ArrayList<>();
        for (String line : componentTooltip) {
            mcTooltip.add(net.minecraft.network.chat.Component.literal(line));
        }
        // guiGraphics is not available here — tooltip rendered inline below
        *///?}
    }

    //? if <1.20 {
    @Override
    protected final void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        renderBGPhase(mouseX, mouseY, partialTicks);
        resetColor();
        this.drawBG(this.guiLeft, this.guiTop, mouseX, mouseY);
    }

    @Override
    protected final void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        resetColor();
        this.drawFG(this.guiLeft, this.guiTop, mouseX, mouseY);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        componentController.handleActiveDrag(mouseX, mouseY);
        resetColor();
        componentController.renderPhase(RenderPhase.NORMAL, mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
        resetColor();
        componentController.renderPhase(RenderPhase.FOREGROUND, mouseX, mouseY, partialTicks);
        renderComponentTooltip(mouseX, mouseY);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        componentController.updateComponents();
    }
    //?} else {
    /*@Override
    protected final void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        renderBGPhase(mouseX, mouseY, partialTick);
        resetColor();
        this.drawBG(this.leftPos, this.topPos, mouseX, mouseY);
    }

    @Override
    protected final void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        resetColor();
        this.drawFG(this.leftPos, this.topPos, mouseX, mouseY);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        componentController.handleActiveDrag(mouseX, mouseY);
        resetColor();
        componentController.renderPhase(RenderPhase.NORMAL, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        resetColor();
        componentController.renderPhase(RenderPhase.FOREGROUND, mouseX, mouseY, partialTick);

        List<String> componentTooltip = componentController.collectTooltip(mouseX, mouseY);
        if (componentTooltip != null && !componentTooltip.isEmpty()) {
            List<net.minecraft.network.chat.Component> mcTooltip = new java.util.ArrayList<>();
            for (String line : componentTooltip) {
                mcTooltip.add(net.minecraft.network.chat.Component.literal(line));
            }
            guiGraphics.renderTooltip(this.font, mcTooltip, java.util.Optional.empty(), mouseX, mouseY);
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        componentController.updateComponents();
    }
    *///?}

    // -------------------------------------------------------------------------
    // Event handling
    // -------------------------------------------------------------------------

    @Override
    //? if <1.20 {
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (componentController.mouseClicked(mouseX, mouseY, mouseButton)) {
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
    //?} else {
    /*public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (componentController.mouseClicked((int) mouseX, (int) mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    *///?}

    @Override
    //? if <1.20 {
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (componentController.mouseReleased(mouseX, mouseY, state)) {
            return;
        }
        super.mouseReleased(mouseX, mouseY, state);
    }
    //?} else {
    /*public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (componentController.mouseReleased((int) mouseX, (int) mouseY, button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    *///?}

    @Override
    //? if <1.20 {
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (componentController.keyTyped(typedChar, keyCode)) {
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }
    //?} else {
    /*public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (componentController.keyTyped((char) 0, keyCode)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    *///?}

    //? if <1.20 {
    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scrollDelta = Mouse.getEventDWheel();
        if (scrollDelta != 0) {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            int delta = scrollDelta > 0 ? 1 : -1;
            componentController.mouseScrolled(mouseX, mouseY, delta);
        }
    }
    //?} else {
    /*@Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta != 0) {
            int d = delta > 0 ? 1 : -1;
            componentController.mouseScrolled((int) mouseX, (int) mouseY, d);
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
    *///?}
}
