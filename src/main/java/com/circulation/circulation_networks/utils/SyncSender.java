package com.circulation.circulation_networks.utils;

public interface SyncSender {
    void sendInt(int channel, int value);

    void sendLong(int channel, long value);

    void sendString(int channel, String value);
}