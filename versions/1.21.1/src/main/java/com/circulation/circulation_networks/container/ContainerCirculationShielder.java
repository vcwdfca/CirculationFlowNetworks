package com.circulation.circulation_networks.container;

import com.circulation.circulation_networks.gui.GuiCirculationShielder;
import com.circulation.circulation_networks.tiles.CirculationShielderBlockEntity;
import com.circulation.circulation_networks.utils.GuiSync;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ContainerCirculationShielder extends CFNBaseContainer {

    public final CirculationShielderBlockEntity te;
    @GuiSync(0)
    public int scope;

    public ContainerCirculationShielder(MenuType<?> menuType, int containerId, Player player, CirculationShielderBlockEntity te) {
        super(menuType, containerId, player);
        this.te = te;
    }

    @Override
    public void broadcastChanges() {
        scope = te.getScope();
    }

    public void onUpdate(final String field, final Object oldValue, final Object newValue) {
        if (player.level().isClientSide && "scope".equals(field)) {
            onClient((Integer) newValue);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void onClient(Integer v) {
        if (Minecraft.getInstance().screen instanceof GuiCirculationShielder g) {
            g.scopeField.setValue(String.valueOf(v));
        }
    }
}
