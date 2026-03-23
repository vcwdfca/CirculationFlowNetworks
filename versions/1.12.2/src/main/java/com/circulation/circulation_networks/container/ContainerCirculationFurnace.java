package com.circulation.circulation_networks.container;

import com.circulation.circulation_networks.tiles.machines.TileEntityCirculationFurnace;
import com.circulation.circulation_networks.utils.GuiSync;
import net.minecraft.entity.player.EntityPlayer;
import org.jetbrains.annotations.NotNull;

public class ContainerCirculationFurnace extends CFNBaseContainer {

    public final ComponentSlotLayout inputLayout;
    public final ComponentSlotLayout outputLayout;
    public final ComponentSlotLayout playerInvLayout;
    private final TileEntityCirculationFurnace te;
    @GuiSync(0)
    public int cookTime;
    @GuiSync(1)
    public int totalCookTime;
    @GuiSync(2)
    public long currentFlow;
    @GuiSync(3)
    public long demandFlow;

    public ContainerCirculationFurnace(EntityPlayer player, TileEntityCirculationFurnace te) {
        super(player);
        this.te = te;

        inputLayout = new ComponentSlotLayout().addSlot(te.inv, 0, 1, 1).build(this);
        outputLayout = new ComponentSlotLayout().addOutput(te.inv, 1, 1, 1).build(this);
        playerInvLayout = ComponentSlotLayout.playerInventory(player.inventory).build(this);
    }

    @Override
    public boolean canInteractWith(@NotNull EntityPlayer playerIn) {
        return playerIn.getDistanceSq(te.getPos().getX() + 0.5D, te.getPos().getY() + 0.5D, te.getPos().getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (isServer()) {
            cookTime = te.getCookTime();
            totalCookTime = te.getTotalCookTime();
            currentFlow = te.getCurrentFlow();
            demandFlow = te.getDemandFlow();
        }
    }

}