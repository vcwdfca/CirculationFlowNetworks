package com.circulation.circulation_networks.items;

//~ mc_imports
import net.minecraft.item.ItemStack;

public final class InspectionToolToggleFeedback {

    private final InspectionToolSelection selection;
    private final OverlaySyncAction overlaySyncAction;

    private InspectionToolToggleFeedback(InspectionToolSelection selection, OverlaySyncAction overlaySyncAction) {
        this.selection = selection;
        this.overlaySyncAction = overlaySyncAction;
    }

    public static InspectionToolToggleFeedback toggle(ItemStack stack) {
        var toggleResult = InspectionToolState.toggleFunction(stack);
        var selection = InspectionToolSelection.fromStack(stack);
        var overlaySyncAction = OverlaySyncAction.NONE;
        if (toggleResult.currentFunction() == InspectionToolModeModel.ToolFunction.CONFIGURATION) {
            overlaySyncAction = OverlaySyncAction.FULL_SYNC;
        } else if (toggleResult.previousFunction() == InspectionToolModeModel.ToolFunction.CONFIGURATION) {
            overlaySyncAction = OverlaySyncAction.CLEAR;
        }
        return new InspectionToolToggleFeedback(selection, overlaySyncAction);
    }

    public InspectionToolSelection selection() {
        return selection;
    }

    public OverlaySyncAction overlaySyncAction() {
        return overlaySyncAction;
    }

    public enum OverlaySyncAction {
        NONE,
        FULL_SYNC,
        CLEAR
    }
}