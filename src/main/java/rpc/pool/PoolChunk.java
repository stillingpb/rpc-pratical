package rpc.pool;

public interface PoolChunk {
    public int allocate(int capacity);
    public void free(int handle, int capacity);
    public byte[] getMemory();
}
