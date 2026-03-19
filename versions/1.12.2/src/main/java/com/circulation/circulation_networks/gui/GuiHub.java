package com.circulation.circulation_networks.gui;

import com.circulation.circulation_networks.container.ContainerHub;
import com.circulation.circulation_networks.gui.component.BackgroundComponent;
import com.circulation.circulation_networks.gui.component.base.Component;
import com.circulation.circulation_networks.gui.component.base.RenderPhase;
import com.circulation.circulation_networks.tiles.nodes.TileEntityHub;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class GuiHub extends CFNBaseGui {

    private final ContainerHub container;

    public GuiHub(EntityPlayer player, TileEntityHub te) {
        super(new ContainerHub(player, te));
        this.container = (ContainerHub) inventorySlots;
        this.xSize = 200;
        this.ySize = 180;
    }

    @Override
    protected void buildComponents(Map<RenderPhase, List<Component>> components) {
        List<Component> bg = components.computeIfAbsent(RenderPhase.BACKGROUND, k -> new ObjectArrayList<>());
        bg.add(new BackgroundComponent(315, 233, "test", this));
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        fontRenderer.drawString(container.input, 45, 14, 0x79d7ff);
        fontRenderer.drawString(container.output, 139, 128, 0x79d7ff);
    }
}
