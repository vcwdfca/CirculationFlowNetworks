package com.circulation.circulation_networks.items;

import com.circulation.circulation_networks.api.hub.IHubPlugin;
import com.circulation.circulation_networks.api.node.IHubNode;
//~ mc_imports
import net.minecraft.item.ItemStack;

public class ItemWideAreaChargingPlugin extends BaseItem implements IHubPlugin {

    //? if <1.20 {
    public ItemWideAreaChargingPlugin() {
        super("wide_area_charging_plugin");
        this.setMaxStackSize(1);
    }
    //?} else {
    /*public ItemWideAreaChargingPlugin(Properties properties) {
        super(properties.stacksTo(1));
    }
    *///?}

    @Override
    public void onInserted(IHubNode hub, int slot, ItemStack stack) {
    }

    @Override
    public void onRemoved(IHubNode hub, int slot, ItemStack stack) {
    }
}
