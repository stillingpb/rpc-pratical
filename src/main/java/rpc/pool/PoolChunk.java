package rpc.pool;

public interface PoolChunk<T> {

    /**
     * 分配指定大小的一块空间.
     *
     * @param capacity 待分配的空间.
     * @return 如果分配成功，返回分配的起始偏移位置；如果失败，返回-1.
     */
    public int allocate(int capacity);

    /**
     * 释放已分配的资源，此方法的实现需要保证同步，最好使用synchronized修饰.
     */
    public void free(int handle, int normalCapacity);

    public T getMemory();
}
