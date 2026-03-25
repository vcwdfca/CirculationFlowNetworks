package com.circulation.circulation_networks.gui.component;

import com.circulation.circulation_networks.gui.CFNBaseGui;
import com.circulation.circulation_networks.gui.component.base.ComponentAtlas;

import java.util.function.BooleanSupplier;

@SuppressWarnings("unused")
public class TriStateButtonComponent extends AbstractButtonComponent {

    private static final String[] EMPTY_LAYERS = new String[0];

    private final String inactiveSprite;
    private final String hoveredSprite;
    private final String activeSprite;
    private final String[] cache = new String[1];

    private boolean active = false;
    private BooleanSupplier activeSupplier;

    public TriStateButtonComponent(int x, int y, int width, int height, CFNBaseGui<?> gui,
                                   String sprite,Runnable run) {
        super(x, y, width, height, gui, run);
        this.inactiveSprite = sprite;
        this.hoveredSprite = sprite + "_hovered";
        this.activeSprite = sprite + "_active";
    }

    public TriStateButtonComponent setActive(boolean active) {
        this.active = active;
        return this;
    }

    public boolean isActive() {
        return activeSupplier != null ? activeSupplier.getAsBoolean() : active;
    }

    public TriStateButtonComponent setActiveSupplier(BooleanSupplier activeSupplier) {
        this.activeSupplier = activeSupplier;
        return this;
    }

    @Override
    protected String[] getActiveLayers() {
        ComponentAtlas atlas = ComponentAtlas.INSTANCE;
        if (hoveredSprite != null && isHovered() && atlas.getRegion(hoveredSprite) != null) {
            cache[0] = hoveredSprite;
            return cache;
        }
        String stateSprite = isActive() ? activeSprite : inactiveSprite;
        if (stateSprite != null && atlas.getRegion(stateSprite) != null) {
            cache[0] = stateSprite;
            return cache;
        }
        return EMPTY_LAYERS;
    }
}