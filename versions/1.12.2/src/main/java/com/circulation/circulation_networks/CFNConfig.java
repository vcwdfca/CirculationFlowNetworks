package com.circulation.circulation_networks;

import net.minecraftforge.common.config.Config;

@Config(modid = CirculationFlowNetworks.MOD_ID)
public final class CFNConfig {

    @Config.Name("Node")
    public static final Node NODE = new Node();

    @Config.Name("EnergyBlacklist")
    @Config.Comment({
        "能源实体黑名单（完全限定名或类名前缀）",
        "黑名单中的TileEntity不会被识别为能源容器",
        "示例：",
        "  - 'com.example.CustomEnergyTile' 精确匹配",
        "  - 'com.example' 前缀匹配，包含所有以此开头的类",
        "",
        "Energy entity blacklist (fully qualified name or class name prefix)",
        "TileEntitys in the blacklist will not be recognized as energy containers",
        "Examples:",
        "  - 'com.example.CustomEnergyTile' Exact match",
        "  - 'com.example' Prefix match, includes all classes starting with this"
    })
    public static String[] classNames = new String[]{"sonar.fluxnetworks.common.tileentity.TileFluxCore"};

    @Config.Name("EnergySupplyBlacklist")
    @Config.Comment({
        "通用能源供应节点的能源设备黑名单（完全限定名或类名前缀）",
        "被匹配的能源设备只能由专用节点（覆写了 isBlacklisted 方法的节点）建立连接，",
        "普通供应节点对这些设备无效。",
        "",
        "Energy device blacklist for generic supply nodes (non-specialized).",
        "Matched devices can ONLY be connected by specialized nodes that override isBlacklisted.",
        "Examples:",
        "  - 'com.example.AdvancedEnergyTile' Exact match",
        "  - 'com.example.advanced' Prefix match"
    })
    public static String[] supplyClassNames = new String[]{};

    public static class Node {

        @Config.Name("PortNode")
        public final PortNodeConfig portNode = new PortNodeConfig();

        @Config.Name("ChargingNode")
        public final ChargingNodeConfig chargingNode = new ChargingNodeConfig();

        @Config.Name("RelayNode")
        public final RelayNodeConfig relayNode = new RelayNodeConfig();

        @Config.Name("Hub")
        public final HubConfig hub = new HubConfig();

        public static class PortNodeConfig {
            @Config.Comment({"环流端口节点的能量范围", "Energy range of Circulation Port Node"})
            @Config.Name("energyScope")
            @Config.RangeDouble(min = 0.1, max = 32)
            public double energyScope = 8;

            @Config.Comment({"环流端口节点的链接范围", "Link range of Circulation Port Node"})
            @Config.Name("linkScope")
            @Config.RangeDouble(min = 0.1, max = 32)
            public double linkScope = 12;
        }

        public static class ChargingNodeConfig {
            @Config.Comment({"环流充能节点的充能范围", "Charging range of Circulation Charging Node"})
            @Config.Name("chargingScope")
            @Config.RangeDouble(min = 1, max = 32)
            public double chargingScope = 5;

            @Config.Comment({"环流充能节点的链接范围", "Link range of Circulation Charging Node"})
            @Config.Name("linkScope")
            @Config.RangeDouble(min = 1, max = 32)
            public double linkScope = 8;
        }

        public static class RelayNodeConfig {
            @Config.Comment({"环流中继节点的链接范围", "Link range of Circulation Relay Node"})
            @Config.Name("linkScope")
            @Config.RangeDouble(min = 1, max = 64)
            public double linkScope = 20;
        }

        public static class HubConfig {
            @Config.Comment({"中枢的能量范围", "Energy range of Hub"})
            @Config.Name("energyScope")
            @Config.RangeDouble(min = 1, max = 32)
            public double energyScope = 10;

            @Config.Comment({"中枢的充能范围", "Charging range of Hub"})
            @Config.Name("chargingScope")
            @Config.RangeDouble(min = 1, max = 32)
            public double chargingScope = 8;

            @Config.Comment({"中枢的链接范围", "Link range of Hub"})
            @Config.Name("linkScope")
            @Config.RangeDouble(min = 1, max = 32)
            public double linkScope = 16;
        }
    }

}
