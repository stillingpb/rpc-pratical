package rpc.pool;

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
        this.pageSizeLevel = pageSize2Level(pageSize);
        this.maxLevel = maxLevel;
        this.chunkSize = pageSize << maxLevel;

        this.totalPageNum = 1 << maxLevel;

        this.memory = new byte[chunkSize];
        pageAllocator = new BuddyPageAllocator(maxLevel);
    }

    private int pageSize2Level(int pageSize) {
        for (int i = 0; i < 32; i++) {
            if ((pageSize & (1 << i)) != 0) {
                return i;
            }
        }
        return -1;
    }


    int allocateSlabPage() {
        return pageAllocator.obtainIdelPagePosition(1);
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
        int pos = pageAllocator.obtainIdelPagePosition(page);
        if (pos < 0) { // allocate failure
            return -1;
        }
        usedPages += page;
        int handle = pos << pageSizeLevel;
        return handle;
    }

    public int usage() {
        return 100 * usedPages / totalPageNum;
    }
}
