package com.circulation.circulation_networks.energy.handler;

import com.circulation.circulation_networks.api.EnergyAmount;
import com.circulation.circulation_networks.api.EnergyAmounts;
import com.circulation.circulation_networks.api.IEnergyHandler;
import com.circulation.circulation_networks.network.nodes.HubNode;
import mekanism.api.Action;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

public final class MEKHandler implements IEnergyHandler {

    private static final Direction[] DIRECTIONS = Direction.values();

    @Nullable
    private IStrictEnergyHandler send;
    @Nullable
    private IStrictEnergyHandler receive;
    private EnergyType energyType;

    public MEKHandler() {
    }

    private static boolean canExtract(IStrictEnergyHandler handler) {
        return handler.extractEnergy(1L, Action.SIMULATE) > 0L;
    }

    private static boolean canReceive(IStrictEnergyHandler handler) {
        return handler.insertEnergy(1L, Action.SIMULATE) == 0L;
    }

    private static long toStrictEnergy(EnergyAmount amount) {
        EnergyAmount strictEnergy = EnergyAmount.obtain(amount);
        try {
            strictEnergy.multiply(5L);
            if (!strictEnergy.isZero()) {
                strictEnergy.add(1L);
            }
            strictEnergy.divide(2L);
            return strictEnergy.asLongClamped();
        } finally {
            strictEnergy.recycle();
        }
    }

    private static EnergyAmount toFeAmount(long amount) {
        if (amount <= 0L) {
            return EnergyAmounts.ZERO;
        }
        return EnergyAmount.obtain(amount).multiply(2L).divide(5L);
    }

    @Override
    public IEnergyHandler init(BlockEntity blockEntity, @Nullable HubNode.HubMetadata hubMetadata) {
        var level = blockEntity.getLevel();
        if (level == null) {
            return this;
        }
        var pos = blockEntity.getBlockPos();
        IStrictEnergyHandler fallback = null;
        for (Direction direction : DIRECTIONS) {
            if (send != null && receive != null) {
                break;
            }
            IStrictEnergyHandler handler = level.getCapability(Capabilities.STRICT_ENERGY.block(), pos, direction);
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
        receive = itemStack.getCapability(Capabilities.STRICT_ENERGY.item());
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
            long request = toStrictEnergy(extractable);
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
            long request = toStrictEnergy(receivable);
            long remainder = receive.insertEnergy(request, Action.EXECUTE);
            EnergyAmount accepted = toFeAmount(request - remainder);
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
        return toFeAmount(send.extractEnergy(Long.MAX_VALUE, Action.SIMULATE));
    }

    @Override
    public EnergyAmount canReceiveValue(@Nullable HubNode.HubMetadata hubMetadata) {
        if (receive == null) {
            return EnergyAmounts.ZERO;
        }
        long remainder = receive.insertEnergy(Long.MAX_VALUE, Action.SIMULATE);
        return toFeAmount(Long.MAX_VALUE - remainder);
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
