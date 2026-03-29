package com.circulation.circulation_networks.manager;

import com.circulation.circulation_networks.api.node.INode;
import com.circulation.circulation_networks.events.AddNodeEvent;
import com.circulation.circulation_networks.events.RemoveNodeEvent;
//~ mc_imports
import net.minecraft.tileentity.TileEntity;
//? if <1.21 {
import net.minecraftforge.common.MinecraftForge;
//?} else {
/*import net.neoforged.neoforge.common.NeoForge;
*///?}
//? if <1.20 {
import net.minecraftforge.fml.common.eventhandler.EventBus;
//?} else if < 1.21 {
/*import net.minecraftforge.eventbus.api.IEventBus;
*///?} else {
/*import net.neoforged.bus.api.IEventBus;
*///?}

final class NodeEventHooks {

    //~ if >=1.20 'EventBus ' -> 'IEventBus ' {
    private static final EventBus eventBus;
    //~}

    static {
        //?if <1.21 {
        eventBus = MinecraftForge.EVENT_BUS;
        //?} else {
         /*eventBus = NeoForge.EVENT_BUS;
         *///?}
    }

    static void postRemoveNodePre(INode node) {
        eventBus.post(new RemoveNodeEvent.Pre(node));
    }

    static void postRemoveNodePost(INode node) {
        eventBus.post(new RemoveNodeEvent.Post(node));
    }

    //~ if >=1.20 'TileEntity tileEntity' -> 'BlockEntity blockEntity' {
    //~ if >=1.20 'tileEntity)' -> 'blockEntity)' {
    static boolean postAddNodePre(INode node, TileEntity tileEntity) {
        //? if <1.21 {
        return eventBus.post(new AddNodeEvent.Pre(node, tileEntity));
        //?} else {
        /*return eventBus.post(new AddNodeEvent.Pre(node, tileEntity)).isCanceled();
        *///?}
    }

    static void postAddNodePost(INode node, TileEntity tileEntity) {
        eventBus.post(new AddNodeEvent.Post(node, tileEntity));
    }
    //~}
    //~}
}