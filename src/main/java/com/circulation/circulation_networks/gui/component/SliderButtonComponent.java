package com.circulation.circulation_networks.gui.component;

import com.circulation.circulation_networks.gui.CFNBaseGui;
import com.circulation.circulation_networks.gui.component.base.Component;
import com.circulation.circulation_networks.gui.component.base.ComponentAtlas;
import com.circulation.circulation_networks.gui.component.base.DraggableComponent;

public class SliderButtonComponent extends DraggableComponent {

    private static final String NORMAL = "slider_button";
    private static final String HOVERED = "slider_button_hovered";
    private static final String PRESSED = "slider_button_pressed";
    private static final String DISABLED = "slider_button_disabled";

    private final String[] cache = new String[1];

    public SliderButtonComponent(int x, int y, CFNBaseGui<?> gui) {
        super(x, y, SliderComponent.TRACK_WIDTH, SliderComponent.BUTTON_HEIGHT, gui);
    }

    @Override
    protected String[] getActiveLayers() {
        ComponentAtlas atlas = ComponentAtlas.INSTANCE;
        if (!isEnabled()) {
            cache[0] = atlas.getRegion(DISABLED) != null ? DISABLED : NORMAL;
        } else if (isDragging()) {
            cache[0] = atlas.getRegion(PRESSED) != null ? PRESSED : NORMAL;
        } else if (isHovered()) {
            cache[0] = atlas.getRegion(HOVERED) != null ? HOVERED : NORMAL;
        } else {
            cache[0] = NORMAL;
        }
        return cache;
    }

    @Override
    protected void onDrag(int deltaX, int deltaY) {
        if (x != 0) {
            setX(0);
        }
        SliderComponent slider = sliderParent();
        if (slider != null) {
            slider.onButtonDragged(false);
        }
    }

    @Override
    protected void onDragEnd(int mouseX, int mouseY) {
        if (x != 0) {
            setX(0);
        }
        SliderComponent slider = sliderParent();
        if (slider != null) {
            slider.onButtonDragged(true);
        }
    }

    private SliderComponent sliderParent() {
        Component p = getParent();
        if (p instanceof SliderComponent) {
            return (SliderComponent) p;
        }
        return null;
    }
}
