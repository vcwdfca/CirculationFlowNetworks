package com.circulation.circulation_networks.items;

import com.circulation.circulation_networks.api.node.IHubNode;
import com.circulation.circulation_networks.utils.Functions;
//? if <1.20 {
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
//?} else {
/*import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
*///?}
//? if >=1.21 {
/*import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
*///?}

import java.util.UUID;

import static com.circulation.circulation_networks.network.nodes.HubNode.EMPTY;

public final class HubChannelPluginData {

    private static final String CHANNEL_ID_KEY = "channelId";
    private static final String CHANNEL_NAME_KEY = "channelName";

    private HubChannelPluginData() {
    }

    public static void setChannelInfo(ItemStack stack, UUID channelId, String channelName) {
        if (stack.isEmpty() || channelId == null || channelName == null) {
            return;
        }
        var tag = Functions.getOrCreateTagCompound(stack);
        putString(tag, CHANNEL_ID_KEY, channelId.toString());
        putString(tag, CHANNEL_NAME_KEY, channelName);
        //? if >=1.21 {
        /*Functions.saveTagCompound(stack, tag);
        *///?}
    }

    public static UUID getChannelId(ItemStack stack) {
        if (stack.isEmpty()) {
            return EMPTY;
        }
        var tag = getTag(stack);
        if (tag != null && contains(tag, CHANNEL_ID_KEY)) {
            return parseChannelId(getString(tag, CHANNEL_ID_KEY));
        }
        return EMPTY;
    }

    public static String getChannelName(ItemStack stack) {
        if (stack.isEmpty()) {
            return "";
        }
        var tag = getTag(stack);
        if (tag != null && contains(tag, CHANNEL_NAME_KEY)) {
            return getString(tag, CHANNEL_NAME_KEY);
        }
        return "";
    }

    public static void applyToHub(IHubNode hub, ItemStack stack) {
        hub.setChannelId(getChannelId(stack));
        hub.setChannelName(getChannelName(stack));
    }

    public static void clearHub(IHubNode hub) {
        hub.setChannelId(EMPTY);
        hub.setChannelName("");
    }

    public static UUID parseChannelId(String rawChannelId) {
        if (rawChannelId == null || rawChannelId.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(rawChannelId);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public static boolean isComplete(UUID channelId, String channelName) {
        return channelId != null && channelName != null && !channelName.isEmpty();
    }

    //? if <1.20 {
    private static NBTTagCompound getTag(ItemStack stack) {
        return stack.getTagCompound();
    }

    private static boolean contains(NBTTagCompound tag, String key) {
        return tag.hasKey(key);
    }

    private static String getString(NBTTagCompound tag, String key) {
        return tag.getString(key);
    }

    private static void putString(NBTTagCompound tag, String key, String value) {
        tag.setString(key, value);
    }
    //?} else if <1.21 {
    /*private static CompoundTag getTag(ItemStack stack) {
        return stack.getTag();
    }

    private static boolean contains(CompoundTag tag, String key) {
        return tag.contains(key);
    }

    private static String getString(CompoundTag tag, String key) {
        return tag.getString(key);
    }

    private static void putString(CompoundTag tag, String key, String value) {
        tag.putString(key, value);
    }
    *///?} else {
    /*private static CompoundTag getTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag() : null;
    }

    private static boolean contains(CompoundTag tag, String key) {
        return tag.contains(key);
    }

    private static String getString(CompoundTag tag, String key) {
        return tag.getString(key);
    }

    private static void putString(CompoundTag tag, String key, String value) {
        tag.putString(key, value);
    }
    *///?}
}