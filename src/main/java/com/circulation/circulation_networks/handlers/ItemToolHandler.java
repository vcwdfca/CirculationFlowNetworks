package com.circulation.circulation_networks.handlers;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import com.circulation.circulation_networks.items.CirculationConfiguratorModeModel;
import com.circulation.circulation_networks.items.CirculationConfiguratorSelection;
import com.circulation.circulation_networks.items.CirculationConfiguratorState;
import com.circulation.circulation_networks.packets.UpdateItemModeMessage;
import com.circulation.circulation_networks.registry.CFNItems;
import com.circulation.circulation_networks.utils.CI18n;
import net.minecraft.client.Minecraft;
//~ mc_imports
import net.minecraft.item.ItemStack;
//? if <1.20 {
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
//?} else if <1.21 {
/*import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
*///?} else {
/*import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
*///?}
//? if <1.20 {
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.MouseEvent;
import org.lwjgl.input.Mouse;
//?} else {
/*import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
//~ neo_imports
import net.minecraftforge.client.event.InputEvent;
*///?}

//~ if >=1.20 '@SideOnly(Side.CLIENT)' -> '@OnlyIn(Dist.CLIENT)' {
@SideOnly(Side.CLIENT)
//~}
public class ItemToolHandler {
    public static final ItemToolHandler INSTANCE = new ItemToolHandler();

    //~ if >=1.20 'Minecraft.getMinecraft()' -> 'Minecraft.getInstance()' {
    private final Minecraft mc = Minecraft.getMinecraft();
    //~}

    @SubscribeEvent
        //? if <1.20 {
    public void onMouseEvent(MouseEvent event) {
        if (mc.player != null && mc.player.isSneaking()) {
            ItemStack stack = mc.player.getHeldItemMainhand();
            int delta = CirculationConfiguratorModeModel.normalizeScrollDelta(Mouse.getEventDWheel());
            //?} else if <1.21 {
    /*public void onMouseEvent(InputEvent.MouseScrollingEvent event) {
        if (mc.player != null && mc.player.isShiftKeyDown()) {
            ItemStack stack = mc.player.getMainHandItem();
            int delta = CirculationConfiguratorModeModel.normalizeScrollDelta((int) event.getScrollDelta());
    *///?} else {
    /*public void onMouseEvent(InputEvent.MouseScrollingEvent event) {
        if (mc.player != null && mc.player.isShiftKeyDown()) {
            ItemStack stack = mc.player.getMainHandItem();
            int delta = CirculationConfiguratorModeModel.normalizeScrollDelta((int) event.getScrollDeltaY());
    *///?}
            if (delta != 0 && stack.getItem() == CFNItems.circulationConfigurator) {
                int mode = CirculationConfiguratorState.getSubMode(stack) + delta;
                CirculationConfiguratorState.setSubMode(stack, mode);

                //? if <1.20 {
                CirculationFlowNetworks.sendToServer(new UpdateItemModeMessage(mode));
                //?} else {
                /*CirculationFlowNetworks.sendToServer(new UpdateItemModeMessage(mode));
                *///?}

                CirculationConfiguratorSelection selection = CirculationConfiguratorSelection.fromStack(stack);
                String modeName = CI18n.format(selection.modeLangKey());
                String subModeName = CI18n.format(selection.subModeLangKey());

                //? if <1.20 {
                TextComponentString message = new TextComponentString(
                    TextFormatting.GOLD + modeName + TextFormatting.GRAY + "[" + TextFormatting.BLUE + subModeName + TextFormatting.GRAY + "]"
                );
                mc.player.sendStatusMessage(message, true);
                //?} else {
                
                /*Component message = Component.literal(
                    ChatFormatting.GOLD + modeName + ChatFormatting.GRAY + "[" + ChatFormatting.BLUE + subModeName + ChatFormatting.GRAY + "]"
                );
                mc.player.displayClientMessage(message, true);
                *///?}

                event.setCanceled(true);
            }
        }
    }
}
