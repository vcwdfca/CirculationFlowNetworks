package com.circulation.circulation_networks.registry;

import com.circulation.circulation_networks.container.ContainerCirculationShielder;
import com.circulation.circulation_networks.container.ContainerHub;
import net.minecraft.world.inventory.MenuType;

public final class CFNMenuTypes {

    public static MenuType<ContainerHub> HUB_MENU;
    public static MenuType<ContainerCirculationShielder> CIRCULATION_SHIELDER_MENU;

    private CFNMenuTypes() {
    }
}
