package rpc.pool;

public class BuddyChunkList {
    private PoolArena poolArena;
    BuddyChunkList nextList;
    BuddyChunkList preList;
    private final int minUseRate;
    private final int maxUseRate;

    BuddyChunk head;

    private int chunkNum;

    public BuddyChunkList(PoolArena poolArena, int minUseRate, int maxUseRate) {
        this.poolArena = poolArena;
        this.minUseRate = minUseRate;
        this.maxUseRate = maxUseRate;
    }

    public boolean allocate(ByteBuff buff, int reqCapacity, int normCapacity) {
        if (head == null) {
            return false;
        }
        for (BuddyChunk cur = head; cur != null; ) {
            boolean rst = poolArena.allocateFromChunk(cur, buff, reqCapacity, normCapacity);
            if (rst) {
                checkUsageAndMove(cur);
                return true;
            } else {
                cur = cur.nextChunk;
            }
        }
        return false;
    }

    /**
     * check chunk usage, and move chunk between chunkList.
     *
     * @param chunk buddyChunk
     */
    void checkUsageAndMove(BuddyChunk chunk) {
        int usage = chunk.usage();
        if (usage >= maxUseRate) {
            this.removeChunk(chunk);
            nextList.addChunk(chunk);
            return;
        }
        if (usage < minUseRate) {
            this.removeChunk(chunk);
            preList.addChunk(chunk);
        }
    }

    void removeChunk(BuddyChunk chunk) {
        if (chunk == head) {
            head = chunk.nextChunk;
        } else {
            BuddyChunk pre = chunk.preChunk;
            pre.nextChunk = chunk.nextChunk;
            if (chunk.nextChunk != null) {
                chunk.nextChunk.preChunk = pre;
            }
        }
        chunk.preChunk = null;
        chunk.nextChunk = null;
        chunk.chunkList = null;
        chunkNum--;
    }

    void addChunk(BuddyChunk chunk) {
        chunk.chunkList = this;
        if (head == null) {
            head = chunk;
        } else {
            chunk.nextChunk = head.nextChunk;
            head.nextChunk = chunk;
            chunk.preChunk = head;
            if (chunk.nextChunk != null) {
                chunk.nextChunk.preChunk = chunk;
            }
        }
        chunkNum++;
    }

    int getChunkNum() {
        return chunkNum;
    }
}
