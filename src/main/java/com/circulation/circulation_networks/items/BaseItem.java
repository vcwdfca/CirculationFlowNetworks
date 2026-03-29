package com.circulation.circulation_networks.items;

import com.circulation.circulation_networks.tooltip.LocalizedComponent;
import com.circulation.circulation_networks.utils.CI18n;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
//~ mc_imports
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

//? if <1.20 {
import com.circulation.circulation_networks.CirculationFlowNetworks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
import static com.circulation.circulation_networks.CirculationFlowNetworks.CREATIVE_TAB;
//?} else if <1.21 {
/*import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;
*///?} else {
/*import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import java.util.List;
*///?}

public abstract class BaseItem extends Item {

    private String[] cachedAutoTooltipKeys;

    //? if <1.20 {
    protected BaseItem(String name) {
        this.setRegistryName(new ResourceLocation(CirculationFlowNetworks.MOD_ID, name));
        this.setTranslationKey(CirculationFlowNetworks.MOD_ID + "." + name);
        this.setCreativeTab(CREATIVE_TAB);
    }
    //?} else {
    /*protected BaseItem(Properties properties) {
        super(properties);
    }
    *///?}

    protected List<LocalizedComponent> buildTooltips(ItemStack stack) {
        if (cachedAutoTooltipKeys == null) {
            //~ if >=1.20 'getTranslationKey(stack)' -> 'getDescriptionId(stack)' {
            cachedAutoTooltipKeys = BaseItemTooltipModel.resolveTooltipKeys(getTranslationKey(stack), CI18n::hasKey);
            //~}
        }
        List<LocalizedComponent> result = new ObjectArrayList<>();
        for (var key : cachedAutoTooltipKeys) {
            result.add(LocalizedComponent.of(key));
        }
        return result;
    }

    //? if <1.20 {
    @Override
    @SideOnly(Side.CLIENT)
    public final void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip, @NotNull ITooltipFlag flagIn) {
        for (var lc : buildTooltips(stack)) {
            tooltip.add(lc.get());
        }
    }
    //?} else if <1.21 {
    /*@Override
    public final void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        for (var lc : buildTooltips(stack)) {
            tooltip.add(Component.literal(lc.get()));
        }
    }
    *///?} else {
    /*@Override
    public final void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        for (var lc : buildTooltips(stack)) {
            tooltip.add(Component.literal(lc.get()));
        }
    }
    *///?}
}
