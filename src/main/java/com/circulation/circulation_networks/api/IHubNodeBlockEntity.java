package com.circulation.circulation_networks.api;

import com.circulation.circulation_networks.api.node.IHubNode;
//? if <1.21 {
import net.minecraftforge.items.IItemHandler;
//?} else {
/*import net.neoforged.neoforge.items.IItemHandler;
*///?}
import org.jetbrains.annotations.NotNull;

public interface IHubNodeBlockEntity extends INodeBlockEntity {

    @Override
    @NotNull IHubNode getNode();

    IItemHandler getPlugins();
}
