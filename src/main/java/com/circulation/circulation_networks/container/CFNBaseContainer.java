package com.circulation.circulation_networks.container;

import com.circulation.circulation_networks.utils.GuiSyncManager;
import com.circulation.circulation_networks.utils.SyncSender;
import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
//? if <1.20 {
import com.circulation.circulation_networks.CirculationFlowNetworks;
import com.circulation.circulation_networks.packets.ContainerProgressBar;
import com.circulation.circulation_networks.packets.ContainerValueConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
//?} else {
/*import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
*///?}
import org.jetbrains.annotations.NotNull;

import java.util.List;

//? if <1.20 {
public abstract class CFNBaseContainer extends Container {

    protected final EntityPlayer player;
    //?} else {
/*public abstract class CFNBaseContainer extends AbstractContainerMenu {

    protected final Player player;
*///?}
    private final GuiSyncManager guiSyncManager = new GuiSyncManager();
    private final List<LayoutEntry> layouts = new ObjectArrayList<>();

    {
        guiSyncManager.scan(this, this::onUpdate);
    }

    //? if <1.20 {
    public CFNBaseContainer(EntityPlayer player) {
        this.player = player;
    }
    //?} else {
    /*public CFNBaseContainer(MenuType<?> menuType, int containerId, Player player) {
        super(menuType, containerId);
        this.player = player;
    }
    *///?}

    //? if >=1.20 {
    /*public Inventory getPlayerInventory() {
        return player.getInventory();
    }
    *///?}

    protected void registerLayout(ComponentSlotLayout layout) {
        registerLayoutInternal(layout, false);
    }

    public ComponentSlotLayout registerPlayerLayout(ComponentSlotLayout layout) {
        registerLayoutInternal(layout, true);
        return layout;
    }

    private void registerLayoutInternal(ComponentSlotLayout layout, boolean isPlayerInventory) {
        //? if <1.20 {
        int start = inventorySlots.size();
        layout.registerInto(this::addSlotToContainer);
        int end = inventorySlots.size();
        //?} else {
        /*int start = slots.size();
        layout.registerInto(this::addSlot);
        int end = slots.size();
        *///?}
        layouts.add(new LayoutEntry(layout, start, end, isPlayerInventory));
    }

