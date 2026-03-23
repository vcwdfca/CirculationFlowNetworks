package com.circulation.circulation_networks.api;

import com.circulation.circulation_networks.api.node.IHubNode;
import net.minecraft.inventory.IInventory;
import org.jetbrains.annotations.NotNull;

public interface IHubNodeBlockEntity extends INodeBlockEntity {

    @Override
    @NotNull IHubNode getNode();

    //? if <1.20 {
    IInventory getPlugins();
    //?} else {
    /*Container getPlugins();
     *///
}
