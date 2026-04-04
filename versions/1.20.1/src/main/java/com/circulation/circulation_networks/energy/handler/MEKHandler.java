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
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

public final class MEKHandler implements IEnergyHandler {

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final double FE_TO_J_RATIO = 2.5D;
    private static final double J_TO_FE_RATIO = 0.4D;

    @Nullable
    private IStrictEnergyHandler send;
    @Nullable
    private IStrictEnergyHandler receive;
    private EnergyType energyType;

    public MEKHandler() {
    }

    private static boolean canExtract(IStrictEnergyHandler handler) {
        return !handler.extractEnergy(FloatingLong.ONE, Action.SIMULATE).isZero();
    }

    private static boolean canReceive(IStrictEnergyHandler handler) {
        return !handler.insertEnergy(FloatingLong.ONE, Action.SIMULATE).equals(FloatingLong.ONE);
    }

    private static FloatingLong toStrictEnergy(EnergyAmount amount) {
        EnergyAmount strictEnergy = EnergyAmount.obtain(amount);
        try {
            strictEnergy.multiply(FE_TO_J_RATIO);
            return FloatingLong.create(EnergyAmountConversionUtils.toDoubleClamped(strictEnergy));
        } finally {
            strictEnergy.recycle();
        }
    }

    private static EnergyAmount toFeAmount(FloatingLong amount) {
        if (amount == null || amount.isZero()) {
            return EnergyAmounts.ZERO;
        }
        return EnergyAmountConversionUtils.obtainFromDoubleFloor(amount.doubleValue() * J_TO_FE_RATIO);
    }

    @Override
    public IEnergyHandler init(BlockEntity blockEntity, @Nullable HubNode.HubMetadata hubMetadata) {
        IStrictEnergyHandler fallback = null;
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
            if (fallback == null) {
                fallback = handler;
            }
            if (send == null && canExtract(handler)) {
                send = handler;
            }
            if (receive == null && canReceive(handler)) {
                receive = handler;
            }
        }
        if (send == null) {
            send = fallback;
        }
        if (receive == null) {
            receive = fallback;
        }
        return this;
    }

    @Override
    public IEnergyHandler init(ItemStack itemStack, @Nullable HubNode.HubMetadata hubMetadata) {
        var optional = itemStack.getCapability(Capabilities.STRICT_ENERGY);
        if (optional.isPresent()) {
            receive = optional.orElse(null);
        }
        return this;
    }

    @Override
    public void clear() {
        send = null;
        receive = null;
        energyType = null;
    }

    @Override
    public EnergyAmount extractEnergy(EnergyAmount maxExtract, @Nullable HubNode.HubMetadata hubMetadata) {
        if (send == null) {
            return EnergyAmounts.ZERO;
        }
        EnergyAmount extractable = canExtractValue(hubMetadata);
        try {
            extractable.min(maxExtract);
            if (extractable.isZero()) {
                return extractable;
            }
            FloatingLong request = toStrictEnergy(extractable);
            EnergyAmount extracted = toFeAmount(send.extractEnergy(request, Action.EXECUTE));
            extracted.min(extractable);
            return extracted;
        } finally {
            extractable.recycle();
        }
    }

    @Override
    public EnergyAmount receiveEnergy(EnergyAmount maxReceive, @Nullable HubNode.HubMetadata hubMetadata) {
        if (receive == null) {
            return EnergyAmounts.ZERO;
        }
        EnergyAmount receivable = canReceiveValue(hubMetadata);
        try {
            receivable.min(maxReceive);
            if (receivable.isZero()) {
                return receivable;
            }
            FloatingLong request = toStrictEnergy(receivable);
            FloatingLong remainder = receive.insertEnergy(request, Action.EXECUTE);
            EnergyAmount accepted = toFeAmount(request.subtract(remainder));
            accepted.min(receivable);
            return accepted;
        } finally {
            receivable.recycle();
        }
    }

    @Override
    public EnergyAmount canExtractValue(@Nullable HubNode.HubMetadata hubMetadata) {
        if (send == null) {
            return EnergyAmounts.ZERO;
        }
        return toFeAmount(send.extractEnergy(FloatingLong.MAX_VALUE, Action.SIMULATE));
    }

    @Override
    public EnergyAmount canReceiveValue(@Nullable HubNode.HubMetadata hubMetadata) {
        if (receive == null) {
            return EnergyAmounts.ZERO;
        }
        FloatingLong remainder = receive.insertEnergy(FloatingLong.MAX_VALUE, Action.SIMULATE);
        return toFeAmount(FloatingLong.MAX_VALUE.subtract(remainder));
    }

    @Override
    public EnergyType getType(@Nullable HubNode.HubMetadata hubMetadata) {
        if (energyType == null) {
            boolean hasReceive = receive != null;
            if (send != null) {
                return energyType = hasReceive ? EnergyType.STORAGE : EnergyType.SEND;
            } else if (hasReceive) {
                return energyType = EnergyType.RECEIVE;
            }
            return energyType = EnergyType.INVALID;
        }
        return energyType;
    }

    @Override
    public boolean canExtract(IEnergyHandler receiveHandler, @Nullable HubNode.HubMetadata hubMetadata) {
        return send != null;
    }

    @Override
    public boolean canReceive(IEnergyHandler sendHandler, @Nullable HubNode.HubMetadata hubMetadata) {
        return receive != null;
    }
}
