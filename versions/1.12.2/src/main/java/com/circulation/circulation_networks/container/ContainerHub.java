package com.circulation.circulation_networks.container;

import com.circulation.circulation_networks.api.hub.ChargingPreference;
import com.circulation.circulation_networks.manager.EnergyMachineManager;
import com.circulation.circulation_networks.tiles.nodes.TileEntityHub;
import com.circulation.circulation_networks.utils.GuiSync;
import net.minecraft.entity.player.EntityPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static com.circulation.circulation_networks.network.nodes.HubNode.EMPTY;

public class ContainerHub extends CFNBaseContainer {

    public final ComponentSlotLayout playerInvLayout;
    public final ComponentSlotLayout[] slots;
    public final TileEntityHub te;
    @GuiSync(0)
    public String input;
    @GuiSync(1)
    public String output;
    public UUID channelId = EMPTY;
    @GuiSync(2)
    public String channelIdString = EMPTY.toString();
    @GuiSync(3)
    public String channelName = "";
    @GuiSync(4)
    public byte chargingModeByte = (byte) 0x111111;
    public ChargingPreference chargingMode = ChargingPreference.defaultAll();

    public ContainerHub(EntityPlayer player, TileEntityHub te) {
        super(player);
        this.te = te;

        playerInvLayout = ComponentSlotLayout.playerInventory(player.inventory).build(this);
        slots = new ComponentSlotLayout[5];
        for (int i = 0; i < 5; i++) {
            slots[i] = new ComponentSlotLayout().addSlot(te.getPlugins(), i, 0, 0).build(this);
        }
        if (te.getWorld().isRemote) {
            input = "0";
            output = "0";
        } else {
            var energy = EnergyMachineManager.INSTANCE.getInteraction().get(te.getNode().getGrid());
            input = energy.getInput().toString();
            output = energy.getOutput().toString();
        }
    }

    @Override
    public boolean canInteractWith(@NotNull EntityPlayer playerIn) {
        return playerIn.getDistanceSq(te.getPos().getX() + 0.5D, te.getPos().getY() + 0.5D, te.getPos().getZ() + 0.5D) <= 128;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (te.getWorld().isRemote) return;
        if (channelId != te.getNode().getChannelId()) {
            channelId = te.getNode().getChannelId();
            channelIdString = channelId.toString();
            channelName = te.getNode().getChannelName();
        }
        chargingMode = te.getNode().getChargingPreference(player.getUniqueID());
        chargingModeByte = chargingMode.toByte();
        if (te.getWorld().getTotalWorldTime() % 10 != 0) return;
        var energy = EnergyMachineManager.INSTANCE.getInteraction().get(te.getNode().getGrid());
        input = energy.getInput().toString();
        output = energy.getOutput().toString();
    }

    @Override
    public void onUpdate(String field, Object oldValue, Object newValue) {
        if ("channelIdString".equals(field)) {
            channelId = UUID.fromString(newValue.toString());
        }
        if ("chargingModeByte".equals(field)) {
            chargingMode.setPrefs((byte) newValue);
        }
    }

    public boolean hasChannel() {
        return channelId != null && !channelId.equals(EMPTY);
    }
}