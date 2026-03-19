package com.circulation.circulation_networks.items;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import com.circulation.circulation_networks.api.node.INode;
import com.circulation.circulation_networks.items.InspectionConfigurationTarget.ValidationStatus;
import com.circulation.circulation_networks.items.InspectionToolModeModel.ConfigurationMode;
import com.circulation.circulation_networks.manager.EnergyTypeOverrideManager;
import com.circulation.circulation_networks.manager.NetworkManager;
import com.circulation.circulation_networks.packets.ConfigOverrideRendering;
import com.circulation.circulation_networks.packets.NodeNetworkRendering;
import com.circulation.circulation_networks.packets.SpoceRendering;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemInspectionTool extends BaseItem {

    public ItemInspectionTool() {
        super("inspection_tool");
    }

    private static void sendModeMessage(EntityPlayerMP player, InspectionToolSelection selection) {
        TextComponentTranslation modeComponent = new TextComponentTranslation(selection.modeLangKey());
        TextComponentTranslation submodeComponent = new TextComponentTranslation(selection.subModeLangKey());

        modeComponent.getStyle().setColor(TextFormatting.GOLD);
        submodeComponent.getStyle().setColor(TextFormatting.BLUE);

        TextComponentTranslation message = new TextComponentTranslation(
            selection.modeDisplayKey(),
            modeComponent,
            submodeComponent
        );
        player.sendStatusMessage(message, true);
    }

    private static InspectionToolSelection toggleFunction(ItemStack stack, EntityPlayerMP player) {
        var feedback = InspectionToolToggleFeedback.toggle(stack);
        switch (feedback.overlaySyncAction()) {
            case FULL_SYNC -> ConfigOverrideRendering.sendFullSync(player);
            case CLEAR -> ConfigOverrideRendering.sendClear(player);
            case NONE -> {
            }
        }
        return feedback.selection();
    }

    private static void sendFeedbackMessage(EntityPlayerMP player, InspectionFeedbackMessage message) {
        if (message == null) {
            return;
        }
        if (message.hasDetailKey()) {
            player.sendMessage(new TextComponentTranslation(message.messageKey(), new TextComponentTranslation(message.detailKey())));
        } else {
            player.sendMessage(new TextComponentTranslation(message.messageKey()));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip, @NotNull ITooltipFlag flagIn) {
        var model = InspectionToolSelection.fromStack(stack).tooltipModel();
        for (var line : model.lines()) {
            switch (line.kind()) {
                case TRANSLATABLE -> tooltip.add(I18n.format(line.key()));
                case TRANSLATABLE_WITH_TRANSLATED_ARG ->
                    tooltip.add(I18n.format(line.key(), I18n.format(line.argumentKey())));
                case DESCRIPTION -> tooltip.add("§7" + I18n.format(line.key()));
                case BLANK -> tooltip.add("");
            }
        }
    }

    @Override
    public @NotNull EnumActionResult onItemUse(@NotNull EntityPlayer player, @NotNull World worldIn, @NotNull BlockPos pos, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!(player instanceof EntityPlayerMP p)) return EnumActionResult.PASS;

        ItemStack stack = p.getHeldItemMainhand();
        InspectionToolSelection selection = InspectionToolSelection.fromStack(stack);
        return switch (selection.function()) {
            case INSPECTION -> executeInspection(p, worldIn, pos, selection.subMode());
            case CONFIGURATION -> executeConfiguration(p, worldIn, pos, selection.subMode());
        };
    }

    @Override
    public @NotNull ActionResult<ItemStack> onItemRightClick(@NotNull World worldIn, @NotNull EntityPlayer player, @NotNull EnumHand hand) {
        if (!worldIn.isRemote && player instanceof EntityPlayerMP p && p.isSneaking()) {
            RayTraceResult ray = p.rayTrace(p.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue(), 1.0F);
            if (ray == null || ray.typeOfHit == RayTraceResult.Type.MISS) {
                ItemStack stack = p.getHeldItem(hand);
                sendModeMessage(p, toggleFunction(stack, p));
                return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            }
        }
        return super.onItemRightClick(worldIn, player, hand);
    }

    private EnumActionResult executeInspection(EntityPlayerMP player, World world, BlockPos pos, int subMode) {
        INode node = NetworkManager.INSTANCE.getNodeFromPos(world, pos);
        if (node == null) return EnumActionResult.PASS;

        InspectionExecutionPlan plan = InspectionExecutionPlan.fromNode(node, subMode);
        var snapshot = plan.snapshot();
        if (plan.renderSpoce()) {
            CirculationFlowNetworks.NET_CHANNEL.sendTo(new SpoceRendering(snapshot.pos(), snapshot.linkScope(), snapshot.energyScope(), snapshot.chargingScope()), player);
        }
        if (plan.renderLink()) {
            CirculationFlowNetworks.NET_CHANNEL.sendTo(new NodeNetworkRendering(player, snapshot.grid()), player);
            NodeNetworkRendering.addPlayer(snapshot.grid(), player);
        }
        return EnumActionResult.SUCCESS;
    }

    private EnumActionResult executeConfiguration(EntityPlayerMP player, World world, BlockPos pos, int subMode) {
        var manager = EnergyTypeOverrideManager.get();
        if (manager == null) return EnumActionResult.FAIL;

        INode node = NetworkManager.INSTANCE.getNodeFromPos(world, pos);
        TileEntity blockEntity = world.getTileEntity(pos);
        ValidationStatus validation = InspectionConfigurationTarget.validate(node, blockEntity);
        if (validation == ValidationStatus.NO_TARGET) {
            return EnumActionResult.PASS;
        }
        if (validation != ValidationStatus.VALID) {
            sendFeedbackMessage(player, InspectionFeedbackMessage.fromValidation(validation));
            return EnumActionResult.FAIL;
        }

        ConfigurationMode mode = ConfigurationMode.fromID(subMode);
        int dim = world.provider.getDimension();
        InspectionConfigurationApplyResult result = InspectionConfigurationApplyResult.apply(manager, dim, pos, mode);
        InspectionFeedbackMessage message = InspectionFeedbackMessage.fromApplyResult(result);

        if (result.overlayAction() == InspectionConfigurationApplyResult.OverlayAction.REMOVE) {
            ConfigOverrideRendering.sendRemove(player, result.packedPos());
        } else {
            ConfigOverrideRendering.sendAdd(player, result.packedPos(), result.energyType());
        }
        sendFeedbackMessage(player, message);
        return EnumActionResult.SUCCESS;
    }
}