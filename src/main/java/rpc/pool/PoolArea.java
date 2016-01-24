package rpc.pool;

public class PoolArea {
    private int pageSize;
    private int maxLevel;

    private BuddyChunkList q050;
    private BuddyChunkList q025;
    private BuddyChunkList qInit;
    private BuddyChunkList q075;

    private SubpagePool subpagePool;

    public PoolArea(int pageSize, int maxLevel) {
        this.pageSize = pageSize;
        this.maxLevel = maxLevel;

        qInit = new BuddyChunkList(-1, 40);
        q025 = new BuddyChunkList(25, 65);
        q050 = new BuddyChunkList(50, 95);
        q075 = new BuddyChunkList(75, 101);
        link(qInit, q025);
        link(q025, q050);
        link(q050, q075);

        subpagePool = new SubpagePool();
    }

    private void link(BuddyChunkList pre, BuddyChunkList next) {
        if (pre == null || next == null) {
            return;
        }
        pre.nextList = next;
        next.preList = pre;
    }

    public synchronized void allocate(ByteBuff buff, int reqCapacity, int normalCapacity) {
        if (isTinyCapacity(normalCapacity)) {
            if (subpagePool.allocate(buff, reqCapacity, normalCapacity)) {
                return;
            }
        } else {
            allocateNormal(buff, reqCapacity, normalCapacity);
        }
    }

    private boolean isTinyCapacity(int capacity) {
        return capacity <= 512; // TODO
    }

    private void allocateNormal(ByteBuff buff, int reqCapacity, int normalCapacity) {
        if (q050.allocate(buff, normalCapacity) || q025.allocate(buff, normalCapacity)
                || qInit.allocate(buff, normalCapacity) || q075.allocate(buff, normalCapacity)) {
            return;
        }
        BuddyChunk chunk = new BuddyChunk(subpagePool, pageSize, maxLevel);
        int handle = chunk.allocate(normalCapacity); // cannot be failure
        buff.init(chunk, handle, reqCapacity); // TODO set normalCapacity has problem , should set init capacity
        qInit.addChunk(chunk);
    }
}
