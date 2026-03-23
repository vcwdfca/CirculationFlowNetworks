package com.circulation.circulation_networks.gui.component;

import com.circulation.circulation_networks.container.ComponentSlotLayout;
import com.circulation.circulation_networks.gui.CFNBaseGui;
import com.circulation.circulation_networks.gui.component.base.Component;

public class InventoryComponent extends Component {

    public static final int WIDTH = 164;
    public static final int HEIGHT = 78;

    public InventoryComponent(int x, int y, CFNBaseGui<?> gui) {
        super(x, y, WIDTH, HEIGHT, gui);
        setSpriteLayers("bg/inventory");
    }

    public InventoryComponent(int x, int y, ComponentSlotLayout layout, CFNBaseGui<?> gui) {
        this(x, y, gui);
        bindLayout(layout);
    }

}