package com.circulation.circulation_networks.utils;

import com.circulation.circulation_networks.CirculationFlowNetworks;
import com.circulation.circulation_networks.container.CFNBaseContainer;
import com.circulation.circulation_networks.packets.ContainerProgressBar;
import com.circulation.circulation_networks.packets.ContainerValueConfig;
import lombok.Getter;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IContainerListener;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.Objects;

public class SyncData {
    private final CFNBaseContainer source;
    private final Field field;
    @Getter
    private final int channel;
    private final SyncType syncType;

    private long numericVersion = 0L;
    private String stringVersion = null;

    public SyncData(CFNBaseContainer container, Field field, GuiSync annotation) {
        this.source = container;
        this.field = field;
        this.channel = annotation.value();
        this.syncType = determineSyncType(field);
    }

    private static SyncType determineSyncType(Field field) {
        Class<?> type = field.getType();
        if (type == String.class) {
            return SyncType.STRING;
        } else if (type == Integer.TYPE || type == Integer.class) {
            return SyncType.INT;
        } else if (type == Long.TYPE || type == Long.class) {
            return SyncType.LONG;
        } else if (type == Boolean.TYPE || type == Boolean.class) {
            return SyncType.BOOLEAN;
        } else if (type.isEnum()) {
            return SyncType.ENUM;
        }
        return SyncType.INT;
    }

    public void tick(IContainerListener c) {
        try {
            Object val = this.field.get(this.source);
            boolean needsSync = isValueChanged(val);

            if (needsSync) {
                try {
                    this.send(c, val);
                    updateCachedVersion(val);
                } catch (Exception e) {
                    CirculationFlowNetworks.LOGGER.debug("Failed to sync data", e);
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            CirculationFlowNetworks.LOGGER.debug(e);
        }
    }

    private boolean isValueChanged(Object val) {
        return switch (syncType) {
            case STRING -> !Objects.equals(val, stringVersion);
            case INT, LONG, BOOLEAN, ENUM -> extractNumericValue(val) != numericVersion;
        };
    }

    private long extractNumericValue(Object val) {
        if (val == null) return 0L;
        return switch (syncType) {
            case INT -> (Integer) val;
            case LONG -> (Long) val;
            case BOOLEAN -> ((Boolean) val) ? 1L : 0L;
            case ENUM -> ((Enum<?>) val).ordinal();
            default -> 0L;
        };
    }

    private void updateCachedVersion(Object val) {
        switch (syncType) {
            case STRING -> stringVersion = (String) val;
            case INT, LONG, BOOLEAN, ENUM -> numericVersion = extractNumericValue(val);
        }
    }

    private void send(IContainerListener o, Object val) {
        switch (syncType) {
            case STRING:
                if (o instanceof EntityPlayerMP) {
                    CirculationFlowNetworks.NET_CHANNEL.sendTo(
                        new ContainerValueConfig((short) this.channel, (String) val),
                        (EntityPlayerMP) o
                    );
                }
                break;
            case ENUM:
                o.sendWindowProperty(this.source, this.channel, ((Enum<?>) val).ordinal());
                break;
            case BOOLEAN:
                o.sendWindowProperty(this.source, this.channel, ((Boolean) val) ? 1 : 0);
                break;
            case INT:
                o.sendWindowProperty(this.source, this.channel, (Integer) val);
                break;
            case LONG:
                if (o instanceof EntityPlayerMP) {
                    CirculationFlowNetworks.NET_CHANNEL.sendTo(
                        new ContainerProgressBar((short) this.channel, (Long) val),
                        (EntityPlayerMP) o
                    );
                }
                break;
        }
    }

    public void update(Object val) {
        try {
            final Object oldValue = this.field.get(this.source);
            switch (syncType) {
                case STRING:
                    this.field.set(this.source, val);
                    break;
                case INT:
                    this.field.set(this.source, ((Number) val).intValue());
                    break;
                case LONG:
                    this.field.set(this.source, ((Number) val).longValue());
                    break;
                case BOOLEAN:
                    this.field.set(this.source, ((Number) val).longValue() == 1L);
                    break;
                case ENUM:
                    updateEnum(((Number) val).longValue());
                    break;
            }
            this.source.onUpdate(this.field.getName(), oldValue, this.field.get(this.source));
        } catch (IllegalArgumentException | IllegalAccessException e) {
            CirculationFlowNetworks.LOGGER.debug(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <E extends Enum<E>> void updateEnum(long ordinal) throws IllegalAccessException {
        Class<E> enumType = (Class<E>) this.field.getType();
        for (Enum<E> e : EnumSet.allOf(enumType)) {
            if ((long) e.ordinal() == ordinal) {
                this.field.set(this.source, e);
                break;
            }
        }
    }

    enum SyncType {
        STRING, INT, LONG, BOOLEAN, ENUM
    }
}
