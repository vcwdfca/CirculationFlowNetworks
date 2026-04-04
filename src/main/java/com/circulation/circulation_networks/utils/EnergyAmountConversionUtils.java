package com.circulation.circulation_networks.utils;

import com.circulation.circulation_networks.api.EnergyAmount;

import java.math.BigDecimal;
import java.math.BigInteger;

public final class EnergyAmountConversionUtils {

    private static final BigDecimal DOUBLE_MAX = BigDecimal.valueOf(Double.MAX_VALUE);
    private static final BigDecimal NEGATIVE_DOUBLE_MAX = DOUBLE_MAX.negate();
    private static final BigInteger POSITIVE_UNBOUNDED = DOUBLE_MAX.toBigInteger().shiftLeft(8);
    private static final BigInteger NEGATIVE_UNBOUNDED = POSITIVE_UNBOUNDED.negate();

    private EnergyAmountConversionUtils() {
    }

    public static EnergyAmount obtainFromDoubleFloor(double value) {
        return setFromDoubleFloor(EnergyAmount.obtain(0L), value);
    }

    public static EnergyAmount setFromDoubleFloor(EnergyAmount target, double value) {
        if (Double.isNaN(value)) {
            return target.setZero();
        }
        if (value == Double.POSITIVE_INFINITY) {
            return target.init(POSITIVE_UNBOUNDED);
        }
        if (value == Double.NEGATIVE_INFINITY) {
            return target.init(NEGATIVE_UNBOUNDED);
        }
        if (value >= Long.MIN_VALUE && value <= Long.MAX_VALUE) {
            return target.init((long) value);
        }
        return target.init(BigDecimal.valueOf(value).toBigInteger());
    }

    public static double toDoubleClamped(EnergyAmount amount) {
        if (amount == null || !amount.isInitialized() || amount.isZero()) {
            return 0.0D;
        }
        if (amount.fitsLong()) {
            return amount.asLongExact();
        }
        BigDecimal value = new BigDecimal(amount.asBigInteger());
        if (value.compareTo(DOUBLE_MAX) > 0) {
            return Double.MAX_VALUE;
        }
        if (value.compareTo(NEGATIVE_DOUBLE_MAX) < 0) {
            return -Double.MAX_VALUE;
        }
        return value.doubleValue();
    }
}
