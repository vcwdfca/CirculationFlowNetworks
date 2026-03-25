package com.circulation.circulation_networks.api.hub;

//? if <1.20 {
import net.minecraft.nbt.NBTTagCompound;
//?} else {
/*import net.minecraft.nbt.CompoundTag;
 *///?}

public class ChargingPreference {

    public static final ChargingPreference INSTANCE = defaultAll();

    private static final int MASK_INVENTORY = 0x01; // bit 0
    private static final int MASK_HOTBAR    = 0x02; // bit 1
    private static final int MASK_MAIN_HAND = 0x04; // bit 2
    private static final int MASK_OFF_HAND  = 0x08; // bit 3
    private static final int MASK_ARMOR     = 0x10; // bit 4
    private static final int MASK_BAUBLES   = 0x20; // bit 5

    private byte prefs;

    public ChargingPreference(boolean chargeInventory, boolean chargeHotbar, boolean chargeBaubles,
                              boolean chargeMainHand, boolean chargeOffHand, boolean chargeArmorSlot) {
        this.prefs = 0;
        setPreference(ChargingDefinition.INVENTORY, chargeInventory);
        setPreference(ChargingDefinition.HOTBAR, chargeHotbar);
        setPreference(ChargingDefinition.BAUBLES, chargeBaubles);
        setPreference(ChargingDefinition.MAIN_HAND, chargeMainHand);
        setPreference(ChargingDefinition.OFF_HAND, chargeOffHand);
        setPreference(ChargingDefinition.ARMOR, chargeArmorSlot);
    }

    public ChargingPreference(byte prefs) {
        this.prefs = prefs;
    }

    public void setPrefs(byte prefs) {
        this.prefs = prefs;
    }

    public static ChargingPreference defaultAll() {
        return new ChargingPreference((byte) 0b00111111);
    }

    //? if <1.20 {
    public static ChargingPreference deserialize(NBTTagCompound nbt) {
        //?} else {
        /*public static ChargingPreference deserialize(CompoundTag nbt) {
         *///?}
        return new ChargingPreference(nbt.getByte("prefs"));
    }

    public boolean getPreference(ChargingDefinition cd) {
        int mask = getMask(cd);
        return (this.prefs & mask) != 0;
    }

    public void setPreference(ChargingDefinition cd, boolean value) {
        byte mask = getMask(cd);
        if (value) {
            this.prefs |= mask;  // 置 1
        } else {
            this.prefs &= (byte) ~mask; // 置 0
        }
    }

    private byte getMask(ChargingDefinition cd) {
        return switch (cd) {
            case INVENTORY -> MASK_INVENTORY;
            case HOTBAR    -> MASK_HOTBAR;
            case MAIN_HAND -> MASK_MAIN_HAND;
            case OFF_HAND  -> MASK_OFF_HAND;
            case ARMOR     -> MASK_ARMOR;
            case BAUBLES   -> MASK_BAUBLES;
        };
    }

    public byte toByte() {
        return this.prefs;
    }

    //? if <1.20 {
    public NBTTagCompound serialize() {
        var nbt = new NBTTagCompound();
        nbt.setByte("prefs", this.prefs);
        return nbt;
    }
    //?} else {
    /*public CompoundTag serialize() {
        var nbt = new CompoundTag();
        nbt.putByte("prefs", this.prefs);
        return nbt;
    }
    *///?}
}