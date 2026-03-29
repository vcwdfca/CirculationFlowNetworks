package com.circulation.circulation_networks.container;

//~ mc_imports
import net.minecraft.item.ItemStack;
//? if <1.20 {
import net.minecraft.inventory.IInventory;
//?}
//? if <1.21 {
import net.minecraftforge.items.IItemHandler;
//?} else {
/*import net.neoforged.neoforge.items.IItemHandler;
*///?}
import org.jetbrains.annotations.NotNull;

public class OutputComponentSlot extends ComponentSlot {

    //? if <1.20 {
    public OutputComponentSlot(IInventory inventory, int index, int relX, int relY) {
        super(inventory, index, relX, relY);
    }
    //?}

    public OutputComponentSlot(IItemHandler inventory, int index, int relX, int relY) {
        super(inventory, index, relX, relY);
    }

    @Override
    //~ if >=1.20 'isItemValid(' -> 'mayPlace(' {
    public boolean isItemValid(@NotNull ItemStack stack) {
    //~}
        return false;
    }
}