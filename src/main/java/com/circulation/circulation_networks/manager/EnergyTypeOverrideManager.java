package com.circulation.circulation_networks.manager;

import com.circulation.circulation_networks.api.IEnergyHandler;
import com.circulation.circulation_networks.events.BlockEntityLifeCycleEvent;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
//? if <1.20 {
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
//?} else {
/*import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
*///?}

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

public final class EnergyTypeOverrideManager {

    private static volatile EnergyTypeOverrideManager INSTANCE;

    private final Int2ObjectMap<Long2ObjectMap<IEnergyHandler.EnergyType>> overrides = new Int2ObjectOpenHashMap<>();
    private boolean m;

    private EnergyTypeOverrideManager() {
    }

    public static EnergyTypeOverrideManager get() {
        if (INSTANCE != null) return INSTANCE;
        INSTANCE = new EnergyTypeOverrideManager();
        INSTANCE.loadFromFile();
        return INSTANCE;
    }

    public static void onServerStop() {
        if (INSTANCE != null) {
            INSTANCE.saveToFile();
            INSTANCE.overrides.clear();
        }
        INSTANCE = null;
    }

    public static void save() {
        if (INSTANCE != null) {
            INSTANCE.saveToFile();
        }
    }

    //~ if >=1.20 '.toLong()' -> '.asLong()' {
    public void setOverride(int dim, BlockPos pos, IEnergyHandler.EnergyType type) {
        overrides.computeIfAbsent(dim, k -> new Long2ObjectOpenHashMap<>()).put(pos.toLong(), type);
        m = true;
    }

    public void clearOverride(int dim, BlockPos pos) {
        var dimMap = overrides.get(dim);
        if (dimMap != null) {
            dimMap.remove(pos.toLong());
            if (dimMap.isEmpty()) overrides.remove(dim);
        }
        m = true;
    }

    @Nullable
    public IEnergyHandler.EnergyType getOverride(int dim, BlockPos pos) {
        var dimMap = overrides.get(dim);
        if (dimMap == null) return null;
        return dimMap.get(pos.toLong());
    }
    //~}

    @Nullable
    public Long2ObjectMap<IEnergyHandler.EnergyType> getOverridesForDim(int dim) {
        return overrides.get(dim);
    }

    public void onBlockEntityInvalidate(BlockEntityLifeCycleEvent.Invalidate event) {
        if (isClientWorld(event.getWorld())) return;
        clearOverride(getDimensionId(event.getWorld()), event.getPos());
    }

    //? if <1.20 {
    private void loadFromFile() {
        File saveFile = new File(NetworkManager.getSaveFile(), "EnergyTypeOverride.dat");
        if (!saveFile.exists()) {
            return;
        }

        try {
            NBTTagCompound nbt = CompressedStreamTools.read(saveFile);
            if (nbt == null) return;

            overrides.clear();
            NBTTagList dims = nbt.getTagList("overrides", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < dims.tagCount(); i++) {
                NBTTagCompound dimTag = dims.getCompoundTagAt(i);
                int dim = dimTag.getInteger("dim");
                NBTTagList entries = dimTag.getTagList("entries", Constants.NBT.TAG_COMPOUND);
                Long2ObjectMap<IEnergyHandler.EnergyType> dimMap = new Long2ObjectOpenHashMap<>();
                for (int j = 0; j < entries.tagCount(); j++) {
                    NBTTagCompound entry = entries.getCompoundTagAt(j);
                    long pos = entry.getLong("pos");
                    int type = entry.getInteger("type");
                    var values = IEnergyHandler.EnergyType.values();
                    if (type >= 0 && type < values.length) {
                        dimMap.put(pos, values[type]);
                    }
                }
                if (!dimMap.isEmpty()) overrides.put(dim, dimMap);
            }
        } catch (IOException ignored) {
        }
    }

