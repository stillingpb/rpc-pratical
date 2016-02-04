package rpc.pool;

import java.nio.ByteBuffer;

import static rpc.pool.PoolUtil.log2;

public abstract class BuddyChunk<T> implements PoolChunk {
    private final int pageSize;
    private final int pageSizeLevel;
    private final int maxLevel;
    private final int chunkSize;
    private final int totalPageNum;
    private int usedPages;

    BuddyChunkList chunkList;
    BuddyChunk nextChunk;
    BuddyChunk preChunk;

    final T memory;
    final BuddyPageAllocator pageAllocator;

    public BuddyChunk(int pageSize, int maxLevel) {
        this.pageSize = pageSize;
        this.pageSizeLevel = log2(pageSize);
        this.maxLevel = maxLevel;
        this.chunkSize = pageSize << maxLevel;

        this.totalPageNum = 1 << maxLevel;

        this.memory = newMemory(chunkSize);
        pageAllocator = new BuddyPageAllocator(maxLevel + 1); // depth equals to level+1
    }

    protected abstract T newMemory(int chunkSize);

    @Override
    public synchronized void free(int handle, int normalCapacity) {
        int pageOffset = handle >> pageSizeLevel;
        int page = normalCapacity >> pageSizeLevel;
        if (pageAllocator.free(pageOffset, page)) { // free success
            usedPages -= page;
        }
        if (chunkList != null) { // update chunkList info
            chunkList.checkUsageAndMove(this);
        }
    }

    @Override
    public T getMemory() {
        return this.memory;
    }


    synchronized int allocateOnePage() {
        int pageOffset = pageAllocator.obtainIdelPagePosition(1);
        if (pageOffset < 0) {
            return -1;
        }
        usedPages++;
        int handle = pageOffset << pageSizeLevel;
        return handle;
    }

    @Override
    public synchronized int allocate(int normCapacity) {
        int page = normCapacity >> pageSizeLevel;
        if (page <= 0) {
            return -1;
        }
        int pageOffset = pageAllocator.obtainIdelPagePosition(page);
        if (pageOffset < 0) { // allocated failure
            return -1;
        }
        usedPages += page;
        int handle = pageOffset << pageSizeLevel;
        return handle;
    }

    public int usage() {
        return 100 * usedPages / totalPageNum;
    }

    static BuddyChunk newHeapChunk(int pageSize, int maxLevel) {
        return new BuddyHeapChunk(pageSize, maxLevel);
    }

    static BuddyChunk newDirectChunk(int pageSize, int maxLevel) {
        return new BuddyDirectChunk(pageSize, maxLevel);
    }

    public static class BuddyHeapChunk extends BuddyChunk<byte[]> {

        public BuddyHeapChunk(int pageSize, int maxLevel) {
            super(pageSize, maxLevel);
        }

        @Override
        protected byte[] newMemory(int chunkSize) {
            return new byte[chunkSize];
        }
    }

    static class BuddyDirectChunk extends BuddyChunk<ByteBuffer> {

        public BuddyDirectChunk(int pageSize, int maxLevel) {
            super(pageSize, maxLevel);
        }

        @Override
        protected ByteBuffer newMemory(int chunkSize) {
            return ByteBuffer.allocateDirect(chunkSize);
        }
    }

}
