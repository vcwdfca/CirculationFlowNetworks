package com.circulation.circulation_networks.utils;

//~ mc_imports
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
//? if >=1.21 {
/*import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
*///?}

import javax.annotation.Nonnull;

public final class Functions {

    //? if <1.20 {
    @Nonnull
    public static NBTTagCompound getOrCreateTagCompound(ItemStack stack) {
        var nbt = stack.getTagCompound();
        if (nbt == null) {
            stack.setTagCompound(nbt = new NBTTagCompound());
        }
        return nbt;
    }
    //?} else if <1.21 {
    /*@Nonnull
    public static CompoundTag getOrCreateTagCompound(ItemStack stack) {
        return stack.getOrCreateTag();
    }
    *///?} else {
    /*@Nonnull
    public static CompoundTag getOrCreateTagCompound(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static void saveTagCompound(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
    *///?}

    public static long mergeChunkCoords(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    public static long mergeChunkCoords(BlockPos pos) {
        return mergeChunkCoords(pos.getX() >> 4, pos.getZ() >> 4);
    }
}
