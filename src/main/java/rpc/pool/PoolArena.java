package rpc.pool;

public class PoolArena {
    private int pageSize;
    private int maxLevel;
    private int chunkSize;

    private BuddyChunkList q050;
    private BuddyChunkList q025;
    private BuddyChunkList qInit;
    private BuddyChunkList q075;

    private SubpagePool subpagePool;
    private int maxSubpageSize;

    public PoolArena(int pageSize, int maxLevel, int maxSubpageSize, int minSubpageSize) {
        this.pageSize = pageSize;
        this.maxLevel = maxLevel;
        this.chunkSize = pageSize << maxLevel;

        qInit = new BuddyChunkList(this, -1, 40);
        q025 = new BuddyChunkList(this, 25, 65);
        q050 = new BuddyChunkList(this, 50, 95);
        q075 = new BuddyChunkList(this, 75, 101);
        link(qInit, q025);
        link(q025, q050);
        link(q050, q075);

        this.maxSubpageSize = maxSubpageSize;
        this.subpagePool = new SubpagePool(maxSubpageSize, minSubpageSize);
    }

    private void link(BuddyChunkList pre, BuddyChunkList next) {
        if (pre == null || next == null) {
            return;
        }
        pre.nextList = next;
        next.preList = pre;
    }

    public synchronized void allocate(ByteBuff buff, int reqCapacity, int normalCapacity) {
        if (normalCapacity < chunkSize) {
            if (isTinyCapacity(normalCapacity) &&
                    allocateFromSubpagePool(buff, reqCapacity, normalCapacity)) {
                return;
            }
            allocateNormal(buff, reqCapacity, normalCapacity);
        } else {
            allocateHuge(buff, reqCapacity, normalCapacity);
        }
    }

    private void allocateHuge(ByteBuff buff, int reqCapacity, int normCapacity) {
        byte[] data = new byte[reqCapacity];
        buff.init(data, 0, reqCapacity);
    }

    private void allocateNormal(ByteBuff buff, int reqCapacity, int normCapacity) {
        if (q050.allocate(buff, reqCapacity, normCapacity) || q025.allocate(buff, reqCapacity, normCapacity)
                || qInit.allocate(buff, reqCapacity, normCapacity) || q075.allocate(buff, reqCapacity, normCapacity)) {
            return;
        }
        BuddyChunk buddyChunk = new BuddyChunk(pageSize, maxLevel);
        allocateFromChunk(buddyChunk, buff, reqCapacity, normCapacity);
        qInit.addChunk(buddyChunk);
    }

    /**
     * allocate several pages of memory from buddy chunk, or slab chunk.
     *
     * @return allocate result
     */
    boolean allocateFromChunk(BuddyChunk buddyChunk, ByteBuff buff, int reqCapacity, int normCapacity) {
        if (normCapacity <= maxSubpageSize) {
            int baseOffset = buddyChunk.allocateOnePage();
            if (baseOffset < 0) {
                return false;
            }
            SlabChunk slabChunk = new SlabChunk(subpagePool, buddyChunk, baseOffset, pageSize, normCapacity);
            int slabHandle = slabChunk.allocate(normCapacity);
            if (slabHandle < 0) {
                return false;
            }
            buff.init(slabChunk, slabHandle, reqCapacity);
        } else {
            int buddyHandle = buddyChunk.allocate(normCapacity);
            if (buddyHandle < 0) {
                return false;
            }
            buff.init(buddyChunk, buddyHandle, reqCapacity);
        }
        return true;
    }

    /**
     * allocate tiny memory from subpage pool.
     *
     * @return allocate if success.
     */
    private boolean allocateFromSubpagePool(ByteBuff buff, int reqCapacity, int normalCapacity) {
        return subpagePool.allocate(buff, reqCapacity, normalCapacity);
    }

    void free(ByteBuff buff, int normalCapacity) {
        if (buff.capacity <= chunkSize) {
            PoolChunk chunk = buff.poolChunk;
            chunk.free(buff.handle, normalCapacity);
        } else { // free huge memory
            ; //do nothing, wait for gc.
        }
    }

    boolean isTinyCapacity(int capacity) {
        return capacity <= maxSubpageSize;
    }

    boolean isHugeCapacity(int capacity) {
        return capacity > chunkSize;
    }

    int getBuddyChunkIdx(int capacity) {
        return (capacity - 1) / pageSize;
    }

    int getSlabChunkIdx(int capacity) {
        return subpagePool.getSlabChunkIdx(capacity);
    }
}
