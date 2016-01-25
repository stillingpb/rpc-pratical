package rpc.pool;

public class PoolArea {
    private int pageSize;
    private int maxLevel;
    private int chunkSize;

    private BuddyChunkList q050;
    private BuddyChunkList q025;
    private BuddyChunkList qInit;
    private BuddyChunkList q075;

    private SubpagePool subpagePool;
    private final int maxSubpageSize;

    public PoolArea(int pageSize, int maxLevel) {
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

        this.maxSubpageSize = pageSize >> 1;
        subpagePool = new SubpagePool(maxSubpageSize, 4);
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
                    allocateTinyFromSubpagePool(buff, reqCapacity, normalCapacity)) {
                return;
            }
            allocateNormal(buff, reqCapacity, normalCapacity);
        } else {
            allocateHuge(buff, reqCapacity, normalCapacity);
        }
    }

    /**
     * allocate from subpage pool.
     *
     * @return allocate if success.
     */
    private boolean allocateTinyFromSubpagePool(ByteBuff buff, int reqCapacity, int normalCapacity) {
        return subpagePool.allocate(buff, reqCapacity, normalCapacity);
    }

    private void allocateHuge(ByteBuff buff, int reqCapacity, int normalCapacity) {
        byte[] data = new byte[reqCapacity]; // TODO
        buff.init(data, reqCapacity, normalCapacity);
    }

    private boolean isTinyCapacity(int capacity) {
        return capacity <= 512; // TODO
    }

    private void allocateNormal(ByteBuff buff, int reqCapacity, int normCapacity) {
        if (q050.allocate(buff, reqCapacity, normCapacity) || q025.allocate(buff, reqCapacity, normCapacity)
                || qInit.allocate(buff, reqCapacity, normCapacity) || q075.allocate(buff, reqCapacity, normCapacity)) {
            return;
        }
        BuddyChunk buddyChunk = new BuddyChunk(subpagePool, pageSize, maxLevel);
        allocateFromChunk(buddyChunk, buff, reqCapacity, normCapacity);
        qInit.addChunk(buddyChunk);
    }

    /**
     * allocate memory from buddy chunk, or slab chunk.
     *
     * @return if allocate success
     */
    boolean allocateFromChunk(BuddyChunk buddyChunk, ByteBuff buff, int reqCapacity, int normCapacity) {
        int buddyHandle = buddyChunk.allocate(normCapacity);
        if (buddyHandle < 0) {
            return false;
        }
        if (normCapacity <= maxSubpageSize) {
            SlabChunk slabChunk = new SlabChunk(buddyChunk, pageSize, normCapacity);
            int slabHandle = slabChunk.allocate(normCapacity);
            if (slabHandle < 0) {
                return false;
            }
            buff.init(slabChunk, slabHandle, reqCapacity);
        } else {
            buff.init(buddyChunk, buddyHandle, reqCapacity);
        }
        return true;
    }
}
