package rpc.pool;

import java.nio.ByteBuffer;

public class ByteBuffPool {
    private static final int DEFAULT_MAX_LEVEL = 4;
    private static final int DEFAULT_PAGE_LEVEL = 1;
    private int pageSize;
    private int maxLevel;
    private int chunkSize;
    private byte[] data;

    private BuddyAllocator allocator;

    public ByteBuffPool() {
        pageSize = DEFAULT_PAGE_LEVEL;
        maxLevel = DEFAULT_MAX_LEVEL;
        chunkSize = pageSize << maxLevel;
        data = new byte[chunkSize];
        allocator = new BuddyAllocator(maxLevel);
    }

    public ByteBuff allocate(int capacity) {
        int offset = allocator.alloc(capacity);
        ByteBuffer buf = ByteBuffer.wrap(data, offset, capacity);
        ByteBuff wrapper = new ByteBuff(buf, offset, capacity);
        return wrapper;
    }

    public void free(ByteBuff buf) {
        int offset = buf.offset;
        int capacity = buf.capacity;
        allocator.free(offset, capacity);
    }
}
