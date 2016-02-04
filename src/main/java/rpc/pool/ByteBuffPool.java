package rpc.pool;

public class ByteBuffPool {
    private static final int PAGE_SIZE = 1024;
    private static final int MAX_LEVEL = 11;
    private static final int MAX_BUDDY_CHUNK_SIZE = PAGE_SIZE << MAX_LEVEL;
    private static final int MAX_SUBPAGE_SIZE = 512;
    private static final int MIN_SUBPAGE_SIZE = 16;
    private static final int minSupageMask = ~(MIN_SUBPAGE_SIZE - 1);

    private static int DEFAULT_ARENA_SIZE = 4;
    private static int arenaSize;
    private static PoolArena[] heapPoolArenas;
    private static PoolArena[] directPoolArenas;
    private static int heapIdx;
    private static int directIdx;
    private static ThreadLocal<ThreadLocalCache> heapThreadLocalCaches;
    private static ThreadLocal<ThreadLocalCache> directThreadLocalCaches;

    private static final int BYTEBUFF_RECYCLE_SIZE = 200;
    static ObjectRecycler<ByteBuff> heapByteBuffRecycler;
    static ObjectRecycler<ByteBuff> directByteBuffRecycler;

    static {
        int processor = Runtime.getRuntime().availableProcessors();
        arenaSize = Math.min(processor, DEFAULT_ARENA_SIZE);
        heapPoolArenas = new PoolArena[arenaSize];
        for (int i = 0; i < arenaSize; i++) {
            heapPoolArenas[i] = new PoolArena(PAGE_SIZE, MAX_LEVEL, MAX_SUBPAGE_SIZE, MIN_SUBPAGE_SIZE, false);
        }
        heapThreadLocalCaches = new ThreadLocal<ThreadLocalCache>();
        heapByteBuffRecycler = new ObjectRecycler<ByteBuff>(BYTEBUFF_RECYCLE_SIZE,
                new ObjectRecycler.ObjectFactory<ByteBuff>() {
                    public ByteBuff createNewObject() {
                        return new PooledHeapByteBuff();
                    }
                });
        directPoolArenas = new PoolArena[arenaSize];
        for (int i = 0; i < arenaSize; i++) {
            directPoolArenas[i] = new PoolArena(PAGE_SIZE, MAX_LEVEL, MAX_SUBPAGE_SIZE, MIN_SUBPAGE_SIZE, true);
        }
        directThreadLocalCaches = new ThreadLocal<ThreadLocalCache>();
        directByteBuffRecycler = new ObjectRecycler<ByteBuff>(BYTEBUFF_RECYCLE_SIZE,
                new ObjectRecycler.ObjectFactory<ByteBuff>() {
                    public ByteBuff createNewObject() {
                        return new PooledDirectByteBuff();
                    }
                });
    }

    static ThreadLocalCache obtainThreadLocalCache(boolean isDirect) {
        ThreadLocal<ThreadLocalCache> threadLocalCaches = isDirect ? directThreadLocalCaches : heapThreadLocalCaches;
        ThreadLocalCache cache = threadLocalCaches.get();
        if (cache == null) {
            PoolArena arena = getPoolArenas(isDirect);
            cache = new ThreadLocalCache(arena, MAX_SUBPAGE_SIZE / MIN_SUBPAGE_SIZE, 256, 1 << MAX_LEVEL, 64);
            threadLocalCaches.set(cache);
        }
        return cache;
    }

    private static synchronized PoolArena getPoolArenas(boolean isDirect) {
        int idx = isDirect ? directIdx : heapIdx;
        if (isDirect) {
            directIdx = (directIdx + 1) % arenaSize;
        } else {
            heapIdx = (heapIdx + 1) % arenaSize;
        }
        PoolArena[] poolArenas = isDirect ? directPoolArenas : heapPoolArenas;
        return poolArenas[idx];
    }

    public static ByteBuff allocateHeap(int reqCapacity) {
        return allocate(reqCapacity, false);
    }

    public static ByteBuff allocateDirect(int reqCapacity) {
        return allocate(reqCapacity, true);
    }

    private static ByteBuff allocate(int reqCapacity, boolean isDirect) {
        ByteBuff buff = getByteBuff(isDirect);
        int normalCapacity = normalizeCapacity(reqCapacity);
        ThreadLocalCache cache = obtainThreadLocalCache(isDirect);
        if (cache.allocateFromCache(buff, reqCapacity, normalCapacity)) {
            return buff;
        }
        PoolArena poolArena = cache.poolArena;
        poolArena.allocate(buff, reqCapacity, normalCapacity);
        return buff;
    }

    public static void free(ByteBuff buff) {
        int normalCapacity = normalizeCapacity(buff.capacity);
        if (buff.isInitThread(Thread.currentThread())) { // free to cache
            ThreadLocalCache cache = obtainThreadLocalCache(buff.isDirect());
            cache.freeToCache(buff);
        } else {                                        // free to chunk
            if (buff.poolChunk != null && buff.capacity <= MAX_BUDDY_CHUNK_SIZE) {
                PoolChunk chunk = buff.poolChunk;
                chunk.free(buff.handle, normalCapacity);
            } else { // free huge memory
                ; //do nothing, wait for gc.
            }
        }
        heapByteBuffRecycler.recycle(buff);
    }

    /**
     * get an byteBuff object, which has not initialized.
     *
     * @return
     */
    private static ByteBuff getByteBuff(boolean isDirect) {
        if (isDirect) {
            return directByteBuffRecycler.get();
        } else {
            return heapByteBuffRecycler.get();
        }
    }

    static int normalizeCapacity(int capacity) {
        if (capacity <= 0) {
            throw new RuntimeException("size too small");
        }
        if (capacity > ((Integer.MAX_VALUE >> 1) + 1)) {
            throw new RuntimeException("size too big");
        }
        if (capacity > MAX_SUBPAGE_SIZE) {
            if ((capacity & (capacity - 1)) == 0) {
                return capacity;
            }
            capacity |= capacity >> 1;
            capacity |= capacity >> 2;
            capacity |= capacity >> 4;
            capacity |= capacity >> 8;
            capacity |= capacity >> 16;
            return capacity + 1;
        }
        return ((capacity - 1) & minSupageMask) + MIN_SUBPAGE_SIZE;
    }
}
