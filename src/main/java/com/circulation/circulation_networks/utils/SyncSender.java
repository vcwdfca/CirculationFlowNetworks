package com.circulation.circulation_networks.utils;

public interface SyncSender {
    void sendInt(int channel, int value);

    void sendLong(int channel, long value);

    void sendByte(int channel, byte value);

    void sendShort(int channel, short value);

    void sendString(int channel, String value);
}