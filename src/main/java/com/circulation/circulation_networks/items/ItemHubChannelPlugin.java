package com.circulation.circulation_networks.items;

import com.circulation.circulation_networks.api.hub.IHubPlugin;
import com.circulation.circulation_networks.api.node.IHubNode;
import com.circulation.circulation_networks.tiles.nodes.TileEntityHub;
import com.circulation.circulation_networks.utils.Functions;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ItemHubChannelPlugin extends BaseItem implements IHubPlugin {

    public ItemHubChannelPlugin() {
        super("hub_channel_plugin");
        this.setMaxStackSize(1);
    }

    /**
     * 从物品NBT中获取频道UUID
     */
    @Nullable
    public UUID getChannelId(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound()) return null;
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey("channelId")) {
            try {
                return UUID.fromString(tag.getString("channelId"));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 从物品NBT中获取频道名称
     */
    @Nullable
    public String getChannelName(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound()) return null;
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey("channelName")) {
            return tag.getString("channelName");
        }
        return null;
    }

    /**
     * 设置物品的频道UUID和名称
     */
    public static void setChannelInfo(ItemStack stack, UUID channelId, String name) {
        if (stack.isEmpty() || channelId == null || name == null) return;
        NBTTagCompound tag = Functions.getOrCreateTagCompound(stack);
        tag.setString("channelId", channelId.toString());
        tag.setString("channelName", name);
    }

    @Override
    public void onInserted(IHubNode hub, int slot, ItemStack stack) {
        if (hub.getTileEntity() instanceof TileEntityHub te) {
            te.setChannelId(getChannelId(stack));
            te.setChannelName(getChannelName(stack));
        }
    }

    @Override
    public void onRemoved(IHubNode hub, int slot, ItemStack stack) {
        if (hub.getTileEntity() instanceof TileEntityHub te) {
            te.setChannelId(null);
            te.setChannelName(null);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip, @NotNull ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        var channelId = getChannelId(stack);
        var channelName = getChannelName(stack);
        if (channelId != null && channelName != null) {
            tooltip.add(I18n.format("item.circulation_networks.hub_channel_plugin.channel", channelName, channelId.toString()));
        } else {
            tooltip.add(I18n.format("item.circulation_networks.hub_channel_plugin.no_channel"));
        }
    }
}
