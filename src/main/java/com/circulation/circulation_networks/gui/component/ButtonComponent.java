package com.circulation.circulation_networks.gui.component;

import com.circulation.circulation_networks.gui.CFNBaseGui;
import com.circulation.circulation_networks.gui.component.base.ComponentAtlas;

@SuppressWarnings("unused")
public class ButtonComponent extends AbstractButtonComponent {

    protected final String[] cache = new String[2];
    private final String normalSprite;
    private final String hoveredSprite;
    private final String pressedSprite;
    private final String disabledSprite;
    private String iconNormal;
    private String iconHovered;
    private String iconPressed;
    private String iconDisabled;

    public ButtonComponent(int x, int y, int width, int height, CFNBaseGui<?> gui, String sprite, Runnable run) {
        super(x, y, width, height, gui, run);
        this.normalSprite = sprite;
        this.hoveredSprite = normalSprite + "_hovered";
        this.pressedSprite = normalSprite + "_pressed";
        this.disabledSprite = normalSprite + "_disabled";
    }

    public ButtonComponent setIconNormal(String iconNormal) {
        this.iconNormal = iconNormal;
        this.iconHovered = iconNormal + "_hovered";
        this.iconPressed = iconNormal + "_pressed";
        this.iconDisabled = iconNormal + "_disabled";
        return this;
    }

    public ButtonComponent setPressed(boolean pressed) {
        this.pressed = pressed;
        return this;
    }

    @Override
    protected String[] getActiveLayers() {
        ComponentAtlas atlas = ComponentAtlas.INSTANCE;
        String bg;
        if (!isEnabled()) bg = atlas.getRegion(disabledSprite) != null ? disabledSprite : normalSprite;
        else if (pressed) bg = atlas.getRegion(pressedSprite) != null ? pressedSprite : normalSprite;
        else if (isHovered()) bg = atlas.getRegion(hoveredSprite) != null ? hoveredSprite : normalSprite;
        else bg = normalSprite;
        cache[0] = bg;

        String icon;
        if (iconNormal != null) {
            if (!isEnabled()) icon = atlas.getRegion(iconDisabled) != null ? iconDisabled : iconNormal;
            else if (pressed) icon = atlas.getRegion(iconPressed) != null ? iconPressed : iconNormal;
            else if (isHovered()) icon = atlas.getRegion(iconHovered) != null ? iconHovered : iconNormal;
            else icon = iconNormal;
            cache[1] = icon;
        } else cache[1] = null;

        return cache;
    }
}