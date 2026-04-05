package com.circulation.circulation_networks.tiles.nodes;

import com.circulation.circulation_networks.api.INodeBlockEntity;
import com.circulation.circulation_networks.api.node.INode;
import com.circulation.circulation_networks.api.node.NodeContext;
import com.circulation.circulation_networks.api.node.NodeType;
import com.circulation.circulation_networks.manager.NetworkManager;
import com.circulation.circulation_networks.utils.Functions;
import com.circulation.circulation_networks.tiles.BaseTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public abstract class BaseNodeTileEntity<N extends INode> extends BaseTileEntity implements INodeBlockEntity {

    private N node;

    @Override
    public @NotNull N getNode() {
        return node;
    }

    @Nonnull
    protected abstract NodeType<? extends N> getNodeType();

    protected @NotNull NodeContext createNodeContext() {
        return NodeContext.fromWorld(world, pos);
    }

    protected void onNodeBound(@NotNull N node) {
    }

    @Override
    public final void invalidate() {
        super.invalidate();
        if (!this.world.isRemote) {
            onInvalidate();
        } else {
            onClientInvalidate();
        }
    }

    @Override
    public final void validate() {
        super.validate();
        if (!shouldInitializeNode()) {
            return;
        }
        if (!this.world.isRemote) {
            if (!NetworkManager.INSTANCE.isInit()) {
                return;
            }
            onValidate();
        } else {
            onClientValidate();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (node != null || world == null) {
            return;
        }
        if (!this.world.isRemote) {
            if (!NetworkManager.INSTANCE.isInit()) {
                return;
            }
            onValidate();
        } else {
            onClientValidate();
        }
    }

    public final void syncNodeAfterNetworkInit() {
        if (world == null || world.isRemote || !NetworkManager.INSTANCE.isInit() || !shouldInitializeNode()) {
            return;
        }
        onValidate();
    }

    protected void onInvalidate() {
        if (node != null) {
            NetworkManager.INSTANCE.removeNode(node);
        }
    }

    @SideOnly(Side.CLIENT)
    protected void onClientInvalidate() {
        if (node != null) {
            node.setActive(false);
        }
    }

    protected void onValidate() {
        var nodeType = getNodeType();
        INode existingNode = NetworkManager.INSTANCE.getNodeFromPos(world, pos);

        if (nodeType.matches(existingNode)) {
            if (node != existingNode) {
                if (node != null) {
                    node.setActive(false);
                }
                node = castNode(nodeType.cast(existingNode));
            }
        } else {
            if (!nodeType.matches(node)) {
                if (existingNode != null) {
                    NetworkManager.INSTANCE.removeNode(existingNode);
                }
                node = Functions.createNode(nodeType, createNodeContext());
            } else if (existingNode != null && existingNode != node) {
                NetworkManager.INSTANCE.removeNode(existingNode);
            }
        }
        onNodeBound(node);
        node.setActive(true);
    }

    @SideOnly(Side.CLIENT)
    protected void onClientValidate() {
        var nodeType = getNodeType();
        if (node == null || !nodeType.matches(node)) {
            node = Functions.createNode(nodeType, createNodeContext());
        }
        onNodeBound(node);
        node.setActive(true);
    }

    @Override
    public @NotNull World getWorld() {
        return world;
    }

    @Override
    public @NotNull BlockPos getNodePos() {
        return this.pos;
    }

    @Override
    public World getNodeWorld() {
        return this.world;
    }

    @SuppressWarnings("unchecked")
    private N castNode(INode node) {
        return (N) node;
    }

    private boolean shouldInitializeNode() {
        return world != null && world.isBlockLoaded(pos);
    }

}
