package com.circulation.circulation_networks.gui.component;

import com.circulation.circulation_networks.gui.component.base.Component;
import com.circulation.circulation_networks.gui.component.base.ComponentGuiContext;

import javax.annotation.Nonnull;

/**
 * A non-interactive component used solely for rendering a GUI background sprite.
 * It does not respond to any mouse or key events.
 */
public class BackgroundComponent extends Component {

    public BackgroundComponent(int width, int height, @Nonnull String bgSprite, @Nonnull ComponentGuiContext gui) {
        super(0, 0, width, height, gui);
        setSpriteLayers("bg/" + bgSprite);
    }

    @Override
    protected void render(int mouseX, int mouseY, float partialTicks) {
    }

    @Override
    public boolean contains(int mouseX, int mouseY) {
        return false;
    }
}