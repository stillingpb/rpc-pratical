package rpc.pool;

import java.nio.*;

public abstract class ByteBuff<T> {
    int handle;
    int capacity;
    private ByteBuffer delegator;
    PoolChunk poolChunk;
    Thread initThread;

    protected void init(PoolChunk poolChunk, T memory, int handle, int capacity) {
        this.poolChunk = poolChunk;
        this.handle = handle;
        this.capacity = capacity;
        this.delegator = newByteBuffer(memory, handle, capacity);
        this.initThread = Thread.currentThread();
    }

    protected abstract ByteBuffer newByteBuffer(T memory, int handle, int capacity);
    protected abstract boolean isDirect();

    public void free() {
        ByteBuffPool.free(this); // TODO
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
