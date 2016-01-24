package rpc.pool;

public class BuddyChunkList {
    BuddyChunkList nextList;
    BuddyChunkList preList;
    private final int minUseRate;
    private final int maxUseRate;

    private BuddyChunk head;

    public BuddyChunkList(int minUseRate, int maxUseRate) {
        this.minUseRate = minUseRate;
        this.maxUseRate = maxUseRate;
    }

    public boolean allocate(ByteBuff buff, int normalCapacity) {
        if (head == null) {
            return false;
        }
        for (BuddyChunk cur = head; cur != null; ) {
            int handle = cur.allocate(normalCapacity);
            if (handle >= 0) {
                buff.init(cur, handle, normalCapacity);
                checkUsageAndMove(cur);
                return true;
            } else {
                cur = cur.nextChunk;
            }
        }
        return false;
    }

    private void checkUsageAndMove(BuddyChunk chunk) {
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
            head.preChunk = null;
        } else {
            BuddyChunk pre = chunk.preChunk;
            pre.nextChunk = chunk.nextChunk;
            if (chunk.nextChunk != null) {
                chunk.nextChunk.preChunk = pre;
            }
        }
        chunk.preChunk = null;
        chunk.nextChunk = null;
    }

    void addChunk(BuddyChunk chunk) {
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
    }
}
