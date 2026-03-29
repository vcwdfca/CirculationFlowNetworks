package com.circulation.circulation_networks.container;

//? if <1.20 {
import net.minecraft.inventory.IInventory;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
//?} else if <1.21 {
/*import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
*///?} else {
/*import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
*///?}

public class ComponentSlot extends SlotItemHandler {

    private final int relX;
    private final int relY;
    private boolean visible = true;

    //? if <1.20 {
    public ComponentSlot(IInventory inventory, int index, int relX, int relY) {
        this(new InvWrapper(inventory), index, relX, relY);
    }
    //?}

    public ComponentSlot(IItemHandler inventory, int index, int relX, int relY) {
        super(inventory, index, relX, relY);
        this.relX = relX;
        this.relY = relY;
    }

    public int getRelX() {
        return relX;
    }

    public int getRelY() {
        return relY;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    //~ if >=1.20 'isEnabled()' -> 'isActive()' {
    public boolean isEnabled() {
    //~}
        return visible;
    }
}