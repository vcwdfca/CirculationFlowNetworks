package com.circulation.circulation_networks.utils;

import javax.annotation.Nonnull;

public abstract class CI18n {

    private static CI18n INSTANCE;

    public static void setI18nInternal(CI18n INSTANCE) {
        CI18n.INSTANCE = INSTANCE;
    }

    @Nonnull
    public static String format(String key, Object... params) {
        return INSTANCE == null ? "" : INSTANCE.formatInternal(key, params);
    }

    public static boolean hasKey(String key) {
        return INSTANCE != null && INSTANCE.hasKeyInternal(key);
    }

    protected abstract String formatInternal(String key, Object... params);

    protected abstract boolean hasKeyInternal(String key);
}