package rpc.pool;

import java.util.HashSet;
import java.util.Set;

public class SlabChunk {
    SlabOrigin slabOrigins[];
    Set<SlabOrigin> fullSlabAllocators;

    public SlabChunk() {
        slabOrigins = new SlabOrigin[32];
        fullSlabAllocators = new HashSet<SlabOrigin>();
    }

    public ByteBuff allocate(BuddyOrigin buddy, int elemCapacity) {
        int idx = getSlabAllocatorIdx(elemCapacity);
        if (slabOrigins[idx] == null) {
            slabOrigins[idx] = new SlabOrigin(buddy, ByteBuffPool.PAGE_SIZE, elemCapacity);
        }
        return slabOrigins[idx].allocate(elemCapacity);
    }

    private int getSlabAllocatorIdx(int elemCapacity) {
        if (elemCapacity >= 512) {
            throw new RuntimeException();
        }
        return (elemCapacity - 1) >> 4;
    }
}
