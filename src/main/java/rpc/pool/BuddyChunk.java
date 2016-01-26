package rpc.pool;

import static rpc.pool.PoolUtil.power2Level;

public class BuddyChunk implements PoolChunk {
    SubpagePool subpagePool;
    private final int pageSize;
    private final int pageSizeLevel;
    private final int maxLevel;
    private final int chunkSize;
    private final int totalPageNum;
    private int usedPages;

    BuddyChunk nextChunk;
    BuddyChunk preChunk;

    final byte[] memory;
    final BuddyPageAllocator pageAllocator;

    public BuddyChunk(SubpagePool subpagePool, int pageSize, int maxLevel) {
        this.subpagePool = subpagePool;
        this.pageSize = pageSize;
        this.pageSizeLevel = power2Level(pageSize);
        this.maxLevel = maxLevel;
        this.chunkSize = pageSize << maxLevel;

        this.totalPageNum = 1 << maxLevel;

        this.memory = new byte[chunkSize];
        pageAllocator = new BuddyPageAllocator(maxLevel);
    }

    int allocateOnePage() {
        int pageOffset = pageAllocator.obtainIdelPagePosition(1);
        return pageOffset << pageSizeLevel;
    }

    @Override
    public void free(int handle, int capacity) {
        int pageOffset = handle >> pageSizeLevel;
        int page = capacity >> pageSizeLevel;
        pageAllocator.free(pageOffset, page);
    }

    @Override
    public byte[] getMemory() {
        return this.memory;
    }

    @Override
    public int allocate(int normCapacity) {
        int page = normCapacity >> pageSizeLevel;
        int pageOffset = pageAllocator.obtainIdelPagePosition(page);
        if (pageOffset < 0) { // allocate failure
            return -1;
        }
        usedPages += page;
        int handle = pageOffset << pageSizeLevel;
        return handle;
    }

    public int usage() {
        return 100 * usedPages / totalPageNum;
    }
}
