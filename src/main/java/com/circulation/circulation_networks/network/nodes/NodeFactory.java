package com.circulation.circulation_networks.network.nodes;

import com.circulation.circulation_networks.api.NodeCreator;
import com.circulation.circulation_networks.api.node.INode;
import com.circulation.circulation_networks.api.node.NodeContext;
import com.circulation.circulation_networks.api.node.NodeType;
import com.circulation.circulation_networks.registry.NodeTypes;
import org.jetbrains.annotations.NotNull;

public final class NodeFactory {

    private NodeFactory() {
    }

    @SuppressWarnings("unchecked")
    public static <N extends INode> @NotNull N createNode(@NotNull NodeType<? extends N> nodeType, @NotNull NodeContext context) {
        NodeCreator creator = NodeTypes.getCreator(nodeType.id());
        if (creator == null) {
            throw new IllegalArgumentException("No creator registered for node type: " + nodeType.id());
        }
        return (N) creator.apply(context);
    }
}