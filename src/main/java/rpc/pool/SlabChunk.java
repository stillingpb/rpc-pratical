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
    int elemCapacity;
    private int elemCapacityLevel;
    private int maxElemNum;
    private int usedElemNum;
    private SlabSubpageAllocator slabAllocator;

    public SlabChunk(SubpagePool subpagePool, BuddyChunk buddy, int pageSize, int elemCapacity) {
        this.subpagePool = subpagePool;
        this.buddyChunk = buddy;
        this.memory = buddy.memory;
        this.baseOffset = buddy.allocateOnePage();
        this.elemCapacity = elemCapacity;
        this.elemCapacityLevel = power2Level(elemCapacity);

        this.maxElemNum = pageSize >> elemCapacityLevel;
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
    public void free(int handle, int normalCapacity) {
        assert elemCapacity == normalCapacity;
        int offset = (handle - baseOffset) >> elemCapacityLevel;
        if (slabAllocator.free(offset)) {
            usedElemNum--;
        }
        checkIfFree2BuddyChunk();
    }

    private void checkIfFree2BuddyChunk() {
        // TODO
    }

    public int getBaseOffset() {
        return baseOffset;
    }

    public SlabSubpageAllocator getSlabAllocator() {
        return slabAllocator;
    }

    @Override
    public byte[] getMemory() {
        return memory;
    }
}
