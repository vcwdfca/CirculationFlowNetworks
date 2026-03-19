package com.circulation.circulation_networks.network.nodes;

import com.circulation.circulation_networks.api.INodeBlockEntity;
import com.circulation.circulation_networks.api.hub.ChargingDefinition;
import com.circulation.circulation_networks.api.hub.ChargingPreference;
import com.circulation.circulation_networks.api.hub.IHubPlugin;
import com.circulation.circulation_networks.api.hub.PermissionMode;
import com.circulation.circulation_networks.api.node.IHubNode;
import com.circulation.circulation_networks.items.HubChannelPluginData;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
//? if <1.20 {
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
//?} else {
/*import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
*///?}
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public final class HubNode extends Node implements IHubNode {

    private final double energyScope;
    private final double energyScopeSq;
    private final double chargingScope;
    private final double chargingScopeSq;

    private final ItemStack[] plugins = new ItemStack[PLUGIN_SLOTS];
    private final Map<UUID, ChargingPreference> playerPreferences = new Object2ObjectOpenHashMap<>();
    private PermissionMode permissionMode = PermissionMode.PUBLIC;
    @Nullable
    private UUID owner;
    @Nullable
    private UUID channelId;
    @Nullable
    private String channelName;

    {
        Arrays.fill(plugins, ItemStack.EMPTY);
    }

    //? if <1.20 {
    public HubNode(NBTTagCompound tag) {
        //?} else {
    /*public HubNode(CompoundTag tag) {
     *///?}
        super(tag);
        this.energyScope = tag.getDouble("energyScope");
        this.energyScopeSq = energyScope * energyScope;
        this.chargingScope = tag.getDouble("chargingScope");
        this.chargingScopeSq = chargingScope * chargingScope;
        deserializeHubData(tag);
    }

    public HubNode(INodeBlockEntity blockEntity, double energyScope, double chargingScope, double linkScope) {
        super(blockEntity, linkScope);
        this.energyScope = energyScope;
        this.energyScopeSq = energyScope * energyScope;
        this.chargingScope = chargingScope;
        this.chargingScopeSq = chargingScope * chargingScope;
    }

    @Override
    public double getEnergyScope() {
        return energyScope;
    }

    @Override
    public double getEnergyScopeSq() {
        return energyScopeSq;
    }

    @Override
    public double getChargingScope() {
        return chargingScope;
    }

    @Override
    public double getChargingScopeSq() {
        return chargingScopeSq;
    }

    @Override
    public PermissionMode getPermissionMode() {
        return permissionMode;
    }

    @Override
    public void setPermissionMode(PermissionMode mode) {
        this.permissionMode = mode;
    }

    @Override
    public ItemStack[] getPlugins() {
        return plugins;
    }

    @Override
    public void setPlugin(int slot, ItemStack stack) {
        if (slot < 0 || slot >= plugins.length) return;

        var oldStack = plugins[slot];
        if (!oldStack.isEmpty() && oldStack.getItem() instanceof IHubPlugin oldPlugin) {
            oldPlugin.onRemoved(this, slot, oldStack);
        }

        plugins[slot] = stack == null ? ItemStack.EMPTY : stack;

        if (!plugins[slot].isEmpty() && plugins[slot].getItem() instanceof IHubPlugin newPlugin) {
            newPlugin.onInserted(this, slot, plugins[slot]);
        }
    }

    @Override
    public @Nullable UUID getChannelId() {
        return channelId;
    }

    @Override
    public void setChannelId(@Nullable UUID channelId) {
        this.channelId = channelId;
    }

    @Override
    public @Nullable String getChannelName() {
        return channelName;
    }

    @Override
    public void setChannelName(@Nullable String channelName) {
        this.channelName = channelName;
    }

    @Override
    public @NotNull ChargingPreference getChargingPreference(UUID playerId) {
        return playerPreferences.computeIfAbsent(playerId, k -> ChargingPreference.defaultAll());
    }

    @Override
    public void setChargingPreference(UUID playerId, ChargingPreference preference) {
        playerPreferences.put(playerId, preference);
    }

    @Override
    public boolean getChargingState(UUID playerId, ChargingDefinition chargingDefinition) {
        return getChargingPreference(playerId).getPreference(chargingDefinition);
    }

    @Override
    public void setChargingState(UUID playerId, ChargingDefinition chargingDefinition, boolean value) {
        getChargingPreference(playerId).setPreference(chargingDefinition, value);
    }

    @Override
    public @Nullable UUID getOwner() {
        return owner;
    }

    @Override
    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
    }

    //? if <1.20 {
    @Override
    public NBTTagCompound serialize() {
        var nbt = super.serialize();
        nbt.setDouble("energyScope", energyScope);
        nbt.setDouble("chargingScope", chargingScope);
        serializeHubData(nbt);
        return nbt;
    }

    private void serializeHubData(NBTTagCompound nbt) {
        nbt.setInteger("permissionMode", permissionMode.getId());

        if (owner != null) {
            nbt.setString("ownerUUID", owner.toString());
        }

        var pluginList = new NBTTagList();
        for (var plugin : plugins) {
            pluginList.appendTag(plugin.writeToNBT(new NBTTagCompound()));
        }
        nbt.setTag("plugins", pluginList);

        var prefList = new NBTTagList();
        for (var entry : playerPreferences.entrySet()) {
            var prefNbt = entry.getValue().serialize();
            prefNbt.setString("playerUUID", entry.getKey().toString());
            prefList.appendTag(prefNbt);
        }
        nbt.setTag("chargingPreferences", prefList);
    }

    private void deserializeHubData(NBTTagCompound nbt) {
        permissionMode = PermissionMode.fromId(nbt.getInteger("permissionMode"));

        if (nbt.hasKey("ownerUUID")) {
            try {
                owner = UUID.fromString(nbt.getString("ownerUUID"));
            } catch (IllegalArgumentException ignored) {
                owner = null;
            }
        }

        if (nbt.hasKey("plugins", Constants.NBT.TAG_LIST)) {
            var pluginList = nbt.getTagList("plugins", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < Math.min(pluginList.tagCount(), plugins.length); i++) {
                plugins[i] = new ItemStack(pluginList.getCompoundTagAt(i));
            }
        }

        playerPreferences.clear();
        if (nbt.hasKey("chargingPreferences", Constants.NBT.TAG_LIST)) {
            var prefList = nbt.getTagList("chargingPreferences", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < prefList.tagCount(); i++) {
                var prefNbt = prefList.getCompoundTagAt(i);
                if (prefNbt.hasKey("playerUUID")) {
                    try {
                        var playerId = UUID.fromString(prefNbt.getString("playerUUID"));
                        playerPreferences.put(playerId, ChargingPreference.deserialize(prefNbt));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        }

        channelId = null;
        channelName = null;
        for (var plugin : plugins) {
            if (!plugin.isEmpty()) {
                channelId = HubChannelPluginData.getChannelId(plugin);
                channelName = HubChannelPluginData.getChannelName(plugin);
                if (HubChannelPluginData.isComplete(channelId, channelName)) {
                    break;
                }
            }
        }
    }
    //?} else {
    /*@Override
    public CompoundTag serialize() {
        var nbt = super.serialize();
        nbt.putDouble("energyScope", energyScope);
        nbt.putDouble("chargingScope", chargingScope);
        serializeHubData(nbt);
        return nbt;
    }

    private void serializeHubData(CompoundTag nbt) {
        nbt.putInt("permissionMode", permissionMode.getId());

        if (owner != null) {
            nbt.putString("ownerUUID", owner.toString());
        }

        var pluginList = new ListTag();
        for (var plugin : plugins) {
            pluginList.add(plugin.save(new CompoundTag()));
        }
        nbt.put("plugins", pluginList);

        var prefList = new ListTag();
        for (var entry : playerPreferences.entrySet()) {
            var prefNbt = entry.getValue().serialize();
            prefNbt.putString("playerUUID", entry.getKey().toString());
            prefList.add(prefNbt);
        }
        nbt.put("chargingPreferences", prefList);
    }

    private void deserializeHubData(CompoundTag nbt) {
        permissionMode = PermissionMode.fromId(nbt.getInt("permissionMode"));

        if (nbt.contains("ownerUUID")) {
            try {
                owner = UUID.fromString(nbt.getString("ownerUUID"));
            } catch (IllegalArgumentException ignored) {
                owner = null;
            }
        }

        if (nbt.contains("plugins", Tag.TAG_LIST)) {
            var pluginList = nbt.getList("plugins", Tag.TAG_COMPOUND);
            for (int i = 0; i < Math.min(pluginList.size(), plugins.length); i++) {
                plugins[i] = ItemStack.of(pluginList.getCompound(i));
            }
        }

        playerPreferences.clear();
        if (nbt.contains("chargingPreferences", Tag.TAG_LIST)) {
            var prefList = nbt.getList("chargingPreferences", Tag.TAG_COMPOUND);
            for (int i = 0; i < prefList.size(); i++) {
                var prefNbt = prefList.getCompound(i);
                if (prefNbt.contains("playerUUID")) {
                    try {
                        var playerId = UUID.fromString(prefNbt.getString("playerUUID"));
                        playerPreferences.put(playerId, ChargingPreference.deserialize(prefNbt));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        }

        channelId = null;
        channelName = null;
        for (var plugin : plugins) {
            if (!plugin.isEmpty()) {
                channelId = HubChannelPluginData.getChannelId(plugin);
                channelName = HubChannelPluginData.getChannelName(plugin);
                if (HubChannelPluginData.isComplete(channelId, channelName)) {
                    break;
                }
            }
        }
    }
    *///?}
}