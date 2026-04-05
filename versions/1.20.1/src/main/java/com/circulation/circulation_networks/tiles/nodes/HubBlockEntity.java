package com.circulation.circulation_networks.tiles.nodes;

import com.circulation.circulation_networks.api.IHubNodeBlockEntity;
import com.circulation.circulation_networks.api.hub.IHubPlugin;
import com.circulation.circulation_networks.api.node.IHubNode;
import com.circulation.circulation_networks.api.node.NodeType;
import com.circulation.circulation_networks.container.ContainerHub;
import com.circulation.circulation_networks.inventory.CFNInternalInventory;
import com.circulation.circulation_networks.inventory.CFNInternalInventoryHost;
import com.circulation.circulation_networks.inventory.CFNInventoryChangeOperation;
import com.circulation.circulation_networks.network.hub.HubPluginCapability;
import com.circulation.circulation_networks.network.nodes.HubNode;
import com.circulation.circulation_networks.network.nodes.HubPluginStateTracker;
import com.circulation.circulation_networks.registry.CFNBlockEntityTypes;
import com.circulation.circulation_networks.registry.CFNMenuTypes;
import com.circulation.circulation_networks.registry.NodeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class HubBlockEntity extends BaseNodeBlockEntity<IHubNode> implements IHubNodeBlockEntity, CFNInternalInventoryHost, MenuProvider {

    private final CFNInternalInventory plugins = new CFNInternalInventory(this, 5, 1).setInputFilter((inventory, slot, itemStack) -> {
        if (!(itemStack.getItem() instanceof IHubPlugin plugin)) {
            return false;
        }
        return isUniquePluginCapability(inventory, slot, plugin.getCapability());
    });
    private boolean init;
    private transient CompoundTag initNbt;

    public HubBlockEntity(BlockPos pos, BlockState state) {
        super(CFNBlockEntityTypes.HUB, pos, state);
    }

    static boolean isUniquePluginCapability(CFNInternalInventory inventory, int slot, HubPluginCapability<?> capability) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (i == slot) {
                continue;
            }

            ItemStack existing = inventory.getStackInSlot(i);
            if (existing.isEmpty() || !(existing.getItem() instanceof IHubPlugin plugin)) {
                continue;
            }

            if (plugin.getCapability() == capability) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected @NotNull NodeType<? extends IHubNode> getNodeType() {
        return NodeTypes.HUB;
    }

    @Override
    protected void onNodeBound(IHubNode node) {
        if (node instanceof HubNode hubNode) {
            hubNode.bindPlugins(getPlugins());
        }
        initializeHubPluginState();
        init = true;
    }

    public CFNInternalInventory getPlugins() {
        return plugins;
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        plugins.writeToNBT(compound, "plugins");
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        plugins.readFromNBT(nbt, "plugins");

        if (!init) {
            initNbt = nbt;
            init = true;
        } else {
            initializeHubPluginState();
        }
    }

    @Override
    protected void onServerValidate() {
        super.onServerValidate();
        if (initNbt != null) {
            initializeHubPluginState();
            initNbt = null;
        }
    }

    @Override
    protected void onClientValidate() {
        super.onClientValidate();
        if (initNbt != null) {
            initializeHubPluginState();
            initNbt = null;
        }
    }

    @Override
    public void onChangeInventory(CFNInternalInventory inventory, int slot, CFNInventoryChangeOperation operation, ItemStack oldStack, ItemStack newStack) {
        if (init) {
            HubPluginStateTracker.saveAllPluginData(getNode(), plugins);
            HubPluginStateTracker.savePluginData(getNode(), oldStack);
            HubPluginStateTracker.syncInventoryChange(getNode(), oldStack, newStack);
        }
        setChanged();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.circulation_networks.hub");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        syncNodeAfterNetworkInit();
        return new ContainerHub(CFNMenuTypes.HUB_MENU, containerId, player, getNode());
    }

    private void initializeHubPluginState() {
        HubPluginStateTracker.initializeFromInventory(getNode(), plugins);
    }
}
