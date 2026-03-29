package com.circulation.circulation_networks.inventory;

//~ mc_imports
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
//? if <1.21 {
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
//?} else {
/*import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
*///?}
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

//? if <1.20
@SuppressWarnings("unused")
public class CFNInternalInventory extends ItemStackHandler implements Iterable<ItemStack> {

    protected final int[] maxStack;
    protected ItemStack previousStack = ItemStack.EMPTY;
    @Nullable
    protected CFNInternalInventoryInputFilter inFilter;
    @Nullable
    protected CFNInternalInventoryOutputFilter outFilter;
    @Nullable
    protected CFNInternalInventoryHost host;
    protected boolean ignoreItemStackLimit;
    protected boolean dirtyFlag = false;

    public CFNInternalInventory(@Nullable CFNInternalInventoryHost host,
                                int size,
                                int maxStack,
                                boolean ignoreItemStackLimit) {
        super(size);
        this.host = host;
        this.ignoreItemStackLimit = ignoreItemStackLimit;
        this.maxStack = new int[size];
        Arrays.fill(this.maxStack, maxStack);
    }

    public CFNInternalInventory(@Nullable CFNInternalInventoryHost host, int size, int maxStack) {
        this(host, size, maxStack, false);
    }

    public CFNInternalInventory(@Nullable CFNInternalInventoryHost host, int size) {
        this(host, size, 64, false);
    }

    public CFNInternalInventory(@Nullable CFNInternalInventoryHost host, int size, boolean ignoreItemStackLimit) {
        this(host, size, 64, ignoreItemStackLimit);
    }

    public CFNInternalInventory setInputFilter(@Nullable CFNInternalInventoryInputFilter filter) {
        this.inFilter = filter;
        return  this;
    }

    public CFNInternalInventory setOutputFilter(@Nullable CFNInternalInventoryOutputFilter filter) {
        this.outFilter = filter;
        return  this;
    }

    public interface CFNInternalInventoryInputFilter {

        boolean allowInsert(CFNInternalInventory inventory, int slot, ItemStack stack);

    }

    public interface CFNInternalInventoryOutputFilter {

        boolean allowExtract(CFNInternalInventory inventory, int slot, int amount);

    }

    public void setHost(@Nullable CFNInternalInventoryHost host) {
        this.host = host;
    }

    public boolean isIgnoringItemStackLimit() {
        return ignoreItemStackLimit;
    }

    public void setIgnoreItemStackLimit(boolean ignoreItemStackLimit) {
        this.ignoreItemStackLimit = ignoreItemStackLimit;
    }

    @Override
    public int getSlotLimit(int slot) {
        return maxStack[slot];
    }

    public void setMaxStackSize(int slot, int size) {
        this.maxStack[slot] = size;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (stack != getStackInSlot(slot)) {
            previousStack = getStackInSlot(slot).copy();
        }
        ItemStack toSet = stack;
        if (!stack.isEmpty()) {
            int limit = getEffectiveStackLimit(slot, stack);
            if (stack.getCount() > limit) {
                toSet = copyStackWithSize(stack, limit);
            }
        }
        super.setStackInSlot(slot, toSet);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (maxStack[slot] == 0) {
            return false;
        }
        return inFilter == null || inFilter.allowInsert(this, slot, stack);
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (inFilter != null && !inFilter.allowInsert(this, slot, stack)) {
            return stack;
        }

        validateSlotIndex(slot);

        if (!simulate) {
            previousStack = getStackInSlot(slot).copy();
        }

        ItemStack existing = stacks.get(slot);
        int limit = getEffectiveStackLimit(slot, stack);

        if (!existing.isEmpty()) {
            if (!canItemStacksStack(stack, existing)) {
                return stack;
            }
            limit -= existing.getCount();
        }

        if (limit <= 0) {
            return stack;
        }

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate) {
            if (existing.isEmpty()) {
                stacks.set(slot, reachedLimit ? copyStackWithSize(stack, limit) : stack.copy());
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            onContentsChanged(slot);
        }

        return reachedLimit ? copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) {
            return ItemStack.EMPTY;
        }

        if (outFilter != null && !outFilter.allowExtract(this, slot, amount)) {
            return ItemStack.EMPTY;
        }

        validateSlotIndex(slot);

        if (!simulate) {
            previousStack = getStackInSlot(slot).copy();
        }

