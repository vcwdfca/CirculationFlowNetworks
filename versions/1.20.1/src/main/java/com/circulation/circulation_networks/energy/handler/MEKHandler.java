package com.circulation.circulation_networks.energy.handler;

import com.circulation.circulation_networks.api.EnergyAmount;
import com.circulation.circulation_networks.api.EnergyAmounts;
import com.circulation.circulation_networks.api.IEnergyHandler;
import com.circulation.circulation_networks.network.nodes.HubNode;
import com.circulation.circulation_networks.utils.EnergyAmountConversionUtils;
import mekanism.api.Action;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tile.TileEntityEnergyCube;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;

public final class MEKHandler implements IEnergyHandler {

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final Class<?> INDUCTION_PORT_CLASS;
    private static final double FE_TO_MEK_RATIO = 2.5D;
    private static final BigInteger MAX_DIRECT_DOUBLE_TRANSFER = BigDecimal.valueOf(Double.MAX_VALUE).toBigInteger();
    private static final BigInteger MAX_SCALED_DOUBLE_TRANSFER = BigDecimal.valueOf(Double.MAX_VALUE / FE_TO_MEK_RATIO).toBigInteger();

    static {
        Class<?> inductionPort;
        try {
            inductionPort = Class.forName("mekanism.common.tile.multiblock.TileEntityInductionPort");
        } catch (ClassNotFoundException e) {
            inductionPort = null;
        }
        INDUCTION_PORT_CLASS = inductionPort;
    }

    private final EnergyAmount maxOutput = EnergyAmountConversionUtils.obtainFromDoubleFloor(Double.MAX_VALUE);
    private final EnergyAmount needEnergy = EnergyAmount.obtain(0L);
    @Nullable
    private IStrictEnergyHandler send;
    @Nullable
    private IStrictEnergyHandler receive;
    private boolean isItem;
    private boolean creative;
    private EnergyType energyType = EnergyType.INVALID;

    public MEKHandler() {
    }

    private static void clampToDirectTransfer(EnergyAmount amount) {
        clampToMaximum(amount, MAX_DIRECT_DOUBLE_TRANSFER);
    }

    private static void clampToScaledTransfer(EnergyAmount amount) {
        clampToMaximum(amount, MAX_SCALED_DOUBLE_TRANSFER);
    }

    private static void clampToMaximum(EnergyAmount amount, BigInteger maximum) {
        if (amount == null || !amount.isInitialized() || amount.isNegative()) {
            return;
        }
        if (amount.asBigInteger().compareTo(maximum) > 0) {
            amount.init(maximum);
        }
    }

    private static boolean canExtractProbe(IStrictEnergyHandler handler) {
        return !handler.extractEnergy(FloatingLong.ONE, Action.SIMULATE).isZero();
    }

    private static boolean canReceiveProbe(IStrictEnergyHandler handler) {
        return !handler.insertEnergy(FloatingLong.ONE, Action.SIMULATE).equals(FloatingLong.ONE);
    }

    private static double getStoredEnergy(IStrictEnergyHandler handler) {
        double total = 0.0D;
        for (int i = 0, count = handler.getEnergyContainerCount(); i < count; i++) {
            total += handler.getEnergy(i).doubleValue();
        }
        return total;
    }

    private static double getMaxStoredEnergy(IStrictEnergyHandler handler) {
        double total = 0.0D;
        for (int i = 0, count = handler.getEnergyContainerCount(); i < count; i++) {
            total += handler.getMaxEnergy(i).doubleValue();
        }
        return total;
    }

    private void bindHandler(@Nullable IStrictEnergyHandler handler) {
        if (handler == null) {
            return;
        }
        if (send == null && canExtractProbe(handler)) {
            send = handler;
        }
        if (receive == null && canReceiveProbe(handler)) {
            receive = handler;
        }
    }

    @Nullable
    private static IStrictEnergyHandler getUnsidedHandler(BlockEntity blockEntity) {
        return blockEntity.getCapability(Capabilities.STRICT_ENERGY, null).orElse(null);
    }

