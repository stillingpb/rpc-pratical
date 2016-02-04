package rpc.pool;

import static rpc.pool.PoolUtil.log2;

public class SubpagePool {
    private static int maxCapacity;
    private static int minLevel;
    SlabChunk slabChunks[];

    public SubpagePool(int maxCapacity, int minCapacity) {
        this.maxCapacity = maxCapacity;
        this.minLevel = log2(minCapacity);
        int chunkLen = maxCapacity >> minLevel;
        slabChunks = new SlabChunk[chunkLen];
    }

    public boolean allocate(ByteBuff buff, int reqCapacity, int elemCapacity) {
        int idx = getSlabChunkIdx(elemCapacity);
        if (idx < 0 || idx > slabChunks.length) {
            return false;
        }
        SlabChunk slabChunk = slabChunks[idx];
        if (slabChunk == null) {
            return false;
        }
        int handle = slabChunk.allocate(elemCapacity);
        if (handle < 0) {
            return false;
        }
        buff.init(slabChunk, handle, reqCapacity);
        return true;
    }

    int getSlabChunkIdx(int elemCapacity) {
        if (elemCapacity > maxCapacity) {
            return -1;
        }
        return (elemCapacity - 1) >> minLevel;
    }

    public void addToPool(SlabChunk chunk) {
        int idx = getSlabChunkIdx(chunk.elemCapacity);
        if (idx < 0 || idx > slabChunks.length) {
            return;
        }
        slabChunks[idx] = chunk;
        chunk.inSubpagePool = true;
    }

    public void removeFromPool(SlabChunk chunk) {
        int idx = getSlabChunkIdx(chunk.elemCapacity);
        if (idx < 0 || idx > slabChunks.length) {
            return;
        }
        slabChunks[idx] = null;
        chunk.inSubpagePool = false;
    }
}
