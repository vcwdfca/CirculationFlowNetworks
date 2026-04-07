package com.circulation.circulation_networks.energy.handler;

import com.circulation.circulation_networks.api.EnergyAmount;
import com.circulation.circulation_networks.api.EnergyAmounts;
import com.circulation.circulation_networks.api.IEnergyHandler;
import com.circulation.circulation_networks.network.nodes.HubNode;
import com.circulation.circulation_networks.utils.EnergyAmountConversionUtils;
import mekanism.api.Action;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.multiblock.TileEntityInductionPort;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;

public final class MEKHandler implements IEnergyHandler {

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final double FE_TO_MEK_RATIO = 2.5D;
    private static final BigInteger MAX_DIRECT_DOUBLE_TRANSFER = BigDecimal.valueOf(Double.MAX_VALUE).toBigInteger();
    private static final BigInteger MAX_SCALED_DOUBLE_TRANSFER = BigDecimal.valueOf(Double.MAX_VALUE / FE_TO_MEK_RATIO).toBigInteger();

    private final EnergyAmount maxOutput = EnergyAmountConversionUtils.obtainFromDoubleFloor(Double.MAX_VALUE);
    private final EnergyAmount needEnergy = EnergyAmount.obtain(0L);
    @Nullable
    private IStrictEnergyHandler send;
    @Nullable
    private IStrictEnergyHandler receive;
    private boolean isItem;
    private EnergyType energyType = EnergyType.INVALID;

    public MEKHandler() {
    }

    private static void clampToMaximum(EnergyAmount amount, BigInteger maximum) {
        if (amount == null || !amount.isInitialized() || amount.isNegative()) {
            return;
        }
        if (amount.asBigInteger().compareTo(maximum) > 0) {
            amount.init(maximum);
        }
    }

    private static double joulesToFe(double joules) {
        return joules / FE_TO_MEK_RATIO;
    }

    private static double getStoredEnergy(IStrictEnergyHandler handler) {
        double total = 0.0D;
        for (int i = 0, count = handler.getEnergyContainerCount(); i < count; i++) {
            total += (double) handler.getEnergy(i);
        }
        return total;
    }

    private static double getMaxStoredEnergy(IStrictEnergyHandler handler) {
        double total = 0.0D;
        for (int i = 0, count = handler.getEnergyContainerCount(); i < count; i++) {
            total += (double) handler.getMaxEnergy(i);
        }
        return total;
    }

    private void bindHandler(@Nullable IStrictEnergyHandler handler) {
        if (handler == null) {
            return;
        }
        if (send == null && handler.extractEnergy(1L, Action.SIMULATE) > 0L) {
            send = handler;
        }
        if (receive == null && handler.insertEnergy(1L, Action.SIMULATE) == 0L) {
            receive = handler;
        }
    }

    @Override
    public IEnergyHandler init(BlockEntity blockEntity, @Nullable HubNode.HubMetadata hubMetadata) {
        EnergyAmountConversionUtils.setFromDoubleFloor(maxOutput, Double.MAX_VALUE);
        var level = blockEntity.getLevel();
        if (level == null) {
            return this;
        }
        if (blockEntity instanceof TileEntityEnergyCube energyCube) {
            send = energyCube;
            receive = energyCube;
            energyType = EnergyType.STORAGE;
            EnergyAmountConversionUtils.setFromDoubleFloor(maxOutput, joulesToFe(energyCube.getTier().getOutput()));
            return this;
        }
        if (blockEntity instanceof TileEntityInductionPort port) {
            send = port;
            receive = port;
            energyType = EnergyType.STORAGE;
            return this;
        }
        var pos = blockEntity.getBlockPos();
        for (Direction direction : DIRECTIONS) {
            if (send != null && receive != null) {
                break;
            }
            IStrictEnergyHandler handler = level.getCapability(Capabilities.STRICT_ENERGY.block(), pos, direction);
            if (handler == null) {
                continue;
            }
            bindHandler(handler);
        }
        if (send == null && receive == null) {
            IStrictEnergyHandler fallback = level.getCapability(Capabilities.STRICT_ENERGY.block(), pos, null);
            if (fallback != null) {
                bindHandler(fallback);
                if (send == null && receive == null) {
                    send = fallback;
                    receive = fallback;
                }
            }
        }
        double detectedRate = Double.MAX_VALUE;
        if (send != null) {
            long probe = send.extractEnergy(Long.MAX_VALUE, Action.SIMULATE);
            double stored = getStoredEnergy(send);
            if (stored > 0 && (double) probe < stored) {
                detectedRate = Math.min(detectedRate, (double) probe);
            }
        }
        if (receive != null) {
            long remainder = receive.insertEnergy(Long.MAX_VALUE, Action.SIMULATE);
            double accepted = (double) (Long.MAX_VALUE - remainder);
            double room = getMaxStoredEnergy(receive) - getStoredEnergy(receive);
            if (room > 0 && accepted < room) {
                detectedRate = Math.min(detectedRate, accepted);
            }
        }
        EnergyAmountConversionUtils.setFromDoubleFloor(maxOutput, joulesToFe(detectedRate));
        if (send != null) {
            energyType = receive != null ? EnergyType.STORAGE : EnergyType.SEND;
        } else if (receive != null) {
            energyType = EnergyType.RECEIVE;
        }
        return this;
    }

