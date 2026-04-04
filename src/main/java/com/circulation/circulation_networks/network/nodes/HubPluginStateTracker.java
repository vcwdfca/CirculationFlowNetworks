package com.circulation.circulation_networks.network.nodes;

import com.circulation.circulation_networks.api.hub.IHubPlugin;
import com.circulation.circulation_networks.api.node.IHubNode;
import com.circulation.circulation_networks.items.HubChannelPluginData;
import com.circulation.circulation_networks.network.hub.HubCapabilitys;
import com.circulation.circulation_networks.network.hub.HubPluginCapability;
//~ mc_imports
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class HubPluginStateTracker {

    private HubPluginStateTracker() {
    }

    public static void initializeFromInventory(IHubNode node, Iterable<ItemStack> plugins) {
        if (!(node instanceof HubNode hubNode)) {
            return;
        }

        clearAllPluginState(hubNode);
        for (ItemStack stack : plugins) {
            installPlugin(hubNode, stack);
        }
    }

    public static void syncInventoryChange(IHubNode node, ItemStack oldStack, ItemStack newStack) {
        if (!(node instanceof HubNode hubNode)) {
            return;
        }

        HubPluginCapability<?> oldCapability = getCapability(oldStack);
        HubPluginCapability<?> newCapability = getCapability(newStack);

        if (oldCapability != null) {
            removePlugin(hubNode, oldCapability);
        }
        if (newCapability != null) {
            installPlugin(hubNode, newStack);
        }
    }

    public static void saveAllPluginData(IHubNode node, Iterable<ItemStack> plugins) {
        for (ItemStack stack : plugins) {
            savePluginData(node, stack);
        }
    }

    public static void savePluginData(IHubNode node, ItemStack stack) {
        if (node == null || stack.isEmpty() || !(stack.getItem() instanceof IHubPlugin plugin)) {
            return;
        }

        HubPluginCapability<?> capability = plugin.getCapability();
        if (capability == null || !node.getHubData().hasKey(capability)) {
            return;
        }

        if (capability == HubCapabilitys.CHANNEL_CAPABILITY) {
            capability.saveDataRaw(HubChannelPluginData.getChannelInfo(node), stack);
            return;
        }

        capability.saveDataRaw(node.getHubData().get(capability), stack);
    }

    private static void clearAllPluginState(HubNode node) {
        removePlugin(node, HubCapabilitys.CHANNEL_CAPABILITY);
        removePlugin(node, HubCapabilitys.CHARGE_CAPABILITY);
    }

    private static void installPlugin(HubNode node, ItemStack stack) {
        HubPluginCapability<?> capability = getCapability(stack);
        if (capability == null) {
            return;
        }

        node.removePluginData(capability);
        node.putPluginDataIfAbsent(capability, stack);

        if (capability == HubCapabilitys.CHANNEL_CAPABILITY) {
            HubChannelPluginData.ChannelInfo channelInfo = node.getHubData().get(HubCapabilitys.CHANNEL_CAPABILITY);
            if (HubChannelPluginData.isComplete(channelInfo)) {
                HubChannelPluginData.applyToHub(node, channelInfo);
            }
        }
    }

    private static void removePlugin(HubNode node, HubPluginCapability<?> capability) {
        if (capability == null) {
            return;
        }

        node.removePluginData(capability);
        if (capability == HubCapabilitys.CHANNEL_CAPABILITY) {
            node.clearChannelBinding();
        }
    }

    private static @Nullable HubPluginCapability<?> getCapability(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IHubPlugin plugin)) {
            return null;
        }
        return plugin.getCapability();
    }
}
