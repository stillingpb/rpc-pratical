package rpc.pool;

import sun.nio.ch.DirectBuffer;

import java.nio.ByteBuffer;

public class PooledDirectByteBuff extends ByteBuff<ByteBuffer> {
    @Override
    protected ByteBuffer newByteBuffer(ByteBuffer memory, int handle, int capacity) {
        assert memory instanceof DirectBuffer;
        ByteBuffer buff = memory.duplicate();
        buff.clear().position(handle).limit(handle + capacity);
        return buff;
    }

    @Override
    protected boolean isDirect() {
        return true;
    }
}