package rpc.pool;

import static rpc.pool.PoolUtil.power2Level;

/**
 * manage a page of memory with slab alogrithm.
 */
public class SlabChunk implements PoolChunk {
    private SubpagePool subpagePool;
    private BuddyChunk buddyChunk;
    private byte[] memory;
    private int baseOffset;
    private int pageSize;
    int elemCapacity;
    private int elemCapacityLevel;
    private int maxElemNum;
    private int usedElemNum;
    private SlabSubpageAllocator slabAllocator;

    public SlabChunk(BuddyChunk buddy, int pageSize, int elemCapacity) {
        this.buddyChunk = buddy;
        this.subpagePool = buddy.subpagePool;
        this.memory = buddy.memory;
        this.baseOffset = buddy.allocateOnePage();
        this.pageSize = pageSize;
        this.elemCapacity = elemCapacity;
        this.elemCapacityLevel = power2Level(elemCapacity);

        this.maxElemNum = pageSize >> elemCapacityLevel;
        this.usedElemNum = 0;
        slabAllocator = new SlabSubpageAllocator(maxElemNum);

        subpagePool.addToPool(this);
    }

    @Override
    public int allocate(int capacity) {
        assert capacity == elemCapacity;
        int offset = slabAllocator.obtainIdelPosition();
        if (offset < 0) {
            return -1;
        }
        int handle = baseOffset + (offset << elemCapacityLevel);
        if (++usedElemNum == maxElemNum) {
            subpagePool.removeFromPool(this);
        }
        return handle;
    }

    @Override
    public void free(int handle, int capacity) { // TODO
        assert elemCapacity == capacity;
        int offset = (handle - baseOffset) >> elemCapacityLevel;
        slabAllocator.free(offset);
        if (--usedElemNum == 0) {
            buddyChunk.free(baseOffset, pageSize); // TODO
        }
    }

    public int getBaseOffset() {
        return baseOffset;
    }

    public SlabSubpageAllocator getSlabAllocator(){
        return slabAllocator;
    }

    @Override
    public byte[] getMemory() {
        return memory;
    }
}
