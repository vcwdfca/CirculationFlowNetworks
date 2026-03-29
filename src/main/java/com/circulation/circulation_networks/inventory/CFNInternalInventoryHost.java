package com.circulation.circulation_networks.inventory;

//~ mc_imports
import net.minecraft.item.ItemStack;

public interface CFNInternalInventoryHost {

    void onChangeInventory(CFNInternalInventory inventory,
                           int slot,
                           CFNInventoryChangeOperation operation,
                           ItemStack oldStack,
                           ItemStack newStack);
}