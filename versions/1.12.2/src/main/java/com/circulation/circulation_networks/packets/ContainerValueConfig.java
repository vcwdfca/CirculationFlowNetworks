package com.circulation.circulation_networks.packets;

import com.circulation.circulation_networks.container.CFNBaseContainer;
import com.circulation.circulation_networks.utils.Packet;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ContainerValueConfig implements Packet<ContainerValueConfig> {

    private static final int COMPRESSION_THRESHOLD = 256;
    private static final byte TYPE_STRING = 0;
    private static final byte TYPE_BYTES = 1;

    private short Name;
    private byte Type = TYPE_STRING;
    private String StringValue;
    private byte[] BytesValue;

    public ContainerValueConfig() {
    }

    public ContainerValueConfig(short name, String value) {
        this.Name = name;
        this.Type = TYPE_STRING;
        this.StringValue = value;
    }

    public ContainerValueConfig(short name, byte[] value) {
        this.Name = name;
        this.Type = TYPE_BYTES;
        this.BytesValue = value != null ? value.clone() : new byte[0];
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

    @Override
    public void fromBytes(ByteBuf buf) {
        this.Name = buf.readShort();
        this.Type = buf.readByte();
        boolean compressed = buf.readBoolean();
        int length = buf.readInt();
        byte[] payload = new byte[length];
        buf.readBytes(payload);
        byte[] decoded = compressed ? inflate(payload) : payload;
        if (Type == TYPE_BYTES) {
            this.BytesValue = decoded;
            this.StringValue = null;
        } else {
            this.StringValue = new String(decoded, StandardCharsets.UTF_8);
            this.BytesValue = null;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeShort(Name);
        buf.writeByte(Type);
        byte[] raw = Type == TYPE_BYTES
            ? (this.BytesValue != null ? this.BytesValue : new byte[0])
            : (this.StringValue != null ? this.StringValue.getBytes(StandardCharsets.UTF_8) : new byte[0]);
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

    @Override
    public IMessage onMessage(ContainerValueConfig message, MessageContext ctx) {
        var c = Minecraft.getMinecraft().player.openContainer;
        if (c instanceof CFNBaseContainer cc) {
            if (message.Type == TYPE_BYTES) {
                cc.bytesSync(message.Name, message.BytesValue);
            } else {
                cc.stringSync(message.Name, message.StringValue);
            }
        }
        return null;
    }
}
