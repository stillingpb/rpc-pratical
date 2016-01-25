package rpc.pool;

public class BuddyChunk implements PoolChunk {
    SubpagePool subpagePool;
    private final int pageSize;
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
        this.maxLevel = maxLevel;
        this.chunkSize = pageSize << maxLevel;

        this.totalPageNum = 1 << maxLevel;

        this.memory = new byte[chunkSize];
        pageAllocator = new BuddyPageAllocator(maxLevel);
    }


    int allocateSlabPage() {
        return pageAllocator.obtainIdelPagePosition(1);
    }

    @Override
    public void free(int handle, int capacity) {
        pageAllocator.free(handle, capacity);
    }

    @Override
    public byte[] getMemory() {
        return this.memory;
    }

    @Override
    public int allocate(int normCapacity) {
        int page = capacity2Page(normCapacity);
        int pos = pageAllocator.obtainIdelPagePosition(page);
        if (pos < 0) { // allocate failure
            return -1;
        }
        usedPages += page;
        int handle = pos * pageSize;
        return handle;
    }

    int capacity2Page(int capacity) {
        return capacity / pageSize;
    }

    public int usage() {
        return 100 * usedPages / totalPageNum;
    }
}
