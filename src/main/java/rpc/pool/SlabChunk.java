package rpc.pool;

/**
 * manage a page of memory with slab alogrithm.
 */
public class SlabChunk implements PoolChunk {
    private SubpagePool subpagePool;
    private BuddyChunk buddyChunk;
    private byte[] memory;
    private int beginOffset;
    private int pageSize;
    int elemCapacity;
    private int maxElemNum;
    private int usedElemNum;
    private SlabSubpageAllocator slabAllocator;

    public SlabChunk(BuddyChunk buddy, int pageSize, int elemCapacity) {
        this.buddyChunk = buddy;
        this.subpagePool = buddy.subpagePool;
        this.memory = buddy.memory;
        this.beginOffset = buddy.allocateSlabPage();
        this.pageSize = pageSize;
        this.elemCapacity = elemCapacity;

        this.maxElemNum = pageSize / elemCapacity;
        this.usedElemNum = 0;
        slabAllocator = new SlabSubpageAllocator(maxElemNum);

        subpagePool.addToPool(this);
    }

    @Override
    public int allocate(int capacity) {
        assert capacity == elemCapacity;
        int pos = slabAllocator.obtainIdelPosition();
        if (pos < 0) {
            return -1;
        }
        int handle = pos * elemCapacity;
        if (++usedElemNum == maxElemNum) {
            subpagePool.removeFromPool(this);
        }
        return handle;
    }

    @Override
    public void free(int handle, int capacity) { // TODO
        assert elemCapacity == capacity;
        int pos = handle / elemCapacity;
        slabAllocator.free(pos);
        if (--usedElemNum == 0) {
            buddyChunk.free(beginOffset, pageSize); // TODO
        }
    }

    @Override
    public byte[] getMemory() {
        return memory;
    }
}
