package com.circulation.circulation_networks.gui.component.base;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public final class ComponentTreeUtils {

    private ComponentTreeUtils() {
    }

    @Nullable
    public static DraggableComponent findDraggingComponent(Component[] nodes) {
        for (Component component : nodes) {
            if (component instanceof DraggableComponent && ((DraggableComponent) component).isDragging()) {
                return (DraggableComponent) component;
            }
            DraggableComponent found = findDraggingComponent(component.getChildren());
            if (found != null) return found;
        }
        return null;
    }

    @Nullable
    public static DraggableComponent findDraggingComponent(List<Component> nodes) {
        for (Component component : nodes) {
            if (component instanceof DraggableComponent && ((DraggableComponent) component).isDragging()) {
                return (DraggableComponent) component;
            }
            DraggableComponent found = findDraggingComponent(component.getChildren());
            if (found != null) return found;
        }
        return null;
    }

    public static List<Component.LocalizedComponent> collectTopTooltip(Component[] components, int mouseX, int mouseY) {
        for (int i = components.length - 1; i >= 0; i--) {
            Component component = components[i];
            if (!component.isVisible() || !component.contains(mouseX, mouseY)) continue;
            return component.collectTooltip(mouseX, mouseY);
        }
        return Collections.emptyList();
    }

    @Nullable
    public static Component findTopComponentAt(Component[] components, int mouseX, int mouseY) {
        for (int i = components.length - 1; i >= 0; i--) {
            Component component = components[i];
            if (!component.isVisible() || !component.contains(mouseX, mouseY)) continue;
            return findTopComponentAt(component, mouseX, mouseY);
        }
        return null;
    }

    @Nullable
    private static Component findTopComponentAt(Component component, int mouseX, int mouseY) {
        List<Component> children = component.getChildren();
        for (int i = children.size() - 1; i >= 0; i--) {
            Component child = children.get(i);
            if (!child.isVisible() || !child.contains(mouseX, mouseY)) continue;
            return findTopComponentAt(child, mouseX, mouseY);
        }
        return component;
    }
}