package com.circulation.circulation_networks.items;

import com.circulation.circulation_networks.items.InspectionToolModeModel.ToolFunction;
import com.circulation.circulation_networks.tooltip.LocalizedComponent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
//~ mc_imports
import net.minecraft.item.ItemStack;

import java.util.List;

public final class InspectionToolSelection {

    public static final String MODE_DISPLAY_KEY = "item.circulation_networks.inspection_tool.mode_display";
    public static final String CURRENT_MODE_KEY = "item.circulation_networks.inspection_tool.current_mode";
    public static final String CURRENT_SUBMODE_KEY = "item.circulation_networks.inspection_tool.current_submode";
    public static final String SWITCH_MODE_USAGE_KEY = "item.circulation_networks.inspection_tool.usage.switch_mode";
    public static final String SWITCH_SUBMODE_USAGE_KEY = "item.circulation_networks.inspection_tool.usage.switch_submode";

    private final ToolFunction function;
    private final int subMode;

    public InspectionToolSelection(ToolFunction function, int subMode) {
        this.function = function;
        this.subMode = subMode;
    }

    public static InspectionToolSelection fromStack(ItemStack stack) {
        return new InspectionToolSelection(InspectionToolState.getFunction(stack), InspectionToolState.getSubMode(stack));
    }

    public ToolFunction function() {
        return function;
    }

    public int subMode() {
        return subMode;
    }

    public String modeLangKey() {
        return function.getLangKey();
    }

    public String modeDisplayKey() {
        return MODE_DISPLAY_KEY;
    }

    public String currentModeDisplayKey() {
        return CURRENT_MODE_KEY;
    }

    public String currentSubModeDisplayKey() {
        return CURRENT_SUBMODE_KEY;
    }

    public String subModeLangKey() {
        return function.getSubModeLangKey(subMode);
    }

    public String descriptionLangKey() {
        return function.getDescriptionLangKey();
    }

    public String switchModeUsageKey() {
        return SWITCH_MODE_USAGE_KEY;
    }

    public String switchSubModeUsageKey() {
        return SWITCH_SUBMODE_USAGE_KEY;
    }

    public List<LocalizedComponent> tooltipLines() {
        List<LocalizedComponent> lines = new ObjectArrayList<>();
        lines.add(LocalizedComponent.withTranslatedArg(currentModeDisplayKey(), modeLangKey()));
        lines.add(LocalizedComponent.withTranslatedArg(currentSubModeDisplayKey(), subModeLangKey()));
        lines.add(LocalizedComponent.description(descriptionLangKey()));
        lines.add(LocalizedComponent.blank());
        lines.add(LocalizedComponent.of(switchModeUsageKey()));
        lines.add(LocalizedComponent.of(switchSubModeUsageKey()));
        return lines;
    }
}