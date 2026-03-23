package com.circulation.circulation_networks.gui.component;

import com.circulation.circulation_networks.container.ComponentSlotLayout;
import com.circulation.circulation_networks.gui.CFNBaseGui;
import com.circulation.circulation_networks.gui.component.base.Component;

public class SlotComponent extends Component {

    public static final int SIZE = 18;

    public SlotComponent(int x, int y, ComponentSlotLayout layout, String bgSprite, CFNBaseGui<?> gui) {
        super(x, y, SIZE, SIZE, gui);
        setSpriteLayers(bgSprite);
        bindLayout(layout);
    }

    public SlotComponent(int x, int y, int w, int h, ComponentSlotLayout layout, String bgSprite, CFNBaseGui<?> gui) {
        super(x, y, w, h, gui);
        setSpriteLayers(bgSprite);
        bindLayout(layout);
    }
}