package com.circulation.circulation_networks.gui;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import com.circulation.circulation_networks.container.ContainerPhaseInterrupter;
import com.circulation.circulation_networks.packets.PhaseInterrupterSyncPacket;
import com.circulation.circulation_networks.tiles.TileEntityPhaseInterrupter;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiPhaseInterrupter extends GuiContainer {

    private static final int MAX_SCOPE = 8;

    private final TileEntityPhaseInterrupter tileEntity;
    private final ContainerPhaseInterrupter container;
    private GuiButton redstoneModeButton;
    public GuiTextField scopeField;

    public GuiPhaseInterrupter(EntityPlayer player, TileEntityPhaseInterrupter te) {
        super(new ContainerPhaseInterrupter(player, te));
        this.tileEntity = te;
        this.container = (ContainerPhaseInterrupter) this.inventorySlots;
        this.xSize = 256;
        this.ySize = 200;
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
        int centerX = this.guiLeft + (this.xSize / 2);
        int centerY = this.guiTop + (this.ySize / 2);

        this.scopeField = new GuiTextField(1, this.fontRenderer, centerX - 25, centerY - 60, 50, 20);
        this.scopeField.setMaxStringLength(1);
        this.scopeField.setText(String.valueOf(tileEntity.getScope()));
        this.scopeField.setValidator(input -> {
            if (input == null || input.isEmpty()) return true;
            try {
                int val = Integer.parseInt(input);
                return val >= 0 && val <= MAX_SCOPE;
            } catch (NumberFormatException e) {
                return false;
            }
        });

        GuiButton showRangeButton = new GuiButton(2, centerX - 75, centerY - 30, 70, 20, I18n.format("gui.phase_interrupter.show_range"));
        this.buttonList.add(showRangeButton);

        this.redstoneModeButton = new GuiButton(3, centerX + 5, centerY - 30, 70, 20, getRedstoneModeButtonText());
        this.buttonList.add(this.redstoneModeButton);

        GuiButton closeButton = new GuiButton(4, centerX - 40, centerY + 20, 80, 20, I18n.format("gui.phase_interrupter.close"));
        this.buttonList.add(closeButton);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.scopeField.textboxKeyTyped(typedChar, keyCode)) {
            applyScopeFromField();
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.scopeField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void applyScopeFromField() {
        String text = this.scopeField.getText();
        if (text.isEmpty()) return;
        try {
            int val = Integer.parseInt(text);
            if (val >= 0 && val <= MAX_SCOPE && val != container.scope) {
                container.scope = val;
                tileEntity.setScope(val);
                CirculationFlowNetworks.NET_CHANNEL.sendToServer(new PhaseInterrupterSyncPacket(tileEntity));
            }
        } catch (NumberFormatException ignored) {
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 2) {
            tileEntity.setShowingRange(!tileEntity.isShowingRange());
        } else if (button.id == 3) {
            tileEntity.toggleRedstoneMode();
            this.redstoneModeButton.displayString = getRedstoneModeButtonText();
            CirculationFlowNetworks.NET_CHANNEL.sendToServer(new PhaseInterrupterSyncPacket(tileEntity));
        } else if (button.id == 4) {
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        int centerX = this.xSize / 2;
        int centerY = this.ySize / 2;

        String title = TextFormatting.BOLD + I18n.format("gui.phase_interrupter.title") + TextFormatting.RESET;
        this.fontRenderer.drawString(title, centerX - this.fontRenderer.getStringWidth(title) / 2, centerY - 90, 0xFFFFFF);

        String scopeLabel = I18n.format("gui.phase_interrupter.scope") + ": ";
        this.fontRenderer.drawString(scopeLabel, centerX - 25 - this.fontRenderer.getStringWidth(scopeLabel) - 2 - this.guiLeft, centerY - 55 - this.guiTop, 0xAAAAAA);

        String showRangeStatus = I18n.format("gui.phase_interrupter.show_range_status") + ": " + (tileEntity.isShowingRange() ? TextFormatting.GREEN + I18n.format("gui.phase_interrupter.enabled") : TextFormatting.RED + I18n.format("gui.phase_interrupter.disabled")) + TextFormatting.RESET;
        this.fontRenderer.drawString(showRangeStatus, centerX - this.fontRenderer.getStringWidth(showRangeStatus) / 2, centerY + 10, 0xFFFFFF);

        String redstoneModeStatus = I18n.format("gui.phase_interrupter.redstone_mode") + ": " + getRedstoneModeText();
        this.fontRenderer.drawString(redstoneModeStatus, centerX - this.fontRenderer.getStringWidth(redstoneModeStatus) / 2, centerY + 30, 0xFFFFFF);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.scopeField.drawTextBox();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.scopeField.updateCursorCounter();
    }

    private String getRedstoneModeButtonText() {
        return tileEntity.getRedstoneMode() ? I18n.format("gui.phase_interrupter.inverse_mode") : I18n.format("gui.phase_interrupter.normal_mode");
    }

    private String getRedstoneModeText() {
        if (tileEntity.getRedstoneMode()) {
            return TextFormatting.YELLOW + I18n.format("gui.phase_interrupter.normal_mode") + TextFormatting.RESET + " " + I18n.format("gui.phase_interrupter.normal_desc");
        } else {
            return TextFormatting.LIGHT_PURPLE + I18n.format("gui.phase_interrupter.inverse_mode") + TextFormatting.RESET + " " + I18n.format("gui.phase_interrupter.inverse_desc");
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return super.doesGuiPauseGame();
    }
}
