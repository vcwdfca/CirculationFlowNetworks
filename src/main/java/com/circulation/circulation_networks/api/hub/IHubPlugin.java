package com.circulation.circulation_networks.api.hub;

import com.circulation.circulation_networks.api.node.IHubNode;
//~ mc_imports
import net.minecraft.item.ItemStack;

/**
 * 中枢插件接口，物品实现此接口以成为可插入中枢的插件
 * <p>
 * Hub plugin interface. Items implementing this interface can be inserted into Hub plugin slots.
 */
public interface IHubPlugin {

    /**
     * 当插件被插入中枢时调用 / Called when plugin is inserted into a hub
     */
    void onInserted(IHubNode hub, int slot, ItemStack stack);

    /**
     * 当插件从中枢移除时调用 / Called when plugin is removed from a hub
     */
    void onRemoved(IHubNode hub, int slot, ItemStack stack);

    /**
     * 检查此插件是否可以插入指定槽位 / Check if plugin can be inserted into the specified slot
     */
    default boolean canInsert(IHubNode hub, ItemStack stack) {
        return true;
    }
}
