package com.circulation.circulation_networks.registry;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import com.circulation.circulation_networks.items.ItemDimensionalChargingPlugin;
import com.circulation.circulation_networks.items.ItemHubChannelPlugin;
import com.circulation.circulation_networks.items.ItemInspectionTool;
import com.circulation.circulation_networks.items.ItemMaterial;
import com.circulation.circulation_networks.items.ItemWideAreaChargingPlugin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

public final class RegistryItems {

    private RegistryItems() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(RegistryItems::onRegisterItems);
    }

    private static void onRegisterItems(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ITEMS, helper -> {
            CFNItems.inspectionTool = register(helper, "inspection_tool", new ItemInspectionTool(new Item.Properties()));
            CFNItems.hubChannelPlugin = register(helper, "hub_channel_plugin", new ItemHubChannelPlugin(new Item.Properties()));
            CFNItems.wideAreaChargingPlugin = register(helper, "wide_area_charging_plugin", new ItemWideAreaChargingPlugin(new Item.Properties()));
            CFNItems.dimensionalChargingPlugin = register(helper, "dimensional_charging_plugin", new ItemDimensionalChargingPlugin(new Item.Properties()));
            CFNItems.circulationSourceCrystal = register(helper, "circulation_source_crystal", new ItemMaterial(new Item.Properties()));
            CFNItems.infernalMeltingCrystal = register(helper, "infernal_melting_crystal", new ItemMaterial(new Item.Properties()));
            CFNItems.endCoreCrystal = register(helper, "end_core_crystal", new ItemMaterial(new Item.Properties()));
        });
    }

    private static <T extends Item> T register(RegisterEvent.RegisterHelper<Item> helper, String name, T item) {
        helper.register(new ResourceLocation(CirculationFlowNetworks.MOD_ID, name), item);
        return item;
    }
}