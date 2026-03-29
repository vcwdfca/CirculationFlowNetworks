package com.circulation.circulation_networks.api;

import com.circulation.circulation_networks.api.node.INode;
//~ mc_imports
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@FunctionalInterface
//~ if >=1.20 'NBTTagCompound' -> 'CompoundTag' {
public interface NodeDeserializer extends Function<NBTTagCompound, INode> {
    @Override
    @NotNull
    INode apply(NBTTagCompound tag);
}
//~}