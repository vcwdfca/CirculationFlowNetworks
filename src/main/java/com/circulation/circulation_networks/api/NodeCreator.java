package com.circulation.circulation_networks.api;

import com.circulation.circulation_networks.api.node.INode;
import com.circulation.circulation_networks.api.node.NodeContext;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@FunctionalInterface
public interface NodeCreator extends Function<NodeContext, INode> {
    @Override
    @NotNull
    INode apply(@NotNull NodeContext context);
}
