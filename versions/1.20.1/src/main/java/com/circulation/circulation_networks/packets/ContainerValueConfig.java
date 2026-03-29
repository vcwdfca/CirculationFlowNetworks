package com.circulation.circulation_networks.packets;

import com.circulation.circulation_networks.container.CFNBaseContainer;
import com.circulation.circulation_networks.utils.Packet;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.function.Supplier;

public final class ContainerValueConfig implements Packet<ContainerValueConfig> {

    private static final int COMPRESSION_THRESHOLD = 256;
    private static final byte TYPE_STRING = 0;
    private static final byte TYPE_BYTES = 1;

    private final short name;
    private final byte type;
    private final String stringValue;
    private final byte[] bytesValue;

    public ContainerValueConfig() {
        this((short) 0, TYPE_STRING, null, null);
    }

    public ContainerValueConfig(short name, String value) {
        this(name, TYPE_STRING, value, null);
    }

    public ContainerValueConfig(short name, byte[] value) {
        this(name, TYPE_BYTES, null, value != null ? value.clone() : new byte[0]);
    }

    private ContainerValueConfig(short name, byte type, String stringValue, byte[] bytesValue) {
        this.name = name;
        this.type = type;
        this.stringValue = stringValue;
        this.bytesValue = bytesValue;
    }

    public ContainerValueConfig decode(FriendlyByteBuf buf) {
        short name = buf.readShort();
        byte type = buf.readByte();
        boolean compressed = buf.readBoolean();
        int length = buf.readInt();
        byte[] payload = new byte[length];
        buf.readBytes(payload);
        byte[] decoded = compressed ? inflate(payload) : payload;
        if (type == TYPE_BYTES) {
            return new ContainerValueConfig(name, type, null, decoded);
        }
        return new ContainerValueConfig(name, type, new String(decoded, StandardCharsets.UTF_8), null);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeShort(name);
        buf.writeByte(type);
        byte[] raw = type == TYPE_BYTES
            ? (bytesValue != null ? bytesValue : new byte[0])
            : (stringValue != null ? stringValue.getBytes(StandardCharsets.UTF_8) : new byte[0]);
        byte[] payload = raw;
        boolean compressed = raw.length >= COMPRESSION_THRESHOLD;
        if (compressed) {
            byte[] compressedPayload = deflate(raw);
            if (compressedPayload.length < raw.length) {
                payload = compressedPayload;
            } else {
                compressed = false;
            }
        }

        buf.writeBoolean(compressed);
        buf.writeInt(payload.length);
        buf.writeBytes(payload);
    }

    public void handle(ContainerValueConfig message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null || !FMLEnvironment.dist.isClient()) {
                return;
            }

            var player = Minecraft.getInstance().player;
            if (player == null || !(player.containerMenu instanceof CFNBaseContainer container)) {
                return;
            }
            if (message.type == TYPE_BYTES) {
                container.bytesSync(message.name, message.bytesValue);
            } else {
                container.stringSync(message.name, message.stringValue);
            }
        });
        context.setPacketHandled(true);
    }

    private static byte[] deflate(byte[] input) {
        Deflater deflater = new Deflater(Deflater.BEST_SPEED);
        deflater.setInput(input);
        deflater.finish();

        byte[] buffer = new byte[Math.max(256, Math.min(4096, input.length))];
        try (ByteArrayOutputStream output = new ByteArrayOutputStream(input.length)) {
            while (!deflater.finished()) {
                int written = deflater.deflate(buffer);
                if (written <= 0) {
                    break;
                }
                output.write(buffer, 0, written);
            }
            return output.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deflate container sync payload", e);
        } finally {
            deflater.end();
        }
    }

    private static byte[] inflate(byte[] input) {
        Inflater inflater = new Inflater();
        inflater.setInput(input);

        byte[] buffer = new byte[Math.max(256, Math.min(4096, input.length * 2))];
        try (ByteArrayOutputStream output = new ByteArrayOutputStream(input.length * 2)) {
            while (!inflater.finished()) {
                int written = inflater.inflate(buffer);
                if (written > 0) {
                    output.write(buffer, 0, written);
                    continue;
                }
                if (inflater.needsDictionary() || inflater.needsInput()) {
                    break;
                }
            }
            return output.toByteArray();
        } catch (DataFormatException e) {
            throw new IllegalStateException("Failed to inflate container sync payload", e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read container sync payload", e);
        } finally {
            inflater.end();
        }
    }
}