package rpc.pool;

public class ByteBuffPool {
    static final int PAGE_SIZE = 1024;

    private SlabChunk slabChunk;
    private BuddyChunk buddyChunk;

    public ByteBuffPool() {
    }

    public ByteBuff allocate(int reqCapacity) {
        ByteBuff buff = null;
        int normalCapacity = normalizeCapacity(reqCapacity);
        if (normalCapacity < PAGE_SIZE) {
            buff = allocateInArena();
        }
        if (buff == null) {
            buff = allocateInChunk(normalCapacity);
        }
        return buff;
    }

    private ByteBuff allocateInChunk(int normalCapacity) {
        return buddyChunk.allocate(normalCapacity);
    }

    private ByteBuff allocateInArena() {
        return null;
    }

    int normalizeCapacity(int capacity) {
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
