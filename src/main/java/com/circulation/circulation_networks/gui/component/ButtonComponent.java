package com.circulation.circulation_networks.gui.component;

import com.circulation.circulation_networks.gui.CFNBaseGui;
import com.circulation.circulation_networks.gui.component.base.Component;
import com.circulation.circulation_networks.gui.component.base.ComponentAtlas;

@SuppressWarnings("unused")
public class ButtonComponent extends Component {

    public static final Runnable EMPTY = () -> {};
    protected final String[] cache = new String[2];
    private final String normalSprite;
    private final String hoveredSprite;
    private final String pressedSprite;
    private final String disabledSprite;
    private String iconNormal;
    private String iconHovered;
    private String iconPressed;
    private String iconDisabled;
    private boolean pressed = false;
    private Runnable run;

    public ButtonComponent(int x, int y, int width, int height, CFNBaseGui<?> gui, String sprite, Runnable run) {
        super(x, y, width, height, gui);
        this.normalSprite = sprite;
        this.hoveredSprite = normalSprite + "_hovered";
        this.pressedSprite = normalSprite + "_pressed";
        this.disabledSprite = normalSprite + "_disabled";
        this.run = run;
    }

    public boolean isPressed() {
        return pressed;
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

    public void setRun(Runnable run) {
        this.run = run;
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

    @Override
    protected final boolean onMouseClicked(int mouseX, int mouseY, int button) {
        if (button == 0) {
            pressed = true;
            return true;
        }
        return false;
    }

    @Override
    protected final boolean onMouseReleased(int mouseX, int mouseY, int button) {
        if (button == 0 && pressed) {
            pressed = false;
            onClick();
            return true;
        }
        pressed = false;
        return false;
    }

    @Override
    protected void onMouseLeave() {
        pressed = false;
    }

    protected void onClick() {
        run.run();
    }
}