package com.circulation.circulation_networks.gui;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import com.circulation.circulation_networks.container.ContainerCirculationShielder;
import com.circulation.circulation_networks.packets.CirculationShielderSyncPacket;
import com.circulation.circulation_networks.tiles.CirculationShielderBlockEntity;
import com.circulation.circulation_networks.utils.CI18n;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class GuiCirculationShielder extends AbstractContainerScreen<ContainerCirculationShielder> {

    private static final int MAX_SCOPE = 8;

    private final CirculationShielderBlockEntity tileEntity;
    private final ContainerCirculationShielder container;
    public EditBox scopeField;

    public GuiCirculationShielder(ContainerCirculationShielder container, Inventory playerInventory, Component title) {
        super(container, playerInventory, title);
        this.tileEntity = container.te;
        this.container = container;
        this.imageWidth = 256;
        this.imageHeight = 200;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.leftPos + (this.imageWidth / 2);
        int centerY = this.topPos + (this.imageHeight / 2);

        this.scopeField = new EditBox(this.font, centerX - 25, centerY - 60, 50, 20, Component.empty());
        this.scopeField.setMaxLength(1);
        this.scopeField.setValue(String.valueOf(tileEntity.getScope()));
        this.scopeField.setFilter(input -> {
            if (input == null || input.isEmpty()) return true;
            try {
                int val = Integer.parseInt(input);
                return val >= 0 && val <= MAX_SCOPE;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        this.scopeField.setResponder(text -> applyScopeFromField());
        this.addRenderableWidget(this.scopeField);

        Button showRangeButton = Button.builder(
            Component.literal(CI18n.format("gui.circulation_shielder.show_range")),
            btn -> tileEntity.setShowingRange(!tileEntity.isShowingRange())
        ).bounds(centerX - 75, centerY - 30, 70, 20).build();
        this.addRenderableWidget(showRangeButton);

        Button redstoneModeButton = Button.builder(
            Component.literal(getRedstoneModeButtonText()),
            btn -> {
                tileEntity.toggleRedstoneMode();
                btn.setMessage(Component.literal(getRedstoneModeButtonText()));
                CirculationFlowNetworks.sendToServer(new CirculationShielderSyncPacket(tileEntity));
            }
        ).bounds(centerX + 5, centerY - 30, 70, 20).build();
        this.addRenderableWidget(redstoneModeButton);

        Button closeButton = Button.builder(
            Component.literal(CI18n.format("gui.circulation_shielder.close")),
            btn -> this.onClose()
        ).bounds(centerX - 40, centerY + 20, 80, 20).build();
        this.addRenderableWidget(closeButton);
    }

    private void applyScopeFromField() {
        String text = this.scopeField.getValue();
        if (text.isEmpty()) return;
        try {
            int val = Integer.parseInt(text);
            if (val >= 0 && val <= MAX_SCOPE && val != container.scope) {
                container.scope = val;
                tileEntity.setScope(val);
                CirculationFlowNetworks.sendToServer(new CirculationShielderSyncPacket(tileEntity));
            }
        } catch (NumberFormatException ignored) {
        }
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int centerX = this.imageWidth / 2;
        int centerY = this.imageHeight / 2;

        String title = ChatFormatting.BOLD + CI18n.format("gui.circulation_shielder.title") + ChatFormatting.RESET;
        guiGraphics.drawString(this.font, title, centerX - this.font.width(title) / 2, centerY - 90, 0xFFFFFF);

        String scopeLabel = CI18n.format("gui.circulation_shielder.scope") + ": ";
        guiGraphics.drawString(this.font, scopeLabel, centerX - 25 - this.font.width(scopeLabel) - 2 - this.leftPos, centerY - 55 - this.topPos, 0xAAAAAA);

        String showRangeStatus = CI18n.format("gui.circulation_shielder.show_range_status") + ": " + (tileEntity.isShowingRange() ? ChatFormatting.GREEN + CI18n.format("gui.circulation_shielder.enabled") : ChatFormatting.RED + CI18n.format("gui.circulation_shielder.disabled")) + ChatFormatting.RESET;
        guiGraphics.drawString(this.font, showRangeStatus, centerX - this.font.width(showRangeStatus) / 2, centerY + 10, 0xFFFFFF);

        String redstoneModeStatus = CI18n.format("gui.circulation_shielder.redstone_mode") + ": " + getRedstoneModeText();
        guiGraphics.drawString(this.font, redstoneModeStatus, centerX - this.font.width(redstoneModeStatus) / 2, centerY + 30, 0xFFFFFF);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
    }

    private String getRedstoneModeButtonText() {
        return tileEntity.getRedstoneMode() ? CI18n.format("gui.circulation_shielder.inverse_mode") : CI18n.format("gui.circulation_shielder.normal_mode");
    }

    private String getRedstoneModeText() {
        if (tileEntity.getRedstoneMode()) {
            return ChatFormatting.YELLOW + CI18n.format("gui.circulation_shielder.normal_mode") + ChatFormatting.RESET + " " + CI18n.format("gui.circulation_shielder.normal_desc");
        } else {
            return ChatFormatting.LIGHT_PURPLE + CI18n.format("gui.circulation_shielder.inverse_mode") + ChatFormatting.RESET + " " + CI18n.format("gui.circulation_shielder.inverse_desc");
        }
    }

    @Override
    public boolean isPauseScreen() {
        return super.isPauseScreen();
    }
}
