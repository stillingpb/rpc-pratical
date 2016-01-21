package rpc.pool;

import java.nio.ByteBuffer;

public class SlabOrigin implements PoolOrigin {
    private byte[] memory;
    private int beginOffset;
    private int length;
    private int elemCapacity;
    private int maxElemNum;
    private int usedElemNum;
    private SlabAllocator slabAllocator;

    public SlabOrigin(BuddyOrigin buddy, int pageSize, int elemCapacity) {
        this.memory = buddy.memory;
        this.beginOffset = buddy.allocateSlabPage();
        this.length = pageSize;
        this.elemCapacity = elemCapacity;

        this.maxElemNum = pageSize / elemCapacity;
        this.usedElemNum = 0;
        slabAllocator = new SlabAllocator(maxElemNum);
    }

    @Override
    public ByteBuff allocate(int capacity) {
        assert capacity == elemCapacity;
        int pos = slabAllocator.obtainIdelPosition();
        int offset = pos * elemCapacity;
        usedElemNum++;
        ByteBuffer buf = ByteBuffer.wrap(memory, offset, elemCapacity);
        ByteBuff wrapper = new ByteBuff(buf, this, offset, elemCapacity);
        return wrapper;
    }

    @Override
    public void free(ByteBuff buf) {
        assert elemCapacity == buf.capacity;
        int offset = buf.offset;
        int pos = offset / elemCapacity;
        slabAllocator.free(pos);
        usedElemNum--;
    }
}
