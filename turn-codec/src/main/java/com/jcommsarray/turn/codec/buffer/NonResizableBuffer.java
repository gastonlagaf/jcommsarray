package com.jcommsarray.turn.codec.buffer;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class NonResizableBuffer {

    private final List<byte[]> buffer = new LinkedList<>();

    private int size = 0;

    public synchronized NonResizableBuffer write(byte[] data) {
        buffer.add(data);
        size += data.length;
        return this;
    }

    public synchronized NonResizableBuffer pad(int count) {
        byte[] bytes = new byte[count];
        write(bytes);
        return this;
    }

    public byte[] toByteArray() {
        byte[] result = new byte[size];

        int position = 0;
        for (byte[] chunk: buffer) {
            System.arraycopy(chunk, 0, result, position, chunk.length);
            position += chunk.length;
        }

        return result;
    }

    public ByteBuffer toByteBuffer() {
        ByteBuffer result = ByteBuffer.allocate(size);

        for (byte[] chunk: buffer) {
            result.put(chunk);
        }

        return result;
    }

    public int size() {
        return size;
    }

}
