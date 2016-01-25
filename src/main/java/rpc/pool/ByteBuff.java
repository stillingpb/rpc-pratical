package rpc.pool;

import java.nio.*;

public class ByteBuff {
    int offset;
    int capacity;
    PoolChunk poolChunk;
    private ByteBuffer delegator;

    public void init(PoolChunk poolChunk, int offset, int capacity) {
        this.poolChunk = poolChunk;
        init(poolChunk.getMemory(), offset, capacity);
    }

    public void init(byte[] memory, int offset, int capacity) {
        this.offset = offset;
        this.capacity = capacity;
        this.delegator = ByteBuffer.wrap(memory, offset, capacity);
    }


    public ByteBuffer getByteBuffer() {
        return delegator;
    }

    public void free() {
        ; // TODO
    }
}
