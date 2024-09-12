package com.gastonlagaf.stun.codec.util;

import java.nio.ByteBuffer;

public class CodecUtils {

    public static int readShort(ByteBuffer buffer) {
        return buffer.getShort() & 0xFFFF;
    }

    public static void writeShort(ByteBuffer buffer, int value) {
        buffer.putShort((short) (value & 0xffff));
    }

    public static byte[] intToByteArray(int value) {
        return new byte[] {
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) (value)
        };
    }

    public static byte[] shortToByteArray(short value) {
        return new byte[] {
                (byte) (value >> 8),
                (byte) (value)
        };
    }

    public static int byteArrayToInt(byte[] bytes) {
        if (4 != bytes.length) {
            throw new IllegalArgumentException("Byte array length greater than int length");
        }
        return ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
    }

}
