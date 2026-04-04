package com.circulation.circulation_networks.api;

import com.circulation.circulation_networks.registry.RegistryEnergyHandler;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import com.circulation.circulation_networks.manager.EnergyMachineManager;
import com.circulation.circulation_networks.network.nodes.HubNode;
//~ mc_imports
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Queue;

public interface IEnergyHandler {

    Map<Class<? extends IEnergyHandler>, Queue<IEnergyHandler>> POOL = new Reference2ObjectOpenHashMap<>();

    //~ if >=1.20 '(TileEntity ' -> '(BlockEntity ' {
    static @org.jetbrains.annotations.Nullable IEnergyHandler release(TileEntity tileEntity, @Nullable HubNode.HubMetadata hubMetadata) {
        if (tileEntity instanceof IMachineNodeBlockEntity mbe) return mbe.getEnergyHandler();
        var m = RegistryEnergyHandler.getEnergyManager(tileEntity);
        if (m == null) return null;
        var q = POOL.get(m.getEnergyHandlerClass());
        var t = q == null || q.isEmpty() ? m.newBlockEntityInstance() : q.poll();
        return t.init(tileEntity, hubMetadata);
    }

    static @org.jetbrains.annotations.Nullable IEnergyHandler release(ItemStack stack, @Nullable HubNode.HubMetadata hubMetadata) {
        if (stack == null || stack.isEmpty()) return null;
        var m = RegistryEnergyHandler.getEnergyManager(stack);
        if (m == null) return null;
        var q = POOL.get(m.getEnergyHandlerClass());
        var t = q == null || q.isEmpty() ? m.newItemInstance() : q.poll();
        return t.init(stack, hubMetadata);
    }

    IEnergyHandler init(TileEntity tileEntity, @Nullable HubNode.HubMetadata hubMetadata);
    //~}

    IEnergyHandler init(ItemStack itemStack, @Nullable HubNode.HubMetadata hubMetadata);

    void clear();

    EnergyAmount receiveEnergy(EnergyAmount maxReceive, @Nullable HubNode.HubMetadata hubMetadata);

    EnergyAmount extractEnergy(EnergyAmount maxExtract, @Nullable HubNode.HubMetadata hubMetadata);

    EnergyAmount canExtractValue(@Nullable HubNode.HubMetadata hubMetadata);

    EnergyAmount canReceiveValue(@Nullable HubNode.HubMetadata hubMetadata);

    boolean canExtract(IEnergyHandler receiveHandler, @Nullable HubNode.HubMetadata hubMetadata);

    boolean canReceive(IEnergyHandler sendHandler, @Nullable HubNode.HubMetadata hubMetadata);

    default void recycle() {
        this.clear();
        var queue = POOL.get(this.getClass());
        //? if <1.20 {
        if (queue != null && queue.size() < EnergyMachineManager.INSTANCE.getMachineGridMap().size()) {
            queue.add(this);
        }
        //?} else {
        /*if (queue != null) {
            queue.add(this);
        }
        *///?}
    }

    EnergyType getType(@Nullable HubNode.HubMetadata hubMetadata);

    enum EnergyType {
        SEND,
        RECEIVE,
        STORAGE,
        INVALID
    }
}
