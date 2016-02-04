package rpc.pool;

import java.nio.ByteBuffer;

public class PooledHeapByteBuff extends ByteBuff<byte[]> {
    @Override
    protected ByteBuffer newByteBuffer(byte[] memory, int handle, int capacity) {
        return ByteBuffer.wrap(memory, handle, capacity);
    }

    @Override
    protected boolean isDirect() {
        return false;
    }
}
