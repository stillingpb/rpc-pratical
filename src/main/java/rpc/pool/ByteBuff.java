package rpc.pool;

import java.nio.*;

public class ByteBuff {
    private ByteBuffer delegator;
    int offset;
    int capacity;
    PoolOrigin poolOrigin;

    ByteBuff(ByteBuffer delegator, PoolOrigin poolOrigin, int offset, int capacity) {
        this.delegator = delegator;
        this.poolOrigin = poolOrigin;
        this.offset = offset;
        this.capacity = capacity;
    }

    public ByteBuffer getByteBuffer() {
        return delegator;
    }

    public void free() {
        ; // TODO
    }
}
