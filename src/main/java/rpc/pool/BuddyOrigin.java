package rpc.pool;

import java.nio.ByteBuffer;

public class BuddyOrigin implements PoolOrigin {
    private final int pageSize;
    private final int maxLevel;
    private final int chunkSize;

    final byte[] memory;
    final BuddyAllocator pageAllocator;

    public BuddyOrigin(int pageSize, int maxLevel) {
        this.pageSize = pageSize;
        this.maxLevel = maxLevel;
        this.chunkSize = pageSize << maxLevel;
        this.memory = new byte[chunkSize];
        pageAllocator = new BuddyAllocator(maxLevel);
    }


    int allocateSlabPage() {
        return pageAllocator.obtainIdelPagePosition(1);
    }

    @Override
    public void free(ByteBuff buf) {
        int offset = buf.offset;
        int capacity = buf.capacity;
        pageAllocator.free(offset, capacity);
    }

    @Override
    public ByteBuff allocate(int capacity) {
        int page = capacity2Page(capacity);
        int pos = pageAllocator.obtainIdelPagePosition(page);
        int offset = pos * pageSize;
        ByteBuffer buf = ByteBuffer.wrap(memory, offset, capacity);
        ByteBuff wrapper = new ByteBuff(buf, this, offset, capacity);
        return wrapper;
    }

    int capacity2Page(int capacity) {
        return capacity / pageSize;
    }
}
