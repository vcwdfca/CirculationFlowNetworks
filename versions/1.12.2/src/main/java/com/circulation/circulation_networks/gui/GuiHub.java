package com.circulation.circulation_networks.gui;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import com.circulation.circulation_networks.api.EnergyAmount;
import com.circulation.circulation_networks.api.hub.ChargingDefinition;
import com.circulation.circulation_networks.container.ComponentSlotLayout;
import com.circulation.circulation_networks.container.ContainerHub;
import com.circulation.circulation_networks.gui.component.BackgroundComponent;
import com.circulation.circulation_networks.gui.component.ButtonComponent;
import com.circulation.circulation_networks.gui.component.CloseButtonComponent;
import com.circulation.circulation_networks.gui.component.InventoryComponent;
import com.circulation.circulation_networks.gui.component.SlotComponent;
import com.circulation.circulation_networks.gui.component.TextComponent;
import com.circulation.circulation_networks.gui.component.TriStateButtonComponent;
import com.circulation.circulation_networks.gui.component.base.Component;
import com.circulation.circulation_networks.gui.component.base.DraggableComponent;
import com.circulation.circulation_networks.gui.component.base.RenderPhase;
import com.circulation.circulation_networks.packets.UpdatePlayerChargingMode;
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
import java.util.function.BooleanSupplier;

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

    private static Runnable setC(ChargingDefinition c) {
        var u = new UpdatePlayerChargingMode(c);
        return () -> CirculationFlowNetworks.sendToServer(u);
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
            .addChild(
                new ButtonComponent(7, 7, 19, 19, this, "energy_unit", () -> {
                    ++state;
                    oldInput = null;
                    oldOutput = null;
                }).addTooltip(TextFormatting.BOLD + "能源显示")
            )
        );
        bg.add(new TextComponent(11, 12, this, () -> f(container.input, true), 0x79d7ff));
        bg.add(new TextComponent(105, 127, this, () -> f(container.output, false), 0x79d7ff));
        List<Component> n = components.computeIfAbsent(RenderPhase.NORMAL, k -> new ObjectArrayList<>());
        int i = -1;
        n.add(upgradePluginUI = new DraggableComponent(-34, 51, 58, 117, this)
            .setSpriteLayers("upgrade_plugin_ui")
            .setVisible(false)
            .addChild(
                getNewClose(upgradePlugin, 32, 7),
                new SlotComponent(11, 11 + ++i * 19, 16, 16, container.slots[i], "upgrade_plugin_slot", this),
                new SlotComponent(11, 11 + ++i * 19, 16, 16, container.slots[i], "upgrade_plugin_slot", this),
                new SlotComponent(11, 11 + ++i * 19, 16, 16, container.slots[i], "upgrade_plugin_slot", this),
                new SlotComponent(11, 11 + ++i * 19, 16, 16, container.slots[i], "upgrade_plugin_slot", this),
                new SlotComponent(11, 11 + ++i * 19, 16, 16, container.slots[i], "upgrade_plugin_slot", this)
            )
        );
        n.add(playerPowerSupplyUI = new DraggableComponent(-34, 29, 142, 116, this)
            .setSpriteLayers("supply_ui")
            .setVisible(false)
            .addChild(
                getNewClose(playerPowerSupply, 115, 9),
                new ButtonComponent(94, 38, 18, 18, this, "supply_all", () -> CirculationFlowNetworks.sendToServer(new UpdatePlayerChargingMode((byte) 1)))
                    .addTooltip(TextFormatting.BOLD + "全部启用"),
                new ButtonComponent(94, 10, 18, 18, this, "supply_reset", () -> CirculationFlowNetworks.sendToServer(new UpdatePlayerChargingMode((byte) 2)))
                    .addTooltip(TextFormatting.BOLD + "全部关闭"),
                new TriStateButtonComponent(13, 12, 8, 41, this, "supply_armored_fence", setC(ChargingDefinition.ARMOR))
                    .setActiveSupplier(getC(ChargingDefinition.ARMOR))
                    .addTooltip(TextFormatting.BOLD + "盔甲栏"),
                new TriStateButtonComponent(57, 45, 8, 8, this, "supply_left", setC(ChargingDefinition.MAIN_HAND))
                    .setActiveSupplier(() -> container.chargingMode.getPreference(ChargingDefinition.MAIN_HAND) || container.chargingMode.getPreference(ChargingDefinition.HOTBAR))
                    .addTooltip(TextFormatting.BOLD + "主手"),
                new TriStateButtonComponent(68, 45, 8, 8, this, "supply_right", setC(ChargingDefinition.OFF_HAND))
                    .setActiveSupplier(getC(ChargingDefinition.OFF_HAND))
                    .addTooltip(TextFormatting.BOLD + "副手"),
                new TriStateButtonComponent(13, 58, 96, 30, this, "supply_inventory", setC(ChargingDefinition.INVENTORY))
                    .setActiveSupplier(getC(ChargingDefinition.INVENTORY))
                    .addTooltip(TextFormatting.BOLD + "背包"),
                new TriStateButtonComponent(13, 93, 96, 8, this, "supply_quick_access_bar", setC(ChargingDefinition.HOTBAR))
                    .setActiveSupplier(getC(ChargingDefinition.HOTBAR))
                    .addTooltip(TextFormatting.BOLD + "快捷栏"),
                new TriStateButtonComponent(57, 12, 8, 8, this, "supply_accessory", setC(ChargingDefinition.ACCESSORY))
                    .setActiveSupplier(getC(ChargingDefinition.ACCESSORY))
                    .addTooltip(TextFormatting.BOLD + "饰品栏")
            )
        );
    }

    private CloseButtonComponent getNewClose(Component c, int x, int y) {
        return new CloseButtonComponent(x, y, 19, 19, this).setMutuallyComponent(c).addTooltip(TextFormatting.BOLD + "关闭UI");
    }

    private BooleanSupplier getC(ChargingDefinition c) {
        return () -> container.chargingMode.getPreference(c);
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
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