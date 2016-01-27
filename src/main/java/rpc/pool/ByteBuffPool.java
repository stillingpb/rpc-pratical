package rpc.pool;

public class ByteBuffPool {
    private static final int PAGE_SIZE = 1024;
    private static final int MAX_LEVEL = 11;
    private static final int MAX_SUBPAGE_SIZE = 512;
    private static final int MIN_SUBPAGE_SIZE = 16;
    private static int minSupageMask = ~(MIN_SUBPAGE_SIZE - 1);

    static PoolArea poolArea;

    static {
        poolArea = new PoolArea(PAGE_SIZE, MAX_LEVEL, MAX_SUBPAGE_SIZE, MIN_SUBPAGE_SIZE);
    }

    public static ByteBuff allocate(int reqCapacity) {
        ByteBuff buff = getByteBuff();
        int normalCapacity = normalizeCapacity(reqCapacity);
        poolArea.allocate(buff, reqCapacity, normalCapacity);
        return buff;
    }

    public static void free(ByteBuff buff) {
        int normalCapacity = normalizeCapacity(buff.capacity);
        poolArea.free(buff, normalCapacity);
    }

    /**
     * get an byteBuff object, which has not initialized
     *
     * @return
     */
    private static ByteBuff getByteBuff() {
        return new ByteBuff();
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
