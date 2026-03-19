package com.circulation.circulation_networks.api;

public final class EnergyAmounts {

    public static final EnergyAmount ZERO = new ConstantEnergyAmount(0L);
    public static final EnergyAmount ONE = new ConstantEnergyAmount(1L);
    public static final EnergyAmount INT_MIN = new ConstantEnergyAmount(Integer.MIN_VALUE);
    public static final EnergyAmount INT_MAX = new ConstantEnergyAmount(Integer.MAX_VALUE);
    public static final EnergyAmount LONG_MAX = new ConstantEnergyAmount(Long.MAX_VALUE);
    public static final EnergyAmount LONG_MIN = new ConstantEnergyAmount(Long.MIN_VALUE);

    private EnergyAmounts() {
    }
}