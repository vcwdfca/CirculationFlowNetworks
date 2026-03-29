package com.circulation.circulation_networks.items;

import com.circulation.circulation_networks.api.IEnergyHandler;
import com.circulation.circulation_networks.items.InspectionToolModeModel.ConfigurationMode;
import com.circulation.circulation_networks.manager.EnergyTypeOverrideManager;
//~ mc_imports
import net.minecraft.util.math.BlockPos;

public final class InspectionConfigurationApplyResult {

    private final OverlayAction overlayAction;
    private final long packedPos;
    private final IEnergyHandler.EnergyType energyType;
    private final String messageKey;
    private final String detailKey;

    private InspectionConfigurationApplyResult(OverlayAction overlayAction, BlockPos pos, IEnergyHandler.EnergyType energyType, String messageKey, String detailKey) {
        this.overlayAction = overlayAction;
        //~ if >=1.20 '.toLong()' -> '.asLong()' {
        this.packedPos = pos.toLong();
        //~}
        this.energyType = energyType;
        this.messageKey = messageKey;
        this.detailKey = detailKey;
    }

    public static InspectionConfigurationApplyResult apply(EnergyTypeOverrideManager manager, int dim, BlockPos pos, ConfigurationMode mode) {
        if (mode == ConfigurationMode.CLEAR) {
            manager.clearOverride(dim, pos);
            return new InspectionConfigurationApplyResult(OverlayAction.REMOVE, pos, null,
                "item.circulation_networks.inspection_tool.config.cleared", null);
        }

        IEnergyHandler.EnergyType energyType = mode.getEnergyType();
        manager.setOverride(dim, pos, energyType);
        return new InspectionConfigurationApplyResult(OverlayAction.ADD, pos, energyType,
            "item.circulation_networks.inspection_tool.config.set", mode.getLangKey());
    }

    public OverlayAction overlayAction() {
        return overlayAction;
    }

    public long packedPos() {
        return packedPos;
    }

    public IEnergyHandler.EnergyType energyType() {
        return energyType;
    }

    public String messageKey() {
        return messageKey;
    }

    public String detailKey() {
        return detailKey;
    }

    public boolean hasDetailKey() {
        return detailKey != null;
    }

    public enum OverlayAction {
        ADD,
        REMOVE
    }
}