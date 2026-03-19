package com.circulation.circulation_networks.container;

import com.circulation.circulation_networks.manager.EnergyMachineManager;
import com.circulation.circulation_networks.tiles.nodes.TileEntityHub;
import com.circulation.circulation_networks.utils.FormatNumberUtils;
import com.circulation.circulation_networks.utils.GuiSync;
import net.minecraft.entity.player.EntityPlayer;
import org.jetbrains.annotations.NotNull;

public class ContainerHub extends CFNBaseContainer {

    private final TileEntityHub te;
    @GuiSync(0)
    public String input = "";
    @GuiSync(1)
    public String output = "";

    public ContainerHub(EntityPlayer player, TileEntityHub te) {
        super(player, te);
        this.te = te;
    }

    @Override
    public boolean canInteractWith(@NotNull EntityPlayer playerIn) {
        return playerIn.getDistanceSq(te.getPos().getX() + 0.5D, te.getPos().getY() + 0.5D, te.getPos().getZ() + 0.5D) <= 128;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (te.getWorld().isRemote) return;
        var energy = EnergyMachineManager.INSTANCE.getInteraction().get(te.getNode().getGrid());
        input = FormatNumberUtils.formatNumber(energy.getInput());
        output = FormatNumberUtils.formatNumber(energy.getOutput());
    }
}
