package com.circulation.circulation_networks.api;

import com.circulation.circulation_networks.api.node.IHubNode;
import com.circulation.circulation_networks.api.node.INode;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
//? if <1.20 {
import net.minecraft.nbt.NBTTagCompound;
//?} else {
/*import net.minecraft.nbt.CompoundTag;
*///?}

import javax.annotation.Nullable;
import java.util.UUID;

public interface IGrid {

    UUID getId();

    ReferenceSet<INode> getNodes();

    // Only the NBT carrier type differs between versions.
    //? if <1.20 {
    NBTTagCompound serialize();
    //?} else {
    /*CompoundTag serialize();
    *///?}

    /**
     * 获取此网络的中枢节点 / Get the hub node of this network
     *
     * @return 中枢节点，不存在时返回 null
     */
    @Nullable
    default IHubNode getHubNode() {
        return null;
    }

    /**
     * 设置此网络的中枢节点 / Set the hub node of this network
     */
    default void setHubNode(@Nullable IHubNode hub) {
    }
}
