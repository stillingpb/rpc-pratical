package rpc.pool;

public class SubpagePool {
    private final int maxSubpageSize;
    private final int subpageLevel;
    SlabChunk slabChunks[];

    public SubpagePool(int maxSubpageSize, int subpageLevel) {
        this.maxSubpageSize = maxSubpageSize;
        this.subpageLevel = subpageLevel;
        int chunkLen = maxSubpageSize >> subpageLevel;
        slabChunks = new SlabChunk[chunkLen];
    }

    public boolean allocate(ByteBuff buff, int reqCapacity, int elemCapacity) {
        int idx = getSlabChunkIdx(elemCapacity);
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
        assert elemCapacity <= maxSubpageSize;
        return (elemCapacity - 1) >> subpageLevel;
    }

    public void addToPool(SlabChunk chunk) {
        int idx = getSlabChunkIdx(chunk.elemCapacity);
        slabChunks[idx] = chunk;
    }

    public void removeFromPool(SlabChunk chunk) {
        int idx = getSlabChunkIdx(chunk.elemCapacity);
        slabChunks[idx] = null;
    }
}
