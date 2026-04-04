package com.circulation.circulation_networks;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.List;

public final class CFNConfig {

    public static String[] classNames = new String[0];
    public static String[] supplyClassNames = new String[0];
    public static final Node NODE = new Node();

    static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CLASS_NAMES;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SUPPLY_CLASS_NAMES;
    private static final ForgeConfigSpec.DoubleValue PORT_NODE_ENERGY_SCOPE;
    private static final ForgeConfigSpec.DoubleValue PORT_NODE_LINK_SCOPE;
    private static final ForgeConfigSpec.DoubleValue CHARGING_NODE_CHARGING_SCOPE;
    private static final ForgeConfigSpec.DoubleValue CHARGING_NODE_LINK_SCOPE;
    private static final ForgeConfigSpec.DoubleValue RELAY_NODE_LINK_SCOPE;
    private static final ForgeConfigSpec.DoubleValue HUB_ENERGY_SCOPE;
    private static final ForgeConfigSpec.DoubleValue HUB_CHARGING_SCOPE;
    private static final ForgeConfigSpec.DoubleValue HUB_LINK_SCOPE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        CLASS_NAMES = builder.comment(
                "Energy entity blacklist (fully qualified name or class name prefix)",
                "BlockEntities in the blacklist will not be recognized as energy containers",
                "Examples:",
                "  - 'com.example.CustomEnergyTile' Exact match",
                "  - 'com.example' Prefix match, includes all classes starting with this"
        ).defineListAllowEmpty(List.of("classNames"), List::of, obj -> obj instanceof String);

        SUPPLY_CLASS_NAMES = builder.comment(
                "Energy device blacklist for generic supply nodes (non-specialized).",
                "Matched devices can ONLY be connected by specialized nodes that override isBlacklisted.",
                "Examples:",
                "  - 'com.example.AdvancedEnergyTile' Exact match",
                "  - 'com.example.advanced' Prefix match"
        ).defineListAllowEmpty(List.of("supplyClassNames"), List::of, obj -> obj instanceof String);

        builder.push("Node");

        builder.push("PortNode");
        PORT_NODE_ENERGY_SCOPE = builder.comment("Energy range of Circulation Port Node").defineInRange("energyScope", 8.0, 0.1, 32.0);
        PORT_NODE_LINK_SCOPE = builder.comment("Link range of Circulation Port Node").defineInRange("linkScope", 12.0, 0.1, 32.0);
        builder.pop();

        builder.push("ChargingNode");
        CHARGING_NODE_CHARGING_SCOPE = builder.comment("Charging range of Circulation Charging Node").defineInRange("chargingScope", 5.0, 1.0, 32.0);
        CHARGING_NODE_LINK_SCOPE = builder.comment("Link range of Circulation Charging Node").defineInRange("linkScope", 8.0, 1.0, 32.0);
        builder.pop();

        builder.push("RelayNode");
        RELAY_NODE_LINK_SCOPE = builder.comment("Link range of Circulation Relay Node").defineInRange("linkScope", 20.0, 1.0, 64.0);
        builder.pop();

        builder.push("Hub");
        HUB_ENERGY_SCOPE = builder.comment("Energy range of Hub").defineInRange("energyScope", 10.0, 1.0, 32.0);
        HUB_CHARGING_SCOPE = builder.comment("Charging range of Hub").defineInRange("chargingScope", 8.0, 1.0, 32.0);
        HUB_LINK_SCOPE = builder.comment("Link range of Hub").defineInRange("linkScope", 16.0, 1.0, 32.0);
        builder.pop();

        builder.pop();

        SPEC = builder.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC);
    }

    public static void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) syncFromSpec();
    }

    public static void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SPEC) syncFromSpec();
    }

    private static void syncFromSpec() {
        classNames = CLASS_NAMES.get().stream().map(Object::toString).toArray(String[]::new);
        supplyClassNames = SUPPLY_CLASS_NAMES.get().stream().map(Object::toString).toArray(String[]::new);
        NODE.portNode.energyScope = PORT_NODE_ENERGY_SCOPE.get();
        NODE.portNode.linkScope = PORT_NODE_LINK_SCOPE.get();
        NODE.chargingNode.chargingScope = CHARGING_NODE_CHARGING_SCOPE.get();
        NODE.chargingNode.linkScope = CHARGING_NODE_LINK_SCOPE.get();
        NODE.relayNode.linkScope = RELAY_NODE_LINK_SCOPE.get();
        NODE.hub.energyScope = HUB_ENERGY_SCOPE.get();
        NODE.hub.chargingScope = HUB_CHARGING_SCOPE.get();
        NODE.hub.linkScope = HUB_LINK_SCOPE.get();
    }

    public static class Node {
        public final PortNodeConfig portNode = new PortNodeConfig();
        public final ChargingNodeConfig chargingNode = new ChargingNodeConfig();
        public final RelayNodeConfig relayNode = new RelayNodeConfig();
        public final HubConfig hub = new HubConfig();

        public static class PortNodeConfig {
            public double energyScope = 8;
            public double linkScope = 12;
        }

        public static class ChargingNodeConfig {
            public double chargingScope = 5;
            public double linkScope = 8;
        }

        public static class RelayNodeConfig {
            public double linkScope = 20;
        }

        public static class HubConfig {
            public double energyScope = 10;
            public double chargingScope = 8;
            public double linkScope = 16;
        }
    }
}
