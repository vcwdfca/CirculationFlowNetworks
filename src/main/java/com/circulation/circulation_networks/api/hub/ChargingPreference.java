package com.circulation.circulation_networks.api.hub;

//? if <1.20 {
import net.minecraft.nbt.NBTTagCompound;
//?} else {
/*import net.minecraft.nbt.CompoundTag;
*///?}

public class ChargingPreference {

    public static final ChargingPreference INSTANCE = defaultAll();

    private boolean chargeInventory;
    private boolean chargeHotbar;
    private boolean chargeBaubles;
    private boolean chargeMainHand;
    private boolean chargeOffHand;
    private boolean chargeArmorSlot;

    public ChargingPreference(boolean chargeInventory, boolean chargeHotbar, boolean chargeBaubles,
                              boolean chargeMainHand, boolean chargeOffHand, boolean chargeArmorSlot) {
        this.chargeInventory = chargeInventory;
        this.chargeHotbar = chargeHotbar;
        this.chargeBaubles = chargeBaubles;
        this.chargeMainHand = chargeMainHand;
        this.chargeOffHand = chargeOffHand;
        this.chargeArmorSlot = chargeArmorSlot;
    }

    public static ChargingPreference defaultAll() {
        return new ChargingPreference(true, true, true, true, true, true);
    }

    //? if <1.20 {
    public static ChargingPreference deserialize(NBTTagCompound nbt) {
        return new ChargingPreference(
            nbt.getBoolean("inv"),
            nbt.getBoolean("hotbar"),
            nbt.getBoolean("baubles"),
            nbt.getBoolean("mainHand"),
            nbt.getBoolean("offHand"),
            nbt.getBoolean("armorSlot")
        );
    }

    public boolean getPreference(ChargingDefinition cd) {
        return switch (cd) {
            case INVENTORY -> chargeInventory;
            case HOTBAR -> chargeHotbar;
            case BAUBLES -> chargeBaubles;
            case MAIN_HAND -> chargeMainHand;
            case OFF_HAND -> chargeOffHand;
            case ARMOR -> chargeArmorSlot;
        };
    }

    public void setPreference(ChargingDefinition cd, boolean value) {
        switch (cd) {
            case INVENTORY -> chargeInventory = value;
            case HOTBAR -> chargeHotbar = value;
            case BAUBLES -> chargeBaubles = value;
            case MAIN_HAND -> chargeMainHand = value;
            case OFF_HAND -> chargeOffHand = value;
            case ARMOR -> chargeArmorSlot = value;
        }
    }

    //? if <1.20 {
    public NBTTagCompound serialize() {
        var nbt = new NBTTagCompound();
        nbt.setBoolean("inv", chargeInventory);
        nbt.setBoolean("hotbar", chargeHotbar);
        nbt.setBoolean("baubles", chargeBaubles);
        nbt.setBoolean("mainHand", chargeMainHand);
        nbt.setBoolean("offHand", chargeOffHand);
        nbt.setBoolean("armorSlot", chargeArmorSlot);
        return nbt;
    }
    //?} else {
    /*public CompoundTag serialize() {
        var nbt = new CompoundTag();
        nbt.putBoolean("inv", chargeInventory);
        nbt.putBoolean("hotbar", chargeHotbar);
        nbt.putBoolean("baubles", chargeBaubles);
        nbt.putBoolean("mainHand", chargeMainHand);
        nbt.putBoolean("offHand", chargeOffHand);
        nbt.putBoolean("armorSlot", chargeArmorSlot);
        return nbt;
    }
    *///?}
}
