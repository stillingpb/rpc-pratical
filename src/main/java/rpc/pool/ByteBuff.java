package rpc.pool;

import java.nio.*;

public class ByteBuff {
    int handle;
    int capacity;
    private ByteBuffer delegator;
    PoolChunk poolChunk;
    Thread initThread;

    public void init(PoolChunk poolChunk, int handle, int capacity) {
        this.poolChunk = poolChunk;
        init(poolChunk.getMemory(), handle, capacity);
    }

    public void init(byte[] memory, int handle, int capacity) {
        this.handle = handle;
        this.capacity = capacity;
        this.delegator = ByteBuffer.wrap(memory, handle, capacity);
        this.initThread = Thread.currentThread();
    }

    public void free() {
        ByteBuffPool.free(this);
        this.poolChunk = null;
        this.handle = 0;
        this.capacity = 0;
        this.delegator = null;
    }

    public ByteBuffer getByteBuffer() {
        return delegator;
    }

    public int getOffset() {
        return handle;
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean isInitThread(Thread curThread) {
        return this.initThread == curThread;
    }
}
