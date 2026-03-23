package com.circulation.circulation_networks.gui;

import com.circulation.circulation_networks.api.EnergyAmount;
import com.circulation.circulation_networks.container.ComponentSlotLayout;
import com.circulation.circulation_networks.container.ContainerHub;
import com.circulation.circulation_networks.gui.component.BackgroundComponent;
import com.circulation.circulation_networks.gui.component.ButtonComponent;
import com.circulation.circulation_networks.gui.component.CloseButtonComponent;
import com.circulation.circulation_networks.gui.component.InventoryComponent;
import com.circulation.circulation_networks.gui.component.SlotComponent;
import com.circulation.circulation_networks.gui.component.base.Component;
import com.circulation.circulation_networks.gui.component.base.DraggableComponent;
import com.circulation.circulation_networks.gui.component.base.RenderPhase;
import com.circulation.circulation_networks.registry.RegistryEnergyHandler;
import com.circulation.circulation_networks.registry.RegistryItems;
import com.circulation.circulation_networks.tiles.nodes.TileEntityHub;
import com.circulation.circulation_networks.utils.FormatNumberUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@SideOnly(Side.CLIENT)
public class GuiHub extends CFNBaseGui<ContainerHub> {

    private int state;
    private String oldInput;
    private String oldOutput;
    private String oldFI;
    private String oldFO;

    private ButtonComponent node, playerPowerSupply, upgradePlugin, channel, permission, settings;
    private Component nodeUI, playerPowerSupplyUI, upgradePluginUI, channelUI, permissionUI, settingsUI;

    public GuiHub(EntityPlayer player, TileEntityHub te) {
        super(new ContainerHub(player, te));
        this.xSize = 178;
        this.ySize = 233;
    }

    @Override
    protected void buildComponents(Map<RenderPhase, List<Component>> components) {
        List<Component> bg = components.computeIfAbsent(RenderPhase.BACKGROUND, k -> new ObjectArrayList<>());
        bg.add(new BackgroundComponent("hub_base", this));
        bg.add(new InventoryComponent(7, 145, container.playerInvLayout, this));
        bg.add(new Component(-34, 0, 33, 80, this)
            .setSpriteLayers("hub_1_3_button")
            .addChild(
                node = new ButtonComponent(7, 7, 19, 19, this, "node", () -> {
                    nodeUI.setVisible(true);
                    node.setEnabled(false);
                }).addTooltip(TextFormatting.BOLD + "节点列表"),
                playerPowerSupply = new ButtonComponent(7, 29, 19, 19, this, "player_power_supply", () -> {
                    playerPowerSupplyUI.setVisible(true);
                    playerPowerSupply.setEnabled(false);
                }).addTooltip(TextFormatting.BOLD + "充能配置"),
                upgradePlugin = new ButtonComponent(7, 51, 19, 19, this, "upgrade_plugin", () -> {
                    upgradePluginUI.setVisible(true);
                    upgradePlugin.setEnabled(false);
                }).addTooltip(TextFormatting.BOLD + "插件列表")
            )
        );
        bg.add(new Component(179, 0, 33, 80, this)
            .setSpriteLayers("hub_1_3_button")
            .addChild(
                channel = new ButtonComponent(7, 7, 19, 19, this, "channel", () -> {
                    channelUI.setVisible(true);
                    channel.setEnabled(false);
                }) {
                    @Override
                    public boolean isEnabled() {
                        return super.isEnabled() && check();
                    }

                    private boolean check() {
                        var s = GuiHub.this.container.slots;
                        for (ComponentSlotLayout slot : s) {
                            var slotSlot = slot.getSlots().get(0);
                            if (slotSlot.getStack().getItem() == RegistryItems.hubChannelPlugin) {
                                return true;
                            }
                        }
                        return false;
                    }
                }.addTooltip(TextFormatting.BOLD + "频道列表"),
                permission = new ButtonComponent(7, 29, 19, 19, this, "permission", () -> {
                    permissionUI.setVisible(true);
                    permission.setEnabled(false);
                }).addTooltip(TextFormatting.BOLD + "权限设置"),
                settings = new ButtonComponent(7, 51, 19, 19, this, "settings", () -> {
                    settingsUI.setVisible(true);
                    settings.setEnabled(false);
                }) {
                    @Override
                    public boolean isEnabled() {
                        return super.isEnabled() && channel.isEnabled() && check();
                    }

                    private boolean check() {
                        return container.hasChannel();
                    }
                }.addTooltip(TextFormatting.BOLD + "频道设置")
            )
        );
        bg.add(new Component(-34, 197, 33, 36, this)
            .setSpriteLayers("hub_1_1_button")
            .addChild(new ButtonComponent(7, 7, 19, 19, this, "energy_unit", () -> {
                ++state;
                oldInput = null;
                oldOutput = null;
            }).addTooltip(TextFormatting.BOLD + "能源显示"))
        );
        int i = -1;
        bg.add(upgradePluginUI = new DraggableComponent(-34 - 58, 51, 58, 117, this)
            .setSpriteLayers("upgrade_plugin_ui")
            .setVisible(false)
            .addChild(
                new CloseButtonComponent(32, 7, 19, 19, this).setMutuallyComponent(upgradePlugin).addTooltip(TextFormatting.BOLD + "关闭UI"),
                new SlotComponent(11, 11 + ++i * 19, 16, 16, container.slots[i], "upgrade_plugin_slot", this),
                new SlotComponent(11, 11 + ++i * 19, 16, 16, container.slots[i], "upgrade_plugin_slot", this),
                new SlotComponent(11, 11 + ++i * 19, 16, 16, container.slots[i], "upgrade_plugin_slot", this),
                new SlotComponent(11, 11 + ++i * 19, 16, 16, container.slots[i], "upgrade_plugin_slot", this),
                new SlotComponent(11, 11 + ++i * 19, 16, 16, container.slots[i], "upgrade_plugin_slot", this)
            )
        );
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        fontRenderer.drawString(f(container.input, true), offsetX + 11, offsetY + 12, 0x79d7ff);
        fontRenderer.drawString(f(container.output, false), offsetX + 105, offsetY + 127, 0x79d7ff);
    }

    private String f(String string, boolean IO) {
        if (IO) {
            if (Objects.equals(string, oldInput)) {
                return oldFI;
            } else {
                oldInput = string;
            }
        } else {
            if (Objects.equals(string, oldOutput)) {
                return oldFO;
            } else {
                oldOutput = string;
            }
        }
        var e = EnergyAmount.obtain(string);
        var p = RegistryEnergyHandler.getPair(state);
        if (p.multiplying() != 0) {
            e.divide(p.multiplying());
        }
        final String o = (IO ? "I : " : "O : ") + FormatNumberUtils.formatNumber(e) + " " + p.unit() + "/t";
        e.recycle();
        return IO ? (oldFI = o) : (oldFO = o);
    }
}