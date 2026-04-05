package com.circulation.circulation_networks.api;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Deque;
import java.util.ArrayDeque;

/**
 * Mutable pooled energy value that stays in {@code long} mode for the common
 * case and automatically upgrades to {@link BigInteger} when an operation would
 * overflow. If a big value later falls back into the {@code long} range it is
 * downgraded automatically.
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class EnergyAmount extends Number implements Comparable<EnergyAmount> {

    protected static final int STATE_UNINITIALIZED = 0;
    protected static final int STATE_LONG = 1;
    protected static final int STATE_BIG = 2;
    protected static final int MAX_POOL_SIZE = 4096;
    protected static final BigInteger BIG_INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE);
    protected static final BigInteger BIG_LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);
    protected static final BigInteger BIG_LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);
    protected static final Deque<EnergyAmount> POOL = new ArrayDeque<>();
    protected byte state = STATE_UNINITIALIZED;
    private long longValue;
    private BigInteger bigValue;

    protected EnergyAmount() {
    }

    protected EnergyAmount(long value) {
        init(value);
    }

    protected EnergyAmount(BigInteger value) {
        init(value);
    }

    public static EnergyAmount obtain(long value) {
        EnergyAmount amount = POOL.pollFirst();
        return amount == null ? new EnergyAmount(value) : amount.init(value);
    }

    public static EnergyAmount obtain(BigInteger value) {
        EnergyAmount amount = POOL.pollFirst();
        return amount == null ? new EnergyAmount(value) : amount.init(value);
    }

    public static EnergyAmount obtain(String value) {
        String trimmed = normalizeNumericString(value);
        return isLongValueString(trimmed) ? obtain(parseLongString(trimmed)) : obtain(new BigInteger(trimmed));
    }

    public static EnergyAmount obtain(EnergyAmount other) {
        EnergyAmount amount = POOL.pollFirst();
        return amount == null ? new EnergyAmount().copyFrom(other) : amount.copyFrom(other);
    }

    private static BigInteger truncateToInteger(BigDecimal value) {
        return value.toBigInteger();
    }

    private static void validateFinite(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("EnergyAmount operand must be finite");
        }
    }

    private static String normalizeNumericString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("EnergyAmount string cannot be null");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("EnergyAmount string cannot be empty");
        }
        return trimmed;
    }

    private static boolean isLongValueString(String value) {
        int start = value.charAt(0) == '-' || value.charAt(0) == '+' ? 1 : 0;
        if (start == value.length()) {
            throw new NumberFormatException("For input string: \"" + value + "\"");
        }
        boolean negative = start > 0 && value.charAt(0) == '-';
        int firstNonZero = start;
        while (firstNonZero < value.length()) {
            char digit = value.charAt(firstNonZero);
            if (digit == '0') {
                firstNonZero++;
                continue;
            }
            if (digit < '0' || digit > '9') {
                throw new NumberFormatException("For input string: \"" + value + "\"");
            }
            break;
        }
        if (firstNonZero == value.length()) {
            return true;
        }
        int digitCount = value.length() - firstNonZero;
        for (int i = firstNonZero + 1; i < value.length(); i++) {
            char digit = value.charAt(i);
            if (digit < '0' || digit > '9') {
                throw new NumberFormatException("For input string: \"" + value + "\"");
            }
        }
        if (digitCount < 19) {
            return true;
        }
        if (digitCount > 19) {
            return false;
        }
        return compareWithLongLimit(value, firstNonZero, negative) <= 0;
    }

    private static long parseLongString(String value) {
        boolean negative = value.charAt(0) == '-';
        int start = negative || value.charAt(0) == '+' ? 1 : 0;
        long result = 0L;
        for (int i = start; i < value.length(); i++) {
            result = result * 10L - (value.charAt(i) - '0');
        }
        return negative ? result : -result;
    }

    private static int compareWithLongLimit(String value, int digitStart, boolean negative) {
        String limit = negative ? "9223372036854775808" : "9223372036854775807";
        for (int i = 0; i < 19; i++) {
            int diff = value.charAt(digitStart + i) - limit.charAt(i);
            if (diff != 0) {
                return diff;
            }
        }
        return 0;
    }

    private static long asWholeLong(double value) {
        if (value < Long.MIN_VALUE || value > Long.MAX_VALUE) {
            return Long.MIN_VALUE;
        }
        return (long) value;
    }

    private static boolean willMultiplyOverflow(long left, long right) {
        if (left == 0L || right == 0L) {
            return false;
        }
        if (left > 0L) {
            if (right > 0L) {
                return left > Long.MAX_VALUE / right;
            }
            return right < Long.MIN_VALUE / left;
        }
        if (right > 0L) {
            return left < Long.MIN_VALUE / right;
        }
        return left < Long.MAX_VALUE / right;
    }

    private static boolean fitsInLong(BigInteger value) {
        return value.bitLength() <= 63;
    }

    private static BigInteger toBigInteger(long value) {
        if (value == 0L) return BigInteger.ZERO;
        if (value == 1L) return BigInteger.ONE;
        if (value == Integer.MAX_VALUE) return BIG_INT_MAX;
        if (value == Long.MAX_VALUE) return BIG_LONG_MAX;
        if (value == Long.MIN_VALUE) return BIG_LONG_MIN;
        return BigInteger.valueOf(value);
    }

    @Override
    public final int intValue() {
        if (state == STATE_LONG) {
            return (int) longValue;
        }
        if (state == STATE_UNINITIALIZED) {
            return 0;
        }
        // STATE_BIG: value is outside long range, hence always outside int range
        return bigValue.signum() > 0 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
    }

    @Override
    public final long longValue() {
        if (state == STATE_LONG) {
            return longValue;
        }
        if (state == STATE_UNINITIALIZED) {
            return 0L;
        }
        // STATE_BIG: value is outside long range by normalize() invariant
        return bigValue.signum() > 0 ? Long.MAX_VALUE : Long.MIN_VALUE;
    }

    @Override
    public final float floatValue() {
        if (state == STATE_LONG) {
            return longValue;
        }
        if (state == STATE_UNINITIALIZED) {
            return 0;
        }
        return bigValue.floatValue();
    }

    @Override
    public final double doubleValue() {
        if (state == STATE_LONG) {
            return longValue;
        }
        if (state == STATE_UNINITIALIZED) {
            return 0.0D;
        }
        return bigValue.doubleValue();
    }

    public EnergyAmount init(long value) {
        longValue = value;
        bigValue = null;
        state = STATE_LONG;
        return this;
    }

    public EnergyAmount init(String value) {
        String trimmed = normalizeNumericString(value);
        return isLongValueString(trimmed) ? init(parseLongString(trimmed)) : init(new BigInteger(trimmed));
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
        bigValue = other.bigValue;
        longValue = other.longValue;
        state = other.state;
        return this;
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

    public final boolean isInitialized() {
        return state != STATE_UNINITIALIZED;
    }

    public final boolean isBig() {
        return state == STATE_BIG;
    }

    public final boolean isZero() {
        if (state == STATE_UNINITIALIZED || state == STATE_LONG) {
            return longValue == 0L;
        }
        return false;
    }

    public final boolean isPositive() {
        if (state == STATE_LONG) {
            return longValue > 0L;
        }
        if (state == STATE_BIG) {
            return bigValue.signum() > 0;
        }
        return false;
    }

    public final boolean isNegative() {
        if (state == STATE_LONG) {
            return longValue < 0L;
        }
        if (state == STATE_BIG) {
            return bigValue.signum() < 0;
        }
        return false;
    }

    public final boolean fitsLong() {
        return state == STATE_LONG || (state == STATE_BIG && fitsInLong(bigValue));
    }

    public final long asLongExact() {
        if (state == STATE_LONG) {
            return longValue;
        }
        if (state == STATE_BIG && fitsInLong(bigValue)) {
            return bigValue.longValueExact();
        }
        throw new ArithmeticException("EnergyAmount does not fit into long");
    }

    public final long asLongClamped() {
        return longValue();
    }

    public final BigInteger asBigInteger() {
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

    public final EnergyAmount multiply(long value) {
        if (!isInitialized()) {
            return setZero();
        }
        if (value == 0L) {
            return setZero();
        }
        if (value == 1L) {
            return this;
        }
        if (state == STATE_LONG) {
            if (willMultiplyOverflow(longValue, value)) {
                return init(toBigInteger(longValue).multiply(toBigInteger(value)));
            }
            return init(longValue * value);
        }
        bigValue = bigValue.multiply(toBigInteger(value));
        normalize();
        return this;
    }

    public final EnergyAmount multiply(double value) {
        validateFinite(value);
        if (!isInitialized()) {
            return setZero();
        }
        if (value == 0.0D) {
            return setZero();
        }
        if (value == 1.0D) {
            return this;
        }
        if (state == STATE_LONG) {
            long wholeValue = asWholeLong(value);
            if (value == (double) wholeValue) {
                return multiply(wholeValue);
            }
        }
        return init(truncateToInteger(asBigDecimal().multiply(BigDecimal.valueOf(value))));
    }

    public final EnergyAmount multiply(EnergyAmount other) {
        if (other == null || !other.isInitialized()) {
            return setZero();
        }
        if (state == STATE_LONG && other.state == STATE_LONG) {
            return multiply(other.longValue);
        }
        return init(asBigInteger().multiply(other.asBigInteger()));
    }

    public final EnergyAmount divide(long value) {
        if (value == 0L) {
            throw new ArithmeticException("Division by zero");
        }
        if (!isInitialized()) {
            return setZero();
        }
        if (value == 1L) {
            return this;
        }
        if (state == STATE_LONG) {
            if (longValue == Long.MIN_VALUE && value == -1L) {
                return init(toBigInteger(longValue).divide(toBigInteger(value)));
            }
            longValue /= value;
            return this;
        }
        bigValue = bigValue.divide(toBigInteger(value));
        normalize();
        return this;
    }

    public final EnergyAmount divide(double value) {
        validateFinite(value);
        if (value == 0.0D) {
            throw new ArithmeticException("Division by zero");
        }
        if (!isInitialized()) {
            return setZero();
        }
        if (value == 1.0D) {
            return this;
        }
        if (state == STATE_LONG) {
            long wholeValue = asWholeLong(value);
            if (value == (double) wholeValue) {
                return divide(wholeValue);
            }
        }
        return init(truncateToInteger(asBigDecimal().divide(BigDecimal.valueOf(value), 32, RoundingMode.DOWN)));
    }

    public final EnergyAmount divide(EnergyAmount other) {
        if (other == null || !other.isInitialized() || other.isZero()) {
            throw new ArithmeticException("Division by zero");
        }
        if (!isInitialized()) {
            return setZero();
        }
        if (state == STATE_LONG && other.state == STATE_LONG) {
            return divide(other.longValue);
        }
        bigValue = asBigInteger().divide(other.asBigInteger());
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

    public final int compareTo(long value) {
        if (state == STATE_UNINITIALIZED) {
            return Long.compare(0L, value);
        }
        if (state == STATE_LONG) {
            return Long.compare(longValue, value);
        }
        // STATE_BIG: value is outside long range, signum determines comparison with any long
        return bigValue.signum();
    }

    @Override
    public final int compareTo(@NotNull EnergyAmount other) {
        if (other == null || !other.isInitialized()) {
            return compareTo(0L);
        }
        if (state == STATE_UNINITIALIZED) {
            return -other.compareTo(0L);
        }
        if (state == STATE_LONG && other.state == STATE_LONG) {
            return Long.compare(longValue, other.longValue);
        }
        // At least one side is STATE_BIG (outside long range)
        if (state == STATE_BIG) {
            if (other.state == STATE_BIG) {
                return bigValue.compareTo(other.bigValue);
            }
            // other is LONG; this BIG is outside long range
            return bigValue.signum();
        }
        // this is LONG, other is BIG (outside long range)
        return -other.bigValue.signum();
    }

    @Override
    public final String toString() {
        if (state == STATE_UNINITIALIZED) {
            return "0";
        }
        return state == STATE_LONG ? Long.toString(longValue) : bigValue.toString();
    }

    private void normalize() {
        if (state == STATE_BIG && fitsInLong(bigValue)) {
            init(bigValue.longValue());
        }
    }

    private BigDecimal asBigDecimal() {
        if (state == STATE_BIG) {
            return new BigDecimal(bigValue);
        }
        if (state == STATE_UNINITIALIZED) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(longValue);
    }

}