    private void saveToFile() {
        if (overrides.isEmpty() && !m) {
            return;
        }

        File saveFile = new File(NetworkManager.getSaveFile(), "EnergyTypeOverride.dat");
        NBTTagCompound nbt = new NBTTagCompound();

        NBTTagList dims = new NBTTagList();
        for (var dimEntry : overrides.int2ObjectEntrySet()) {
            NBTTagCompound dimTag = new NBTTagCompound();
            dimTag.setInteger("dim", dimEntry.getIntKey());
            NBTTagList entries = new NBTTagList();
            for (var posEntry : dimEntry.getValue().long2ObjectEntrySet()) {
                NBTTagCompound entry = new NBTTagCompound();
                entry.setLong("pos", posEntry.getLongKey());
                entry.setInteger("type", posEntry.getValue().ordinal());
                entries.appendTag(entry);
            }
            dimTag.setTag("entries", entries);
            dims.appendTag(dimTag);
        }
        nbt.setTag("overrides", dims);

        try {
            CompressedStreamTools.safeWrite(nbt, saveFile);
        } catch (IOException ignored) {
        }

        m = false;
    }
    //?} else {
    /*private void loadFromFile() {
        File saveFile = new File(NetworkManager.getSaveFile(), "EnergyTypeOverride.dat");
        if (!saveFile.exists()) {
            return;
        }

        try {
            CompoundTag nbt = NetworkManager.readCompressedNbt(saveFile);
            if (nbt == null) return;

            overrides.clear();
            ListTag dims = nbt.getList("overrides", Tag.TAG_COMPOUND);
            for (int i = 0; i < dims.size(); i++) {
                CompoundTag dimTag = dims.getCompound(i);
                int dim = dimTag.getInt("dim");
                ListTag entries = dimTag.getList("entries", Tag.TAG_COMPOUND);
                Long2ObjectMap<IEnergyHandler.EnergyType> dimMap = new Long2ObjectOpenHashMap<>();
                for (int j = 0; j < entries.size(); j++) {
                    CompoundTag entry = entries.getCompound(j);
                    long pos = entry.getLong("pos");
                    int type = entry.getInt("type");
                    var values = IEnergyHandler.EnergyType.values();
                    if (type >= 0 && type < values.length) {
                        dimMap.put(pos, values[type]);
                    }
                }
                if (!dimMap.isEmpty()) overrides.put(dim, dimMap);
            }
        } catch (IOException ignored) {
        }
    }

    private void saveToFile() {
        if (overrides.isEmpty() && !m) {
            return;
        }

        File saveFile = new File(NetworkManager.getSaveFile(), "EnergyTypeOverride.dat");
        CompoundTag nbt = new CompoundTag();

        ListTag dims = new ListTag();
        for (var dimEntry : overrides.int2ObjectEntrySet()) {
            CompoundTag dimTag = new CompoundTag();
            dimTag.putInt("dim", dimEntry.getIntKey());
            ListTag entries = new ListTag();
            for (var posEntry : dimEntry.getValue().long2ObjectEntrySet()) {
                CompoundTag entry = new CompoundTag();
                entry.putLong("pos", posEntry.getLongKey());
                entry.putInt("type", posEntry.getValue().ordinal());
                entries.add(entry);
            }
            dimTag.put("entries", entries);
            dims.add(dimTag);
        }
        nbt.put("overrides", dims);

        try {
            NetworkManager.writeCompressedNbt(nbt, saveFile);
        } catch (IOException ignored) {
        }

        m = false;
    }
    *///?}

    //~ if >=1.20 'net.minecraft.world.World' -> 'net.minecraft.world.level.Level' {
    //~ if >=1.20 '.isRemote' -> '.isClientSide' {
    //~ if >=1.20 '.provider.getDimension()' -> '.dimension().location().hashCode()' {
    private static boolean isClientWorld(net.minecraft.world.World world) {
        return world.isRemote;
    }

    private static int getDimensionId(net.minecraft.world.World world) {
        return world.provider.getDimension();
    }
    //~}
    //~}
    //~}
}