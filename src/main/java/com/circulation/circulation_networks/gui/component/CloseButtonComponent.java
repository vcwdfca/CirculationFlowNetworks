package com.circulation.circulation_networks.gui.component;

import com.circulation.circulation_networks.gui.CFNBaseGui;
import com.circulation.circulation_networks.gui.component.base.Component;

public class CloseButtonComponent extends ButtonComponent {

    public static final String DEFAULT_SPRITE = "ui_close";
    private Component mutuallyExclusiveComponent;

    public CloseButtonComponent(int x, int y, int width, int height, CFNBaseGui<?> gui) {
        super(x, y, width, height, gui, DEFAULT_SPRITE, EMPTY);
    }

    public CloseButtonComponent(int x, int y, int width, int height, CFNBaseGui<?> gui, String sprite) {
        super(x, y, width, height, gui, sprite, EMPTY);
    }

    public CloseButtonComponent setMutuallyComponent(Component component) {
        this.mutuallyExclusiveComponent = component;
        return this;
    }

    public Component getMutuallyExclusiveComponent() {
        return mutuallyExclusiveComponent;
    }

    @Override
    protected void onClick() {
        Component parent = getParent();
        if (parent != null) {
            parent.setVisible(false);
            if (mutuallyExclusiveComponent != null) {
                mutuallyExclusiveComponent.setEnabled(true);
            }
            return;
        }
        closeCurrentGui();
    }

    private void closeCurrentGui() {
        //~ if >=1.20 '.mc.player.closeScreen()' -> '.getMinecraft().player.closeContainer()' {
        this.gui.mc.player.closeScreen();
        //~}
    }
}
