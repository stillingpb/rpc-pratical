package rpc.pool;

/**
 * manage a page of memory with slab alogrithm.
 */
public class SlabChunk implements PoolChunk<Object> {
    private SubpagePool subpagePool;
    boolean inSubpagePool;
    private BuddyChunk buddyChunk;
    private int baseOffset;
    private int pageSize;

    int elemCapacity;  // eleCapacity可能不是2的n次方.
    private int eleAmount; // the amount of element.
    private int usedElemNum;
    private SlabSubpageAllocator slabAllocator;

    public SlabChunk(SubpagePool subpagePool, BuddyChunk buddy, int baseOffset, int pageSize, int elemCapacity) {
        this.subpagePool = subpagePool;
        this.buddyChunk = buddy;
        this.baseOffset = baseOffset;
        this.elemCapacity = elemCapacity;

        this.pageSize = pageSize;
        this.eleAmount = pageSize / elemCapacity;
        slabAllocator = new SlabSubpageAllocator(eleAmount);

        subpagePool.addToPool(this);
    }

    @Override
    public synchronized int allocate(int capacity) {
        assert capacity == elemCapacity;
        int offset = slabAllocator.obtainIdelPosition();
        if (offset < 0) {
            return -1;
        }
        int handle = baseOffset + (offset * elemCapacity);
        if (++usedElemNum == eleAmount) {
            subpagePool.removeFromPool(this);
        }
        return handle;
    }

    @Override
    public synchronized void free(int handle, int normalCapacity) {
        assert elemCapacity == normalCapacity;
        int offset = (handle - baseOffset) / elemCapacity;
        if (slabAllocator.free(offset)) {
            usedElemNum--;
        }
        checkIfFree2BuddyChunk();
    }

    private void checkIfFree2BuddyChunk() {
        // if slabChunk is not in subpagePool, free slabChunk to buddyChunk.
        if (!inSubpagePool) {
            buddyChunk.free(baseOffset, pageSize);
        }
    }

    public SlabSubpageAllocator getSlabAllocator() {
        return slabAllocator;
    }

    @Override
    public Object getMemory() {
        return buddyChunk.getMemory();
    }
}
