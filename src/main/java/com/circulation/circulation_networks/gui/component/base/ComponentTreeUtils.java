package com.circulation.circulation_networks.gui.component.base;

import com.circulation.circulation_networks.tooltip.LocalizedComponent;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public final class ComponentTreeUtils {

    private ComponentTreeUtils() {
    }

    @Nullable
    public static DraggableComponent findDraggingComponent(Component[] nodes) {
        for (Component component : nodes) {
            if (component instanceof DraggableComponent c && c.isDragging()) {
                return c;
            }
            DraggableComponent found = findDraggingComponent(component.getChildren());
            if (found != null) return found;
        }
        return null;
    }

    @Nullable
    public static DraggableComponent findDraggingComponent(List<Component> nodes) {
        if (nodes.isEmpty()) {
            return null;
        }
        for (Component component : nodes) {
            if (component instanceof DraggableComponent c && c.isDragging()) {
                return c;
            }
            DraggableComponent found = findDraggingComponent(component.getChildren());
            if (found != null) return found;
        }
        return null;
    }

    public static List<LocalizedComponent> collectTopTooltip(Component[] components, int mouseX, int mouseY) {
        for (Component component : components) {
            if (!component.isVisible() || !component.contains(mouseX, mouseY)) continue;
            return component.collectTooltip(mouseX, mouseY);
        }
        return Collections.emptyList();
    }

    @Nullable
    public static Component findTopComponentAt(Component[] components, int mouseX, int mouseY) {
        for (Component component : components) {
            if (!component.isVisible() || !component.contains(mouseX, mouseY)) continue;
            return findTopComponentAt(component, mouseX, mouseY);
        }
        return null;
    }

    @Nullable
    private static Component findTopComponentAt(Component component, int mouseX, int mouseY) {
        List<Component> children = component.getChildren();
        if (children.isEmpty()) {
            return component;
        }
        for (int i = children.size(); i-- > 0;) {
            Component child = children.get(i);
            if (!child.isVisible() || !child.contains(mouseX, mouseY)) continue;
            return findTopComponentAt(child, mouseX, mouseY);
        }
        return component;
    }
}