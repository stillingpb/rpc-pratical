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
    private static PoolArena[] poolArenas;
    private static int idx;
    private static ThreadLocal<ThreadLocalCache> threadLocalCaches;

    private static final int BYTEBUFF_RECYCLE_SIZE = 200;
    static ObjectRecycler<ByteBuff> byteBuffRecycler;

    static {
        int processor = Runtime.getRuntime().availableProcessors();
        arenaSize = Math.min(processor, DEFAULT_ARENA_SIZE);
        poolArenas = new PoolArena[arenaSize];
        for (int i = 0; i < arenaSize; i++) {
            poolArenas[i] = new PoolArena(PAGE_SIZE, MAX_LEVEL, MAX_SUBPAGE_SIZE, MIN_SUBPAGE_SIZE);
        }
        threadLocalCaches = new ThreadLocal<ThreadLocalCache>();

        ObjectRecycler.ObjectFactory<ByteBuff> byteBuffFactory = new ObjectRecycler.ObjectFactory<ByteBuff>() {
            @Override
            public ByteBuff createNewObject() {
                return new ByteBuff();
            }

            @Override
            public void freeObject(ByteBuff buff) {
                buff = null; // set gc free
            }
        };
        byteBuffRecycler = new ObjectRecycler<ByteBuff>(BYTEBUFF_RECYCLE_SIZE, byteBuffFactory);
    }

    static ThreadLocalCache obtainThreadLocalCache() {
        ThreadLocalCache cache = threadLocalCaches.get();
        if (cache == null) {
            synchronized (ThreadLocalCache.class) {
                idx = (idx + 1) % arenaSize;
                PoolArena arena = poolArenas[idx];
                cache = new ThreadLocalCache(arena, MAX_SUBPAGE_SIZE / MIN_SUBPAGE_SIZE, 256, 1 << MAX_LEVEL, 64);
                threadLocalCaches.set(cache);
            }
        }
        return cache;
    }

    public static ByteBuff allocate(int reqCapacity) {
        ByteBuff buff = getByteBuff();
        int normalCapacity = normalizeCapacity(reqCapacity);
        ThreadLocalCache cache = obtainThreadLocalCache();
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
            ThreadLocalCache cache = obtainThreadLocalCache();
            cache.freeToCache(buff);
        } else {                                        // free to chunk
            if (buff.capacity <= MAX_BUDDY_CHUNK_SIZE) {
                PoolChunk chunk = buff.poolChunk;
                chunk.free(buff.handle, normalCapacity);
            } else { // free huge memory
                ; //do nothing, wait for gc.
            }
        }
        byteBuffRecycler.recycle(buff);
    }

    /**
     * get an byteBuff object, which has not initialized.
     *
     * @return
     */
    private static ByteBuff getByteBuff() {
        return byteBuffRecycler.get();
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
