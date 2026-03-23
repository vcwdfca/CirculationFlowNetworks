package com.circulation.circulation_networks.utils;

public abstract class CI18n {

    public static CI18n INSTANCE;

    public abstract String format(String key, Object... params);

    public abstract boolean hasKey(String key);
}