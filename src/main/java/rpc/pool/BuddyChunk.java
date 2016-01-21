package rpc.pool;

public class BuddyChunk {
    private BuddyOrigin[] chunks;
    private int pageSize;

    public BuddyChunk(int pageSize) {
        this.pageSize = pageSize;
        chunks = new BuddyOrigin[10];
        for (int i = 0; i < chunks.length; i++) {
            chunks[i] = new BuddyOrigin(pageSize, 4);
        }
    }

    public ByteBuff allocate(int normalCapacity) {
        return null;
    }
}
