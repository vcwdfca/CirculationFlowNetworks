package com.circulation.circulation_networks.handlers;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import com.circulation.circulation_networks.items.InspectionToolModeModel;
import com.circulation.circulation_networks.items.InspectionToolSelection;
import com.circulation.circulation_networks.items.InspectionToolState;
import com.circulation.circulation_networks.packets.UpdateItemModeMessage;
import com.circulation.circulation_networks.registry.CFNItems;
import com.circulation.circulation_networks.utils.CI18n;
import net.minecraft.client.Minecraft;
//~ mc_imports
import net.minecraft.item.ItemStack;
//? if <1.20 {
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
//?} else {
/*import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
*///?}
//? if <1.20 {
//?} else if <1.21 {
/*import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
*///?} else {
/*import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
*///?}

//? if <1.20 {
@SideOnly(Side.CLIENT)
//?} else {
/*@OnlyIn(Dist.CLIENT)
 *///?}
public class ItemToolHandler {
    public static final ItemToolHandler INSTANCE = new ItemToolHandler();

    //? if <1.20 {
    private final Minecraft mc = Minecraft.getMinecraft();
    //?} else {
    /*private final Minecraft mc = Minecraft.getInstance();
     *///?}

    @SubscribeEvent
        //? if <1.20 {
    public void onMouseEvent(MouseEvent event) {
        if (mc.player != null && mc.player.isSneaking()) {
            ItemStack stack = mc.player.getHeldItemMainhand();
            int delta = InspectionToolModeModel.normalizeScrollDelta(Mouse.getEventDWheel());
            //?} else if <1.21 {
    /*public void onMouseEvent(InputEvent.MouseScrollingEvent event) {
        if (mc.player != null && mc.player.isShiftKeyDown()) {
            ItemStack stack = mc.player.getMainHandItem();
            int delta = InspectionToolModeModel.normalizeScrollDelta((int) event.getScrollDelta());
    *///?} else {
    /*public void onMouseEvent(InputEvent.MouseScrollingEvent event) {
        if (mc.player != null && mc.player.isShiftKeyDown()) {
            ItemStack stack = mc.player.getMainHandItem();
            int delta = InspectionToolModeModel.normalizeScrollDelta((int) event.getScrollDeltaY());
    *///?}
            if (delta != 0 && stack.getItem() == CFNItems.inspectionTool) {
                int mode = InspectionToolState.getSubMode(stack) + delta;
                InspectionToolState.setSubMode(stack, mode);

                CirculationFlowNetworks.sendToServer(new UpdateItemModeMessage(mode));

                InspectionToolSelection selection = InspectionToolSelection.fromStack(stack);
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