    @Override
    public IEnergyHandler init(ItemStack itemStack, @Nullable HubNode.HubMetadata hubMetadata) {
        isItem = true;
        IStrictEnergyHandler handler = itemStack.getCapability(Capabilities.STRICT_ENERGY.item());
        if (handler != null) {
            receive = handler;
            double stored = getStoredEnergy(handler);
            double max = getMaxStoredEnergy(handler);
            double remaining = max - stored;
            double r = remaining / 10.0D;
            long insertRemainder = handler.insertEnergy(Long.MAX_VALUE, Action.SIMULATE);
            double maxInsert = (double) (Long.MAX_VALUE - insertRemainder);
            EnergyAmountConversionUtils.setFromDoubleFloor(needEnergy, joulesToFe(Math.clamp(r, 0.0D, maxInsert)));
        }
        energyType = EnergyType.RECEIVE;
        return this;
    }

    @Override
    public void clear() {
        EnergyAmountConversionUtils.setFromDoubleFloor(maxOutput, Double.MAX_VALUE);
        send = null;
        receive = null;
        energyType = EnergyType.INVALID;
        isItem = false;
        needEnergy.setZero();
    }

    @Override
    public EnergyAmount receiveEnergy(EnergyAmount maxReceive, @Nullable HubNode.HubMetadata hubMetadata) {
        if (isItem) {
            if (receive == null) return EnergyAmounts.ZERO;
            EnergyAmount accepted = EnergyAmount.obtain(needEnergy).min(maxReceive);
            clampToMaximum(accepted, MAX_DIRECT_DOUBLE_TRANSFER);
            if (accepted.isZero()) {
                return accepted;
            }
            long requested = (long) (EnergyAmountConversionUtils.toDoubleClamped(accepted) * FE_TO_MEK_RATIO);
            long remainder = receive.insertEnergy(requested, Action.EXECUTE);
            long inserted = Math.max(0L, requested - remainder);
            EnergyAmount actual = EnergyAmountConversionUtils.obtainFromDoubleFloor(joulesToFe((double) inserted));
            needEnergy.subtract(actual);
            return actual;
        }
        EnergyAmount receivable = canReceiveValue(hubMetadata);
        receivable.min(maxReceive);
        clampToMaximum(receivable, MAX_SCALED_DOUBLE_TRANSFER);
        if (receivable.isZero()) {
            return receivable;
        }
        long requestJoules = (long) (EnergyAmountConversionUtils.toDoubleClamped(receivable) * FE_TO_MEK_RATIO);
        long insertedJoules;
        if (receive == null) return EnergyAmounts.ZERO;
        long remainder = receive.insertEnergy(requestJoules, Action.EXECUTE);
        insertedJoules = requestJoules - remainder;
        return EnergyAmountConversionUtils.obtainFromDoubleFloor(insertedJoules / FE_TO_MEK_RATIO);
    }

    @Override
    public EnergyAmount extractEnergy(EnergyAmount maxExtract, @Nullable HubNode.HubMetadata hubMetadata) {
        EnergyAmount extractable = canExtractValue(hubMetadata);
        extractable.min(maxExtract);
        clampToMaximum(extractable, MAX_SCALED_DOUBLE_TRANSFER);
        if (extractable.isZero()) {
            return extractable;
        }
        long requestJoules = (long) (EnergyAmountConversionUtils.toDoubleClamped(extractable) * FE_TO_MEK_RATIO);
        long extractedJoules;
        if (send == null) return EnergyAmounts.ZERO;
        extractedJoules = send.extractEnergy(requestJoules, Action.EXECUTE);
        return EnergyAmountConversionUtils.obtainFromDoubleFloor(extractedJoules / FE_TO_MEK_RATIO);
    }

    @Override
    public EnergyAmount canExtractValue(@Nullable HubNode.HubMetadata hubMetadata) {
        if (send == null) return EnergyAmounts.ZERO;
        EnergyAmount extractable = EnergyAmountConversionUtils.obtainFromDoubleFloor(getStoredEnergy(send) * 0.4D);
        return extractable.min(maxOutput);
    }

    @Override
    public EnergyAmount canReceiveValue(@Nullable HubNode.HubMetadata hubMetadata) {
        if (isItem) {
            return EnergyAmount.obtain(needEnergy);
        }
        if (receive == null) return EnergyAmounts.ZERO;
        EnergyAmount receivable = EnergyAmountConversionUtils.obtainFromDoubleFloor(
                (getMaxStoredEnergy(receive) - getStoredEnergy(receive)) * 0.4D
        );
        return receivable.min(maxOutput);
    }

    @Override
    public boolean canExtract(IEnergyHandler receiveHandler, @Nullable HubNode.HubMetadata hubMetadata) {
        return send != null && getStoredEnergy(send) >= 2.5D;
    }

    @Override
    public boolean canReceive(IEnergyHandler sendHandler, @Nullable HubNode.HubMetadata hubMetadata) {
        if (isItem) return needEnergy.isPositive();
        return receive != null && (getMaxStoredEnergy(receive) - getStoredEnergy(receive)) * 0.4D > 0.0D;
    }

    @Override
    public EnergyType getType(@Nullable HubNode.HubMetadata hubMetadata) {
        return energyType;
    }
}
