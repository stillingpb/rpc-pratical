package rpc.pool;

import java.util.HashSet;
import java.util.Set;

public class SubpagePool {
    SlabChunk slabChunks[];
    Set<SlabChunk> fullSlabAllocators; // TODO

    public SubpagePool() {
        slabChunks = new SlabChunk[32];
        fullSlabAllocators = new HashSet<SlabChunk>();
    }

    public boolean allocate(ByteBuff buff, int reqCapacity, int elemCapacity) {
        int idx = getSlabAllocatorIdx(elemCapacity);
        if (slabChunks[idx] == null) {
            return false;
        }
        int handle = slabChunks[idx].allocate(elemCapacity);
        if (handle < 0) {
            return false;
        }
        buff.init(slabChunks[idx], handle, reqCapacity);
        return true;
    }

    private int getSlabAllocatorIdx(int elemCapacity) {
        if (elemCapacity >= 512) {
            throw new RuntimeException();
        }
        return (elemCapacity - 1) >> 4;
    }

    public void addToPool(SlabChunk chunk) {
        int idx = getSlabAllocatorIdx(chunk.elemCapacity);
        slabChunks[idx] = chunk;
    }

    public void removeFromPool(SlabChunk chunk) {
        int idx = getSlabAllocatorIdx(chunk.elemCapacity);
        slabChunks[idx] = null;
    }
}
