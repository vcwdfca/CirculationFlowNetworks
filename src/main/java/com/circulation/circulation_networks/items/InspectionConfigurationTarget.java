package com.circulation.circulation_networks.items;

import com.circulation.circulation_networks.api.node.INode;
import com.circulation.circulation_networks.registry.RegistryEnergyHandler;
//~ mc_imports
import net.minecraft.tileentity.TileEntity;

public final class InspectionConfigurationTarget {

    private InspectionConfigurationTarget() {
    }

    //~ if >=1.20 ' TileEntity ' -> ' BlockEntity ' {
    public static ValidationStatus validate(INode node, TileEntity blockEntity) {
        if (node != null) {
            return ValidationStatus.NODE_BLOCKED;
        }
        if (blockEntity == null) {
            return ValidationStatus.NO_TARGET;
        }
        if (RegistryEnergyHandler.isBlack(blockEntity) || !RegistryEnergyHandler.isEnergyTileEntity(blockEntity)) {
            return ValidationStatus.INVALID_TARGET;
        }
        return ValidationStatus.VALID;
    }
    //~}

    public enum ValidationStatus {
        NO_TARGET(null),
        NODE_BLOCKED("item.circulation_networks.inspection_tool.config.node_blocked"),
        INVALID_TARGET("item.circulation_networks.inspection_tool.config.invalid_target"),
        VALID(null);

        private final String messageKey;

        ValidationStatus(String messageKey) {
            this.messageKey = messageKey;
        }

        public String getMessageKey() {
            return messageKey;
        }

        public boolean hasMessage() {
            return messageKey != null;
        }
    }
}