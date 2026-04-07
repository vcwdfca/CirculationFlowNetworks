package com.circulation.circulation_networks.pocket;

import com.circulation.circulation_networks.api.ClientTickMachine;
import com.circulation.circulation_networks.registry.PocketNodeItems;
import net.minecraft.client.Minecraft;
//~ mc_imports
import net.minecraft.item.ItemStack;

public final class PocketNodeClientHost implements ClientTickMachine {

    private final PocketNodeRecord record;
    private final ItemStack renderStack;
    private byte gui3dState = -1;

    public PocketNodeClientHost(PocketNodeRecord record) {
        this.record = record;
        this.renderStack = PocketNodeItems.createStack(record.nodeType());
    }

    public PocketNodeRecord getRecord() {
        return record;
    }

    public ItemStack getRenderStack() {
        return renderStack;
    }

    public boolean isGui3d() {
        if (gui3dState >= 0) {
            return gui3dState == 1;
        }
        //? if <1.20 {
        Minecraft mc = Minecraft.getMinecraft();
        //?} else {
        /*Minecraft mc = Minecraft.getInstance();
         *///?}
        //? if <1.20 {
        boolean gui3d = mc.getRenderItem().getItemModelWithOverrides(renderStack, mc.world, null).isGui3d();
        //?} else {
        /*boolean gui3d = mc.getItemRenderer().getModel(renderStack, mc.level, null, 0).isGui3d();
         *///?}
        gui3dState = (byte) (gui3d ? 1 : 0);
        return gui3d;
    }

    @Override
    public void clientUpdate() {
    }
}
