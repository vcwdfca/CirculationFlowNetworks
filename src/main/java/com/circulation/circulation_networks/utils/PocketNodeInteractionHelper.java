package com.circulation.circulation_networks.utils;

public final class PocketNodeInteractionHelper {

    private PocketNodeInteractionHelper() {
    }

    public static boolean shouldPrioritizeConfiguratorDetach(boolean mainHand, boolean sneaking, boolean holdingConfigurator) {
        return mainHand && !sneaking && holdingConfigurator;
    }
}
