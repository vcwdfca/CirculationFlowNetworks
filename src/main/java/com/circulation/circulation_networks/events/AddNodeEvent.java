package com.circulation.circulation_networks.events;

import com.circulation.circulation_networks.api.node.INode;
//~ mc_imports
import net.minecraft.tileentity.TileEntity;
//? if <1.20 {
import net.minecraftforge.fml.common.eventhandler.Cancelable;
//?} else if <1.21 {
/*import net.minecraftforge.eventbus.api.Cancelable;
*///?}

// Shared node-add event whose attached world object is treated as a block entity.
public class AddNodeEvent extends NodeEvent {

    //~ if >=1.20 ' TileEntity ' -> ' BlockEntity ' {
    //~ if >=1.20 '(TileEntity ' -> '(BlockEntity ' {
    private final TileEntity blockEntity;

    public AddNodeEvent(INode node, TileEntity blockEntity) {
        super(node);
        this.blockEntity = blockEntity;
    }

    public TileEntity getBlockEntity() {
        return blockEntity;
    }
    //~}
    //~}

    //? if <1.20 {
    @Cancelable
    public static class Pre extends AddNodeEvent {

        public Pre(INode node, TileEntity blockEntity) {
            super(node, blockEntity);
        }

        @Override
        public boolean isCancelable() {
            return true;
        }
    }
    //?} else if <1.21 {
    /*@Cancelable
    public static class Pre extends AddNodeEvent {

        public Pre(INode node, BlockEntity blockEntity) {
            super(node, blockEntity);
        }
    }
    *///?} else {
    /*public static class Pre extends AddNodeEvent {

        private boolean canceled;

        public Pre(INode node, BlockEntity blockEntity) {
            super(node, blockEntity);
        }

        public boolean isCancelable() {
            return true;
        }

        public boolean isCanceled() {
            return canceled;
        }

        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }
    }
    *///?}

    public static class Post extends AddNodeEvent {

        //~ if >=1.20 ' TileEntity ' -> ' BlockEntity ' {
        public Post(INode node, TileEntity blockEntity) {
            super(node, blockEntity);
        }
        //~}
    }
}