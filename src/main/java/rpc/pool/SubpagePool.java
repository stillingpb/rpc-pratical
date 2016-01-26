package rpc.pool;

import static rpc.pool.PoolUtil.power2Level;

public class SubpagePool {
    private final int maxCapacity;
    private final int minLevel;
    SlabChunk slabChunks[];

    public SubpagePool(int maxCapacity, int minCapacity) {
        this.maxCapacity = maxCapacity;
        this.minLevel = power2Level(minCapacity);
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

    private int getSlabChunkIdx(int elemCapacity) {
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
    }

    public void removeFromPool(SlabChunk chunk) {
        int idx = getSlabChunkIdx(chunk.elemCapacity);
        if (idx < 0 || idx > slabChunks.length) {
            return;
        }
        slabChunks[idx] = null;
    }
}