        ItemStack existing = stacks.get(slot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int toExtract = Math.min(amount, existing.getCount());
        if (!ignoreItemStackLimit) {
            toExtract = Math.min(toExtract, existing.getMaxStackSize());
        }

        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                stacks.set(slot, ItemStack.EMPTY);
                onContentsChanged(slot);
            }
            return existing.copy();
        }

        ItemStack extracted = copyStackWithSize(existing, toExtract);
        if (!simulate) {
            stacks.set(slot, copyStackWithSize(existing, existing.getCount() - toExtract));
            onContentsChanged(slot);
        }

        return extracted;
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (host != null && !dirtyFlag) {
            dirtyFlag = true;
            ItemStack newStack = getStackInSlot(slot).copy();
            ItemStack oldStack = previousStack.copy();
            CFNInventoryChangeOperation operation = CFNInventoryChangeOperation.SET;

            if (oldStack.isEmpty() && !newStack.isEmpty()) {
                operation = CFNInventoryChangeOperation.INSERT;
            } else if (!oldStack.isEmpty() && newStack.isEmpty()) {
                operation = CFNInventoryChangeOperation.EXTRACT;
            } else if (sameStackType(oldStack, newStack) && oldStack.getCount() != newStack.getCount()) {
                if (newStack.getCount() > oldStack.getCount()) {
                    ItemStack inserted = newStack.copy();
                    inserted.shrink(oldStack.getCount());
                    oldStack = ItemStack.EMPTY;
                    newStack = inserted;
                    operation = CFNInventoryChangeOperation.INSERT;
                } else {
                    ItemStack extracted = oldStack.copy();
                    extracted.shrink(newStack.getCount());
                    oldStack = extracted;
                    newStack = ItemStack.EMPTY;
                    operation = CFNInventoryChangeOperation.EXTRACT;
                }
            }

            host.onChangeInventory(this, slot, operation, oldStack, newStack);
            previousStack = ItemStack.EMPTY;
            dirtyFlag = false;
        }
        super.onContentsChanged(slot);
    }

    //? if <1.20 {
    public void writeToNBT(final NBTTagCompound data, final String name) {
        data.setTag(name, serializeNBT());
    }

    public void readFromNBT(final NBTTagCompound data, final String name) {
        final NBTTagCompound compound = data.getCompoundTag(name);
        if (compound != null) {
            readFromNBT(compound);
        }
    }

    public void readFromNBT(final NBTTagCompound data) {
        deserializeNBT(data);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagList itemList = new NBTTagList();
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (!stack.isEmpty()) {
                NBTTagCompound itemTag = stack.serializeNBT();
                itemTag.setInteger("Slot", i);
                itemList.appendTag(itemTag);
            }
        }
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("Items", itemList);
        nbt.setInteger("Size", stacks.size());
        return nbt;
    }
    //?} else if <1.21 {
    /*public void writeToNBT(final CompoundTag data, final String name) {
        data.put(name, serializeNBT());
    }

    public void readFromNBT(final CompoundTag data, final String name) {
        if (data.contains(name)) {
            readFromNBT(data.getCompound(name));
        }
    }

    public void readFromNBT(final CompoundTag data) {
        deserializeNBT(data);
    }
    *///?} else {
    /*public void writeToNBT(final CompoundTag data, final String name) {
        var provider = ServerLifecycleHooks.getCurrentServer().registryAccess();
        data.put(name, serializeNBT(provider));
    }

    public void readFromNBT(final CompoundTag data, final String name) {
        if (data.contains(name)) {
            readFromNBT(data.getCompound(name));
        }
    }

    public void readFromNBT(final CompoundTag data) {
        var provider = ServerLifecycleHooks.getCurrentServer().registryAccess();
        deserializeNBT(provider, data);
    }
    *///?}

    @Override
    public @NotNull Iterator<ItemStack> iterator() {
        return Collections.unmodifiableList(stacks).iterator();
    }

    private int getEffectiveStackLimit(int slot, @Nonnull ItemStack stack) {
        return ignoreItemStackLimit ? getSlotLimit(slot) : Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    private static ItemStack copyStackWithSize(ItemStack stack, int size) {
        //? if <1.21 {
        return ItemHandlerHelper.copyStackWithSize(stack, size);
        //?} else {
        /*return stack.copyWithCount(size);
        *///?}
    }

    private static boolean canItemStacksStack(ItemStack left, ItemStack right) {
        //? if <1.21 {
        return ItemHandlerHelper.canItemStacksStack(left, right);
        //?} else {
        /*return ItemStack.isSameItemSameComponents(left, right);
        *///?}
    }

    private static boolean sameStackType(ItemStack left, ItemStack right) {
        //? if <1.20 {
        return ItemStack.areItemsEqual(left, right) && ItemStack.areItemStackTagsEqual(left, right);
        //?} else if <1.21 {
        /*return ItemStack.isSameItemSameTags(left, right);
        *///?} else {
        /*return ItemStack.isSameItemSameComponents(left, right);
        *///?}
    }
}