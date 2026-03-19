package com.circulation.circulation_networks.api;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Mutable pooled energy value that stays in {@code long} mode for the common
 * case and automatically upgrades to {@link BigInteger} when an operation would
 * overflow. If a big value later falls back into the {@code long} range it is
 * downgraded automatically.
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class EnergyAmount extends Number implements Comparable<EnergyAmount> {

    private static final int STATE_UNINITIALIZED = 0;
    private static final int STATE_LONG = 1;
    private static final int STATE_BIG = 2;
    private static final int MAX_POOL_SIZE = 4096;
    private static final BigInteger BIG_ZERO = BigInteger.ZERO;
    private static final BigInteger BIG_ONE = BigInteger.ONE;
    private static final BigInteger BIG_INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE);
    private static final BigInteger BIG_LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);
    private static final BigInteger BIG_LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);
    private static final Deque<EnergyAmount> POOL = new ConcurrentLinkedDeque<>();

    private long longValue;
    private BigInteger bigValue;
    private byte state = STATE_UNINITIALIZED;

    protected EnergyAmount() {
    }

    @Override
    public int intValue() {
        if (state == STATE_LONG) {
            return (int) longValue;
        }
        if (state == STATE_UNINITIALIZED) {
            return 0;
        }
        if (compareTo(EnergyAmounts.INT_MIN) < 0) {
            return Integer.MIN_VALUE;
        }
        if (compareTo(EnergyAmounts.INT_MAX) > 0) {
            return Integer.MAX_VALUE;
        }
        return bigValue.intValue();
    }

    @Override
    public long longValue() {
        if (state == STATE_LONG) {
            return longValue;
        }
        if (state == STATE_UNINITIALIZED) {
            return 0L;
        }
        if (compareTo(EnergyAmounts.LONG_MIN) < 0) {
            return Long.MIN_VALUE;
        }
        if (compareTo(EnergyAmounts.LONG_MAX) > 0) {
            return Long.MAX_VALUE;
        }
        return bigValue.longValue();
    }

    @Override
    public float floatValue() {
        if (state == STATE_LONG) {
            return longValue;
        }
        if (state == STATE_UNINITIALIZED) {
            return 0;
        }
        return bigValue.floatValue();
    }

    @Override
    public double doubleValue() {
        if (state == STATE_LONG) {
            return longValue;
        }
        if (state == STATE_UNINITIALIZED) {
            return 0.0D;
        }
        return bigValue.doubleValue();
    }

    public EnergyAmount(long value) {
        assignLongDirect(value);
    }

    public EnergyAmount(BigInteger value) {
        init(value);
    }

    public EnergyAmount(String value) {
        init(value);
    }

    private static EnergyAmount obtainMutable() {
        EnergyAmount amount = POOL.pollFirst();
        return amount == null ? new EnergyAmount() : amount;
    }

    public static EnergyAmount obtain(long value) {
        return obtainMutable().init(value);
    }

    public static EnergyAmount obtain(BigInteger value) {
        return obtainMutable().init(value);
    }

    public static EnergyAmount obtain(String value) {
        if (value == null) {
            throw new IllegalArgumentException("EnergyAmount string cannot be null");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("EnergyAmount string cannot be empty");
        }
        try {
            return obtain(Long.parseLong(trimmed));
        } catch (NumberFormatException ignored) {
            return obtain(new BigInteger(trimmed));
        }
    }

    public static EnergyAmount obtain(EnergyAmount other) {
        return obtainMutable().copyFrom(other);
    }

    public EnergyAmount init(long value) {
        longValue = value;
        bigValue = null;
        state = STATE_LONG;
        return this;
    }

    public EnergyAmount init(String value) {
        if (value == null) {
            throw new IllegalArgumentException("EnergyAmount string cannot be null");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("EnergyAmount string cannot be empty");
        }
        try {
            return init(Long.parseLong(trimmed));
        } catch (NumberFormatException ignored) {
            return init(new BigInteger(trimmed));
        }
    }

    public EnergyAmount init(BigInteger value) {
        if (value == null) {
            throw new IllegalArgumentException("EnergyAmount BigInteger cannot be null");
        }
        if (fitsInLong(value)) {
            return init(value.longValue());
        }
        bigValue = value;
        longValue = 0L;
        state = STATE_BIG;
        return this;
    }

    public EnergyAmount copyFrom(EnergyAmount other) {
        if (other == null || !other.isInitialized()) {
            clear();
            return this;
        }
        if (other.isBig()) {
            bigValue = other.bigValue;
            longValue = 0L;
            state = STATE_BIG;
            return this;
        }
        return init(other.longValue);
    }

    public void clear() {
        longValue = 0L;
        bigValue = null;
        state = STATE_UNINITIALIZED;
    }

    public void recycle() {
        clear();
        if (POOL.size() < MAX_POOL_SIZE) {
            POOL.addFirst(this);
        }
    }

    public boolean isInitialized() {
        return state != STATE_UNINITIALIZED;
    }

    public boolean isBig() {
        return state == STATE_BIG;
    }

    public boolean isZero() {
        if (state == STATE_UNINITIALIZED || state == STATE_LONG) {
            return longValue == 0L;
        }
        return false;
    }

    public boolean isPositive() {
        if (state == STATE_LONG) {
            return longValue > 0L;
        }
        if (state == STATE_BIG) {
            return bigValue.signum() > 0;
        }
        return false;
    }

    public boolean isNegative() {
        if (state == STATE_LONG) {
            return longValue < 0L;
        }
        if (state == STATE_BIG) {
            return bigValue.signum() < 0;
        }
        return false;
    }

    public boolean fitsLong() {
        return state == STATE_LONG || (state == STATE_BIG && fitsInLong(bigValue));
    }

    public long asLongExact() {
        if (state == STATE_LONG) {
            return longValue;
        }
        if (state == STATE_BIG && fitsInLong(bigValue)) {
            return bigValue.longValueExact();
        }
        throw new ArithmeticException("EnergyAmount does not fit into long");
    }

    public long asLongClamped() {
        return longValue();
    }

    public BigInteger asBigInteger() {
        if (state == STATE_BIG) {
            return bigValue;
        }
        if (state == STATE_UNINITIALIZED) {
            return BigInteger.ZERO;
        }
        return toBigInteger(longValue);
    }

    public EnergyAmount setZero() {
        return init(0L);
    }

    public EnergyAmount add(long value) {
        if (state == STATE_UNINITIALIZED) {
            return init(value);
        }
        if (state == STATE_LONG) {
            if (value > 0 ? longValue <= Long.MAX_VALUE - value : longValue >= Long.MIN_VALUE - value) {
                longValue += value;
                return this;
            }
            return init(toBigInteger(longValue).add(toBigInteger(value)));
        }
        bigValue = bigValue.add(toBigInteger(value));
        normalize();
        return this;
    }

    public EnergyAmount add(EnergyAmount other) {
        if (other == null || !other.isInitialized()) {
            return this;
        }
        if (!isInitialized()) {
            return copyFrom(other);
        }
        if (state == STATE_LONG && other.state == STATE_LONG) {
            return add(other.longValue);
        }
        bigValue = asBigInteger().add(other.asBigInteger());
        state = STATE_BIG;
        longValue = 0L;
        normalize();
        return this;
    }

    public EnergyAmount subtract(long value) {
        if (state == STATE_UNINITIALIZED) {
            if (value != Long.MIN_VALUE) {
                return init(-value);
            }
            return init(toBigInteger(value).negate());
        }
        if (state == STATE_LONG) {
            if (value > 0 ? longValue >= Long.MIN_VALUE + value : longValue <= Long.MAX_VALUE + value) {
                longValue -= value;
                return this;
            }
            return init(toBigInteger(longValue).subtract(toBigInteger(value)));
        }
        bigValue = bigValue.subtract(toBigInteger(value));
        normalize();
        return this;
    }

    public EnergyAmount subtract(EnergyAmount other) {
        if (other == null || !other.isInitialized()) {
            return this;
        }
        if (!isInitialized()) {
            return init(other.asBigInteger().negate());
        }
        if (state == STATE_LONG && other.state == STATE_LONG) {
            return subtract(other.longValue);
        }
        bigValue = asBigInteger().subtract(other.asBigInteger());
        state = STATE_BIG;
        longValue = 0L;
        normalize();
        return this;
    }

    public EnergyAmount min(EnergyAmount other) {
        if (other != null && compareTo(other) > 0) {
            copyFrom(other);
        }
        return this;
    }

    public EnergyAmount max(EnergyAmount other) {
        if (other != null && compareTo(other) < 0) {
            copyFrom(other);
        }
        return this;
    }

    public int compareTo(long value) {
        if (state == STATE_UNINITIALIZED) {
            return Long.compare(0L, value);
        }
        if (state == STATE_LONG) {
            return Long.compare(longValue, value);
        }
        return bigValue.compareTo(toBigInteger(value));
    }

    @Override
    public int compareTo(@NotNull EnergyAmount other) {
        if (other == null || !other.isInitialized()) {
            return compareTo(0L);
        }
        if (state == STATE_UNINITIALIZED) {
            return -other.compareTo(0L);
        }
        if (state == STATE_LONG && other.state == STATE_LONG) {
            return Long.compare(longValue, other.longValue);
        }
        return asBigInteger().compareTo(other.asBigInteger());
    }

    @Override
    public String toString() {
        if (state == STATE_UNINITIALIZED) {
            return "0";
        }
        return state == STATE_LONG ? Long.toString(longValue) : bigValue.toString();
    }

    private void normalize() {
        if (state == STATE_BIG && fitsInLong(bigValue)) {
            assignLongDirect(bigValue.longValue());
        }
    }

    final EnergyAmount assignLongDirect(long value) {
        longValue = value;
        bigValue = null;
        state = STATE_LONG;
        return this;
    }

    private static boolean fitsInLong(BigInteger value) {
        return value.bitLength() <= 63;
    }

    private static BigInteger toBigInteger(long value) {
        if (value == 0L) return BIG_ZERO;
        if (value == 1L) return BIG_ONE;
        if (value == Integer.MAX_VALUE) return BIG_INT_MAX;
        if (value == Long.MAX_VALUE) return BIG_LONG_MAX;
        if (value == Long.MIN_VALUE) return BIG_LONG_MIN;
        return BigInteger.valueOf(value);
    }

}