package rpc.pool;

public class ThreadLocalCache {
    PoolArena poolArena;

    MemoryRegion[] subPageRegions;
    MemoryRegion[] buddyPageRegions;

    public ThreadLocalCache(PoolArena poolArena, int subPageRegionAmount, int subPageRegionSize,
                            int buddyPageRegionAmount, int buddyPageRegionSize) {
        this.poolArena = poolArena;
        subPageRegions = new MemoryRegion[subPageRegionAmount];
        for (int i = 0; i < subPageRegionAmount; i++) {
            subPageRegions[i] = new MemoryRegion(subPageRegionSize);
        }
        buddyPageRegions = new MemoryRegion[buddyPageRegionAmount];
        for (int i = 0; i < buddyPageRegionAmount; i++) {
            buddyPageRegions[i] = new MemoryRegion(buddyPageRegionSize);
        }
    }


    public boolean allocateFromCache(ByteBuff buff, int reqCapacity, final int normalCapacity) {
        if (poolArena.isHugeCapacity(normalCapacity)) {
            return false;
        }
        MemoryRegion region = getMemoryRegion(normalCapacity);
        Entry entry = region.obtain();
        if (entry == null) {
            return false;
        }
        buff.init(entry.chunk, entry.chunk.getMemory(), entry.handle, reqCapacity);
        return true;
    }

    public boolean freeToCache(ByteBuff buff) {
        int capacity = buff.capacity;
        if (poolArena.isHugeCapacity(capacity)) {
            return false;
        }
        MemoryRegion region = getMemoryRegion(capacity);
        return region.add(buff.poolChunk, buff.handle);
    }

    private MemoryRegion getMemoryRegion(int capacity) {
        MemoryRegion region = null;
        if (poolArena.isTinyCapacity(capacity)) {
            int idx = poolArena.getSlabChunkIdx(capacity);
            region = subPageRegions[idx];
        } else {
            int idx = poolArena.getBuddyChunkIdx(capacity);
            region = buddyPageRegions[idx];
        }
        return region;
    }

    private static class MemoryRegion {
        private int regionSize;
        private Entry[] entries;
        private int head;
        private int tail;
        private int inStorage;

        public MemoryRegion(int regionSize) {
            this.regionSize = regionSize;
            entries = new Entry[regionSize];
            for (int i = 0; i < regionSize; i++) {
                entries[i] = new Entry();
            }
        }

        public boolean add(PoolChunk chunk, int handle) {
            if (inStorage >= regionSize) {
                return false;
            }
            entries[tail].init(chunk, handle);
            tail = nextIdx(tail);
            inStorage++;
            return true;
        }

        /**
         * @return entry. if empty,return null.
         */
        public Entry obtain() {
            if (inStorage <= 0) {
                return null;
            }
            Entry entry = entries[head];
            head = nextIdx(head);
            inStorage--;
            return entry;
        }

        private int nextIdx(int idx) {
            return (idx + 1) % regionSize;
        }
    }

    static class Entry {
        PoolChunk chunk;
        int handle;

        void init(PoolChunk chunk, int handle) {
            this.chunk = chunk;
            this.handle = handle;
        }
    }
}