    @Override
    public IEnergyHandler init(BlockEntity blockEntity, @Nullable HubNode.HubMetadata hubMetadata) {
        EnergyAmountConversionUtils.setFromDoubleFloor(maxOutput, Double.MAX_VALUE);
        if (blockEntity instanceof TileEntityEnergyCube energyCube) {
            IStrictEnergyHandler handler = getUnsidedHandler(blockEntity);
            if (handler != null) {
                send = handler;
                receive = handler;
                creative = energyCube.getTier() == EnergyCubeTier.CREATIVE;
                energyType = EnergyType.STORAGE;
                EnergyAmountConversionUtils.setFromDoubleFloor(maxOutput, energyCube.getTier().getOutput().doubleValue());
                return this;
            }
        } else if (INDUCTION_PORT_CLASS != null && INDUCTION_PORT_CLASS.isInstance(blockEntity)) {
            IStrictEnergyHandler handler = getUnsidedHandler(blockEntity);
            if (handler != null) {
                send = handler;
                receive = handler;
                energyType = EnergyType.STORAGE;
                return this;
            }
        }
        for (Direction direction : DIRECTIONS) {
            if (send != null && receive != null) {
                break;
            }
            var optional = blockEntity.getCapability(Capabilities.STRICT_ENERGY, direction);
            if (!optional.isPresent()) {
                continue;
            }
            IStrictEnergyHandler handler = optional.orElse(null);
            if (handler == null) {
                continue;
            }
            bindHandler(handler);
        }
        if (send == null && receive == null) {
            var unsided = blockEntity.getCapability(Capabilities.STRICT_ENERGY, null).orElse(null);
            if (unsided != null) {
                bindHandler(unsided);
                if (send == null && receive == null) {
                    send = unsided;
                    receive = unsided;
                }
            }
        }
        // Detect creative (infinite energy storage)
        IStrictEnergyHandler primary = send != null ? send : receive;
        if (primary != null) {
            for (int i = 0, count = primary.getEnergyContainerCount(); i < count; i++) {
                if (primary.getMaxEnergy(i).equals(FloatingLong.MAX_VALUE)) {
                    creative = true;
                    break;
                }
            }
        }
        // Compute maxOutput from probes
        if (!creative) {
            double detectedRate = Double.MAX_VALUE;
            if (send != null) {
                double probe = send.extractEnergy(FloatingLong.MAX_VALUE, Action.SIMULATE).doubleValue();
                double stored = getStoredEnergy(send);
                if (stored > 0 && probe < stored) {
                    detectedRate = Math.min(detectedRate, probe);
                }
            }
            if (receive != null) {
                FloatingLong remainder = receive.insertEnergy(FloatingLong.MAX_VALUE, Action.SIMULATE);
                double accepted = FloatingLong.MAX_VALUE.subtract(remainder).doubleValue();
                double room = getMaxStoredEnergy(receive) - getStoredEnergy(receive);
                if (room > 0 && accepted < room) {
                    detectedRate = Math.min(detectedRate, accepted);
                }
            }
            EnergyAmountConversionUtils.setFromDoubleFloor(maxOutput, detectedRate);
        }
        // Set energy type
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
        var optional = itemStack.getCapability(Capabilities.STRICT_ENERGY);
        if (optional.isPresent()) {
            IStrictEnergyHandler handler = optional.orElse(null);
            if (handler != null) {
                receive = handler;
                double stored = getStoredEnergy(handler);
                double max = getMaxStoredEnergy(handler);
                double remaining = max - stored;
                double r = remaining / 10.0D;
                FloatingLong insertRemainder = handler.insertEnergy(FloatingLong.MAX_VALUE, Action.SIMULATE);
                double maxInsert = FloatingLong.MAX_VALUE.subtract(insertRemainder).doubleValue();
                EnergyAmountConversionUtils.setFromDoubleFloor(needEnergy, Math.max(0.0D, Math.min(r, maxInsert)));
            }
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
        creative = false;
        isItem = false;
        needEnergy.setZero();
    }

    @Override
    public EnergyAmount receiveEnergy(EnergyAmount maxReceive, @Nullable HubNode.HubMetadata hubMetadata) {
        if (isItem) {
            if (receive == null) return EnergyAmounts.ZERO;
            EnergyAmount accepted = EnergyAmount.obtain(needEnergy).min(maxReceive);
            clampToDirectTransfer(accepted);
            if (!accepted.isZero()) {
                double addAmount = EnergyAmountConversionUtils.toDoubleClamped(accepted);
                for (int i = 0, count = receive.getEnergyContainerCount(); i < count && addAmount > 0; i++) {
                    double current = receive.getEnergy(i).doubleValue();
                    double maxE = receive.getMaxEnergy(i).doubleValue();
                    double room = maxE - current;
                    double add = Math.min(room, addAmount);
                    if (add > 0) {
                        receive.setEnergy(i, FloatingLong.create(current + add));
                        addAmount -= add;
                    }
                }
                needEnergy.subtract(accepted);
            }
            return accepted;
        } else {
            if (receive == null) return EnergyAmounts.ZERO;
            EnergyAmount receivable = canReceiveValue(hubMetadata);
            receivable.min(maxReceive);
            clampToScaledTransfer(receivable);
            if (!receivable.isZero()) {
                double addJoules = EnergyAmountConversionUtils.toDoubleClamped(receivable) * FE_TO_MEK_RATIO;
                for (int i = 0, count = receive.getEnergyContainerCount(); i < count && addJoules > 0; i++) {
                    double current = receive.getEnergy(i).doubleValue();
                    double maxE = receive.getMaxEnergy(i).doubleValue();
                    double room = maxE - current;
                    double add = Math.min(room, addJoules);
                    if (add > 0) {
                        receive.setEnergy(i, FloatingLong.create(current + add));
                        addJoules -= add;
                    }
                }
            }
            return receivable;
        }
    }

    @Override
    public EnergyAmount extractEnergy(EnergyAmount maxExtract, @Nullable HubNode.HubMetadata hubMetadata) {
        if (send == null) return EnergyAmounts.ZERO;
        EnergyAmount extractable = canExtractValue(hubMetadata);
        extractable.min(maxExtract);
        clampToScaledTransfer(extractable);
        if (!extractable.isZero() && !creative) {
            double subJoules = EnergyAmountConversionUtils.toDoubleClamped(extractable) * FE_TO_MEK_RATIO;
            for (int i = 0, count = send.getEnergyContainerCount(); i < count && subJoules > 0; i++) {
                double current = send.getEnergy(i).doubleValue();
                double sub = Math.min(current, subJoules);
                if (sub > 0) {
                    send.setEnergy(i, FloatingLong.create(current - sub));
                    subJoules -= sub;
                }
            }
        }
        return extractable;
    }

    @Override
    public EnergyAmount canExtractValue(@Nullable HubNode.HubMetadata hubMetadata) {
        if (send == null) return EnergyAmounts.ZERO;
        if (creative) return EnergyAmount.obtain(maxOutput);
        EnergyAmount extractable = EnergyAmountConversionUtils.obtainFromDoubleFloor(getStoredEnergy(send) * 0.4D);
        return extractable.min(maxOutput);
    }

    @Override
    public EnergyAmount canReceiveValue(@Nullable HubNode.HubMetadata hubMetadata) {
        if (isItem) {
            return EnergyAmount.obtain(needEnergy);
        } else {
            if (receive == null) return EnergyAmounts.ZERO;
            EnergyAmount receivable = EnergyAmountConversionUtils.obtainFromDoubleFloor(
                    (getMaxStoredEnergy(receive) - getStoredEnergy(receive)) * 0.4D
            );
            return receivable.min(maxOutput);
        }
    }

    @Override
    public boolean canExtract(IEnergyHandler receiveHandler, @Nullable HubNode.HubMetadata hubMetadata) {
        if (creative) return true;
        return send != null && getStoredEnergy(send) >= 2.5D;
    }

    @Override
    public boolean canReceive(IEnergyHandler sendHandler, @Nullable HubNode.HubMetadata hubMetadata) {
        if (isItem) return needEnergy.isPositive();
        else return receive != null && (getMaxStoredEnergy(receive) - getStoredEnergy(receive)) * 0.4D > 0.0D;
    }

    @Override
    public EnergyType getType(@Nullable HubNode.HubMetadata hubMetadata) {
        return energyType;
    }
}