    @Override
        //? if <1.20 {
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer playerIn, int index) {
        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return ItemStack.EMPTY;
        //?} else {
    /*public @NotNull ItemStack quickMoveStack(@NotNull Player playerIn, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
    *///?}

        //? if <1.20 {
        ItemStack stack = slot.getStack();
        //?} else {
        /*ItemStack stack = slot.getItem();
         *///?}
        ItemStack result = stack.copy();

        if (slot instanceof FilterComponentSlot) return result;

        LayoutEntry source = null;
        for (LayoutEntry e : layouts) {
            if (index >= e.start && index < e.end) {
                source = e;
                break;
            }
        }

        boolean merged = false;
        if (slot instanceof OutputComponentSlot) {
            for (LayoutEntry e : layouts) {
                if (e.isPlayerInventory) {
                    merged = mergeStack(stack, e.start, e.end, true);
                    if (merged) break;
                }
            }
            //? if <1.20 {
            if (merged) slot.onSlotChange(stack, result);
            //?} else {
            /*if (merged) slot.onQuickCraft(stack, result);
             *///?}
        } else if (source != null && source.isPlayerInventory) {
            for (LayoutEntry e : layouts) {
                if (!e.isPlayerInventory) {
                    merged = mergeStack(stack, e.start, e.end, false);
                    if (merged) break;
                }
            }
        } else {
            for (LayoutEntry e : layouts) {
                if (e.isPlayerInventory) {
                    merged = mergeStack(stack, e.start, e.end, false);
                    if (merged) break;
                }
            }
        }

        if (!merged) return ItemStack.EMPTY;
        //? if <1.20 {
        if (stack.isEmpty()) slot.putStack(ItemStack.EMPTY);
        else slot.onSlotChanged();
        //?} else {
        /*if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        *///?}
        if (stack.getCount() == result.getCount()) return ItemStack.EMPTY;
        slot.onTake(playerIn, stack);
        return result;
    }

    private boolean mergeStack(ItemStack stack, int start, int end, boolean reverseDirection) {
        //? if <1.20 {
        return mergeItemStack(stack, start, end, reverseDirection);
        //?} else {
        /*return moveItemStackTo(stack, start, end, reverseDirection);
         *///?}
    }

    private boolean handleFilterSlotClick(int slotId, int button, ClickType clickType, ItemStack carried) {
        //? if <1.20 {
        if (slotId < 0 || slotId >= inventorySlots.size()) return false;
        Slot slot = inventorySlots.get(slotId);
        //?} else {
        /*if (slotId < 0 || slotId >= slots.size()) return false;
        Slot slot = slots.get(slotId);
        *///?}
        if (!(slot instanceof FilterComponentSlot)) return false;
        if (clickType == ClickType.PICKUP) {
            ((FilterComponentSlot) slot).ghostClickWith(carried, button);
        }
        return true;
    }

    @Override
        //? if <1.20 {
    public @NotNull ItemStack slotClick(int slotId, int dragType, @NotNull ClickType clickTypeIn, @NotNull EntityPlayer player) {
        if (handleFilterSlotClick(slotId, dragType, clickTypeIn, player.inventory.getItemStack()))
            return ItemStack.EMPTY;
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }
    //?} else {
    /*public void clicked(int slotId, int button, @NotNull ClickType clickType, @NotNull Player player) {
        if (handleFilterSlotClick(slotId, button, clickType, getCarried())) return;
        super.clicked(slotId, button, clickType, player);
    }
    *///?}

    @Override
        //? if <1.20 {
    public boolean canInteractWith(@NotNull EntityPlayer playerIn) {
        //?} else {
        /*public boolean stillValid(@NotNull Player playerIn) {
         *///?}
        return true;
    }

    //? if <1.20 {
    public void detectAndSendChanges() {
        if (isServer()) {
            for (IContainerListener listener : this.listeners) {
                guiSyncManager.detectAndSendChanges(createSyncSender(listener));
            }
        }
        super.detectAndSendChanges();
    }

    private SyncSender createSyncSender(IContainerListener listener) {
        return new SyncSender() {
            @Override
            public void sendInt(int channel, int value) {
                listener.sendWindowProperty(CFNBaseContainer.this, channel, value);
            }

            @Override
            public void sendLong(int channel, long value) {
                if (listener instanceof EntityPlayerMP) {
                    CirculationFlowNetworks.sendToPlayer(
                        new ContainerProgressBar((short) channel, value),
                        (EntityPlayerMP) listener
                    );
                }
            }

            @Override
            public void sendByte(int channel, byte value) {
                listener.sendWindowProperty(CFNBaseContainer.this, channel, value);
            }

            @Override
            public void sendShort(int channel, short value) {
                listener.sendWindowProperty(CFNBaseContainer.this, channel, value);
            }

            @Override
            public void sendString(int channel, String value) {
                if (listener instanceof EntityPlayerMP) {
                    CirculationFlowNetworks.sendToPlayer(
                        new ContainerValueConfig((short) channel, value),
                        (EntityPlayerMP) listener
                    );
                }
            }
        };
    }
    //?} else {
    /*@Override
    public void broadcastChanges() {
        if (isServer()) {
            guiSyncManager.detectAndSendChanges(createSyncSender());
        }
        super.broadcastChanges();
    }

    private SyncSender createSyncSender() {
        // TODO: implement packet-based sync for 1.20+
        return new SyncSender() {
            @Override
            public void sendInt(int channel, int value) {
                if (player instanceof ServerPlayer sp) {
                    sp.containerMenu.sendAllDataToRemote();
                }
            }

            @Override
            public void sendLong(int channel, long value) {
                // TODO: implement via network packet
            }

            @Override
            public void sendByte(int channel, byte value) {
                // TODO: implement via network packet
            }

            @Override
            public void sendShort(int channel, short value) {
                // TODO: implement via network packet
            }

            @Override
            public void sendString(int channel, String value) {
                // TODO: implement via network packet
            }
        };
    }
    *///?}

    protected final boolean isServer() {
        //? if <1.20 {
        return !this.player.world.isRemote;
        //?} else {
        /*return !player.level().isClientSide;
         *///?}
    }

    public final void updateFullProgressBar(int idx, long value) {
        if (guiSyncManager.hasChannel(idx)) {
            guiSyncManager.updateField(idx, value);
        } else {
            //? if <1.20 {
            this.updateProgressBar(idx, (int) value);
            //?} else {
            /*this.cfnUpdateProgressBar(idx, (int) value);
             *///?}
        }
    }

    public final void stringSync(int idx, String value) {
        guiSyncManager.updateField(idx, value);
    }

    //? if <1.20 {
    public final void updateProgressBar(int idx, int value) {
        //?} else {
        /*public final void cfnUpdateProgressBar(int idx, int value) {
         *///?}
        guiSyncManager.updateField(idx, (long) value);
    }

    public void init() {
        guiSyncManager.init();
    }

    public void onUpdate(final String field, final Object oldValue, final Object newValue) {

    }

    //? if <1.20 {
    @Desugar
        //?}
    private record LayoutEntry(ComponentSlotLayout layout, int start, int end, boolean isPlayerInventory) {
    }
}