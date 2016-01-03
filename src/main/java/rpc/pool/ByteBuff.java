package rpc.pool;

import java.nio.*;

public class ByteBuff {
    public final ByteBuffer delegator;
    final int offset;
    final int capacity;

    ByteBuff(ByteBuffer delegator, int offset, int capacity) {
        this.delegator = delegator;
        this.offset = offset;
        this.capacity = capacity;
    }
}
