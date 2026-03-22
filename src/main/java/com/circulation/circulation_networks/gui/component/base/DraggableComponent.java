package com.circulation.circulation_networks.gui.component.base;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public abstract class DraggableComponent extends Component {

    private boolean draggable = true;
    private boolean dragging = false;

    @Nullable
    private int[] dragBounds = null;

    private int dragOffsetX;
    private int dragOffsetY;

    protected DraggableComponent(int x, int y, int width, int height, ComponentGuiContext gui) {
        super(x, y, width, height, gui);
    }

    public boolean isDraggable() {
        return draggable;
    }

    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
    }

    public boolean isDragging() {
        return dragging;
    }

    @Nullable
    public int[] getDragBounds() {
        return dragBounds;
    }

    public void setDragBounds(@Nullable int[] dragBounds) {
        this.dragBounds = dragBounds;
    }

    public void startDrag(int mouseX, int mouseY) {
        dragging = true;
        dragOffsetX = mouseX - getAbsoluteX();
        dragOffsetY = mouseY - getAbsoluteY();
        onDragStart(mouseX, mouseY);
    }

    public void handleDrag(int mouseX, int mouseY) {
        if (!dragging) return;

        int parentAbsX = (getParent() != null) ? getParent().getAbsoluteX() : 0;
        int parentAbsY = (getParent() != null) ? getParent().getAbsoluteY() : 0;

        int newX = mouseX - dragOffsetX - parentAbsX;
        int newY = mouseY - dragOffsetY - parentAbsY;

        if (dragBounds != null) {
            newX = Math.max(dragBounds[0], Math.min(dragBounds[2], newX));
            newY = Math.max(dragBounds[1], Math.min(dragBounds[3], newY));
        }

        if (getParent() != null) {
            int maxX = getParent().width - this.width;
            int maxY = getParent().height - this.height;
            newX = Math.max(0, Math.min(maxX, newX));
            newY = Math.max(0, Math.min(maxY, newY));
        }

        int deltaX = newX - x;
        int deltaY = newY - y;
        setX(newX);
        setY(newY);
        update = true;

        onDrag(deltaX, deltaY);
    }

    public void stopDrag(int mouseX, int mouseY) {
        if (!dragging) return;
        handleDrag(mouseX, mouseY);
        dragging = false;
        onDragEnd(mouseX, mouseY);
    }

    @Override
    protected boolean onMouseClicked(int mouseX, int mouseY, int button) {
        if (draggable && button == 0) {
            startDrag(mouseX, mouseY);
            return true;
        }
        return false;
    }

    protected void onDragStart(int mouseX, int mouseY) {
    }

    protected void onDrag(int deltaX, int deltaY) {
    }

    protected void onDragEnd(int mouseX, int mouseY) {
    }
}