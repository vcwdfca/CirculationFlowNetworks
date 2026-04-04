package com.circulation.circulation_networks.container;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.NotNull;

public final class EmptyContainer extends CFNBaseContainer {

    public EmptyContainer(MenuType<?> menuType, int containerId, Player player) {
        super(menuType, containerId, player);
    }

    @Override
    public boolean stillValid(@NotNull Player playerIn) {
        return false;
    }
}
