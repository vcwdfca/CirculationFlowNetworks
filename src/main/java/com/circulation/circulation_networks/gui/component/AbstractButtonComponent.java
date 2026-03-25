package com.circulation.circulation_networks.gui.component;

import com.circulation.circulation_networks.gui.CFNBaseGui;
import com.circulation.circulation_networks.gui.component.base.Component;

public abstract class AbstractButtonComponent extends Component {

    public static final Runnable EMPTY = () -> {};

    protected boolean pressed = false;
    private Runnable run;

    protected AbstractButtonComponent(int x, int y, int width, int height, CFNBaseGui<?> gui, Runnable run) {
        super(x, y, width, height, gui);
        this.run = run != null ? run : EMPTY;
    }

    public boolean isPressed() {
        return pressed;
    }

    public void setRun(Runnable run) {
        this.run = run != null ? run : EMPTY;
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