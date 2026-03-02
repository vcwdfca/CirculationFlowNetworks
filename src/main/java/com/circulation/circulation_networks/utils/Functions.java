package com.circulation.circulation_networks.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public final class Functions {

    @Nonnull
    public static NBTTagCompound getOrCreateTagCompound(ItemStack stack) {
        var nbt = stack.getTagCompound();
        if (nbt == null) {
            stack.setTagCompound(nbt = new NBTTagCompound());
        }
        return nbt;
    }

    public static long mergeChunkCoords(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    public static long mergeChunkCoords(BlockPos pos) {
        return mergeChunkCoords(pos.getX() >> 4, pos.getZ() >> 4);
    }
}
