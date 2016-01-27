package rpc.pool;

import static rpc.pool.PoolUtil.power2Level;

public class BuddyChunk implements PoolChunk {
    private final int pageSize;
    private final int pageSizeLevel;
    private final int maxLevel;
    private final int chunkSize;
    private final int totalPageNum;
    private int usedPages;

    BuddyChunkList chunkList;
    BuddyChunk nextChunk;
    BuddyChunk preChunk;

    final byte[] memory;
    final BuddyPageAllocator pageAllocator;

    public BuddyChunk(int pageSize, int maxLevel) {
        this.pageSize = pageSize;
        this.pageSizeLevel = power2Level(pageSize);
        this.maxLevel = maxLevel;
        this.chunkSize = pageSize << maxLevel;

        this.totalPageNum = 1 << maxLevel;

        this.memory = new byte[chunkSize];
        pageAllocator = new BuddyPageAllocator(maxLevel + 1); // depth equals to level+1
    }

    @Override
    public void free(int handle, int normalCapacity) {
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
    public byte[] getMemory() {
        return this.memory;
    }


    int allocateOnePage() {
        int pageOffset = pageAllocator.obtainIdelPagePosition(1);
        if (pageOffset < 0) {
            return -1;
        }
        usedPages++;
        int handle = pageOffset << pageSizeLevel;
        return handle;
    }

    @Override
    public int allocate(int normCapacity) {
        int page = normCapacity >> pageSizeLevel;
        if (page <= 0) {
            return -1;
        }
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
