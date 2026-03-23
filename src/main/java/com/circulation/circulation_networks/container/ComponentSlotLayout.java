package com.circulation.circulation_networks.container;

import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
//? if <1.20 {
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
//?} else {
/*import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
*///?}

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Shared descriptor that binds a set of inventory slots to a GUI component.
 */
@SuppressWarnings("unused")
public class ComponentSlotLayout {

    private final List<SlotSpec> specs = new ObjectArrayList<>();
    private final List<ComponentSlot> prebuilt = new ObjectArrayList<>();
    private final List<ComponentSlot> slots = new ObjectArrayList<>();
    private boolean registered = false;

    //? if <1.20 {
    public static ComponentSlotLayout playerInventory(InventoryPlayer inventoryPlayer) {
        //?} else {
        /*public static ComponentSlotLayout playerInventory(Inventory inventoryPlayer) {
         *///?}
        ComponentSlotLayout layout = new ComponentSlotLayout();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                layout.addSlot(inventoryPlayer, j + i * 9 + 9, 2 + j * 18, 2 + i * 18);
            }
        }
        for (int k = 0; k < 9; k++) {
            layout.addSlot(inventoryPlayer, k, 2 + k * 18, 60);
        }
        return layout;
    }

    public ComponentSlotLayout build(CFNBaseContainer container) {
        container.registerPlayerLayout(this);
        return this;
    }

    //? if <1.20 {
    public ComponentSlotLayout addSlot(IInventory inventory, int index, int relX, int relY) {
        //?} else {
        /*public ComponentSlotLayout addSlot(Container inventory, int index, int relX, int relY) {
         *///?}
        if (registered) {
            throw new IllegalStateException("Cannot add slot specs after registerInto() has been called");
        }
        specs.add(new SlotSpec(inventory, index, relX, relY));
        return this;
    }

    public ComponentSlotLayout addPrebuilt(ComponentSlot slot) {
        if (registered) {
            throw new IllegalStateException("Cannot add slots after registerInto() has been called");
        }
        prebuilt.add(slot);
        return this;
    }

    //? if <1.20 {
    public ComponentSlotLayout addOutput(IInventory inventory, int index, int relX, int relY) {
        //?} else {
        /*public ComponentSlotLayout addOutput(Container inventory, int index, int relX, int relY) {
         *///?}
        return addPrebuilt(new OutputComponentSlot(inventory, index, relX, relY));
    }

    //? if <1.20 {
    public ComponentSlotLayout addFilter(IInventory inventory, int index, int relX, int relY, int maxCount) {
        //?} else {
        /*public ComponentSlotLayout addFilter(Container inventory, int index, int relX, int relY, int maxCount) {
         *///?}
        return addPrebuilt(new FilterComponentSlot(inventory, index, relX, relY, maxCount));
    }

    public void registerInto(Consumer<ComponentSlot> adder) {
        if (registered) return;
        registered = true;
        for (SlotSpec spec : specs) {
            ComponentSlot slot = new ComponentSlot(spec.inventory, spec.index, spec.relX, spec.relY);
            adder.accept(slot);
            slots.add(slot);
        }
        for (ComponentSlot slot : prebuilt) {
            adder.accept(slot);
            slots.add(slot);
        }
    }

    public void syncPositions(int absX, int absY, boolean visible) {
        for (ComponentSlot slot : slots) {
            //? if <1.20 {
            slot.xPos = absX + slot.getRelX();
            slot.yPos = absY + slot.getRelY();
            //?} else {
            /*slot.x = absX + slot.getRelX();
            slot.y = absY + slot.getRelY();
            *///?}
            slot.setVisible(visible);
        }
    }

    public List<ComponentSlot> getSlots() {
        return Collections.unmodifiableList(slots);
    }

    //? if <1.20 {
    @Desugar
    private record SlotSpec(IInventory inventory, int index, int relX, int relY) { }
    //?} else {
    /*private record SlotSpec(Container inventory, int index, int relX, int relY) { }
     *///?}
}