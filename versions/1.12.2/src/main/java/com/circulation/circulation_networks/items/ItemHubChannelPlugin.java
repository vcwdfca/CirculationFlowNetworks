package com.circulation.circulation_networks.items;

import com.circulation.circulation_networks.api.hub.IHubPlugin;
import com.circulation.circulation_networks.api.node.IHubNode;
import com.circulation.circulation_networks.tooltip.LocalizedComponent;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ItemHubChannelPlugin extends BaseItem implements IHubPlugin {

    public ItemHubChannelPlugin() {
        super("hub_channel_plugin");
        this.setMaxStackSize(1);
    }

    /**
     * 设置物品的频道UUID和名称
     */
    public static void setChannelInfo(ItemStack stack, UUID channelId, String name) {
        HubChannelPluginData.setChannelInfo(stack, channelId, name);
    }

    /**
     * 从物品NBT中获取频道UUID
     */
    @Nullable
    public UUID getChannelId(ItemStack stack) {
        return HubChannelPluginData.getChannelId(stack);
    }

    /**
     * 从物品NBT中获取频道名称
     */
    @Nullable
    public String getChannelName(ItemStack stack) {
        return HubChannelPluginData.getChannelName(stack);
    }

    @Override
    public void onInserted(IHubNode hub, int slot, ItemStack stack) {
        HubChannelPluginData.applyToHub(hub, stack);
    }

    @Override
    public void onRemoved(IHubNode hub, int slot, ItemStack stack) {
        HubChannelPluginData.clearHub(hub);
    }

    @Override
    protected List<LocalizedComponent> buildTooltips(ItemStack stack) {
        List<LocalizedComponent> tips = super.buildTooltips(stack);
        var channelId = getChannelId(stack);
        var channelName = getChannelName(stack);
        if (HubChannelPluginData.isComplete(channelId, channelName)) {
            tips.add(LocalizedComponent.withArgs("item.circulation_networks.hub_channel_plugin.channel", () -> new Object[]{channelName, channelId.toString()}));
        } else {
            tips.add(LocalizedComponent.of("item.circulation_networks.hub_channel_plugin.no_channel"));
        }
        return tips;
    }
}
