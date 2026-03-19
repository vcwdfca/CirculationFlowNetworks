package com.circulation.circulation_networks.gui.component.base;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
//? if <1.20 {
import net.minecraft.client.renderer.GlStateManager;
//?} else {
/*import com.mojang.blaze3d.systems.RenderSystem;
*///?}

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class ComponentScreenController {

    private static final Component[] EMPTY = new Component[0];

    private final Component[][] phaseComponents = new Component[RenderPhase.VALUES.length][];
    private Component[] allComponents = EMPTY;
    @Nullable
    private DraggableComponent dragTarget;

    {
        Arrays.fill(phaseComponents, EMPTY);
    }

    public void initializeComponents(Map<RenderPhase, List<Component>> phaseMap) {
        dragTarget = null;
        List<Component> all = new ObjectArrayList<>();
        for (RenderPhase phase : RenderPhase.VALUES) {
            List<Component> list = phaseMap.getOrDefault(phase, Collections.emptyList());
            if (list.isEmpty()) {
                phaseComponents[phase.ordinal()] = EMPTY;
            } else {
                List<Component> sorted = new ObjectArrayList<>(list);
                sorted.sort(Comparator.comparingInt(Component::getZIndex));
                phaseComponents[phase.ordinal()] = sorted.toArray(new Component[0]);
                all.addAll(sorted);
            }
        }
        all.sort(Comparator.comparingInt(Component::getZIndex));
        allComponents = all.toArray(new Component[0]);
    }

    public void handleActiveDrag(int mouseX, int mouseY) {
        if (dragTarget != null && dragTarget.isDragging()) {
            dragTarget.handleDrag(mouseX, mouseY);
        }
    }

    public void renderPhase(RenderPhase phase, int mouseX, int mouseY, float partialTicks) {
        //? if <1.20 {
        GlStateManager.pushMatrix();
        //?} else {
        /*var modelView = RenderSystem.getModelViewStack();
        //? if <1.21 {
        modelView.pushPose();
        //?} else {
        modelView.pushMatrix();
        //?}
        *///?}
        for (Component component : phaseComponents[phase.ordinal()]) {
            component.renderComponent(mouseX, mouseY, partialTicks);
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

    public void updateComponents() {
        for (Component component : allComponents) {
            component.update();
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        for (int i = allComponents.length - 1; i >= 0; i--) {
            if (allComponents[i].dispatchMouseClicked(mouseX, mouseY, mouseButton)) {
                dragTarget = ComponentTreeUtils.findDraggingComponent(allComponents);
                return true;
            }
        }
        return false;
    }

    public boolean mouseReleased(int mouseX, int mouseY, int state) {
        if (dragTarget != null) {
            dragTarget.stopDrag(mouseX, mouseY);
            dragTarget = null;
            return true;
        }
        for (int i = allComponents.length - 1; i >= 0; i--) {
            if (allComponents[i].dispatchMouseReleased(mouseX, mouseY, state)) {
                return true;
            }
        }
        return false;
    }

    public boolean keyTyped(char typedChar, int keyCode) {
        for (int i = allComponents.length - 1; i >= 0; i--) {
            if (allComponents[i].dispatchKeyTyped(typedChar, keyCode)) {
                return true;
            }
        }
        return false;
    }

    public boolean mouseScrolled(int mouseX, int mouseY, int delta) {
        for (int i = allComponents.length - 1; i >= 0; i--) {
            if (allComponents[i].dispatchMouseScrolled(mouseX, mouseY, delta)) {
                return true;
            }
        }
        return false;
    }

    public List<String> collectTooltip(int mouseX, int mouseY) {
        if (allComponents.length == 0) {
            return Collections.emptyList();
        }
        return ComponentTreeUtils.collectTopTooltip(allComponents, mouseX, mouseY);
    }
}