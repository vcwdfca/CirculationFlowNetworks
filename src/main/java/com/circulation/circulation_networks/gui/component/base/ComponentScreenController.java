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
    private Component[] allComponentsTopFirst = EMPTY;
    @Nullable
    private DraggableComponent dragTarget;

    {
        Arrays.fill(phaseComponents, EMPTY);
    }

    public void initializeComponents(Map<RenderPhase, List<Component>> phaseMap) {
        dragTarget = null;
        List<Component> all = new ObjectArrayList<>();
        RenderPhase[] phases = RenderPhase.VALUES;
        for (RenderPhase phase : phases) {
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
        allComponentsTopFirst = reverseCopy(allComponents);
    }

    private static Component[] reverseCopy(Component[] source) {
        int length = source.length;
        if (length == 0) {
            return EMPTY;
        }

        Component[] reversed = new Component[length];
        for (int i = 0; i < length; i++) {
            reversed[i] = source[length - 1 - i];
        }
        return reversed;
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
        RenderSystem.applyModelViewMatrix();
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
        /^modelView.popMatrix();
        ^///?}
        *///?}
    }

    public void updateComponents() {
        for (Component allComponent : allComponents) {
            allComponent.update();
        }
    }

    public void bringToFront(@Nullable Component component) {
        if (component == null) {
            return;
        }

        boolean changed = moveToEnd(allComponents, component);
        for (Component[] phaseArray : phaseComponents) {
            changed |= moveToEnd(phaseArray, component);
        }

        if (changed) {
            allComponentsTopFirst = reverseCopy(allComponents);
        }
    }

    private static boolean moveToEnd(Component[] source, Component component) {
        int index = -1;
        for (int i = 0; i < source.length; i++) {
            if (source[i] == component) {
                index = i;
                break;
            }
        }

        if (index < 0 || index == source.length - 1) {
            return false;
        }

        System.arraycopy(source, index + 1, source, index, source.length - index - 1);
        source[source.length - 1] = component;
        return true;
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        for (Component component : allComponents) {
            component.dispatchGlobalMouseClicked(mouseX, mouseY, mouseButton);
        }

        for (Component component : allComponentsTopFirst) {
            if (!component.isVisible() || !component.contains(mouseX, mouseY)) {
                continue;
            }
            boolean handled = component.dispatchMouseClicked(mouseX, mouseY, mouseButton);
            if (handled) {
                dragTarget = ComponentTreeUtils.findDraggingComponent(allComponents);
            }
            return handled;
        }
        return false;
    }

    public boolean mouseReleased(int mouseX, int mouseY, int state) {
        if (dragTarget != null) {
            dragTarget.stopDrag(mouseX, mouseY);
            dragTarget = null;
            return true;
        }
        for (Component component : allComponentsTopFirst) {
            if (!component.isVisible() || !component.contains(mouseX, mouseY)) {
                continue;
            }
            return component.dispatchMouseReleased(mouseX, mouseY, state);
        }
        return false;
    }

    public boolean keyTyped(char typedChar, int keyCode) {
        for (Component component : allComponentsTopFirst) {
            if (component.dispatchKeyTyped(typedChar, keyCode)) {
                return true;
            }
        }
        return false;
    }

    public boolean mouseScrolled(int mouseX, int mouseY, int delta) {
        for (Component component : allComponentsTopFirst) {
            if (!component.isVisible() || !component.contains(mouseX, mouseY)) {
                continue;
            }
            return component.dispatchMouseScrolled(mouseX, mouseY, delta);
        }
        return false;
    }

    public List<String> collectTooltip(int mouseX, int mouseY) {
        if (allComponentsTopFirst.length == 0) {
            return Collections.emptyList();
        }
        var list = ComponentTreeUtils.collectTopTooltip(allComponentsTopFirst, mouseX, mouseY);
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        var n = new ObjectArrayList<String>();
        for (var s : list) {
            n.add(s.get());
        }
        return n;
    }

    @Nullable
    public Component getTopComponentAt(int mouseX, int mouseY) {
        if (allComponentsTopFirst.length == 0) {
            return null;
        }
        return ComponentTreeUtils.findTopComponentAt(allComponentsTopFirst, mouseX, mouseY);
    }

    public Component[] getAllComponents() {
        return allComponents;
    }
}