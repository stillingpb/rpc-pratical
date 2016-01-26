package rpc.pool;

public class ByteBuffPool {
    static final int PAGE_SIZE = 4096;
    static final int MAX_LEVEL = 11;

    static PoolArea poolArea;

    static {
        poolArea = new PoolArea(PAGE_SIZE, MAX_LEVEL);
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

    private static int normalizeCapacity(int capacity) {
        if (capacity < 0) {
            throw new RuntimeException("size too small");
        }
        if (capacity > ((Integer.MAX_VALUE >> 1) + 1)) {
            throw new RuntimeException("size too big");
        }
        if ((capacity & (capacity - 1)) == 0) {
            return capacity;
        }
        if (capacity < 512) {
            capacity |= capacity >> 1;
            capacity |= capacity >> 2;
            capacity |= capacity >> 4;
            capacity |= capacity >> 8;
            capacity |= capacity >> 16;
            return capacity + 1;
        }
        return (capacity & 0xFFFFFF00) + 16;
    }
}
