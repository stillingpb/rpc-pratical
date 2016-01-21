package rpc.pool;

public interface PoolOrigin {
    public ByteBuff allocate(int capacity);
    public void free(ByteBuff buff);
}
