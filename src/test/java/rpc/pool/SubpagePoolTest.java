package rpc.pool;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SubpagePoolTest {

    @Test
    public void test() {
        int pageSize = 128;
        int maxCapacity = 64;
        int minCapacity = 16;
        SubpagePool pool = new SubpagePool(maxCapacity, minCapacity);
        BuddyChunk buddy = new BuddyChunk.BuddyDirectChunk(pageSize, 2);
        SlabChunk[] slabs = pool.slabChunks;
        for (int i = 0; i < maxCapacity / minCapacity; i++) {
            int elemCapacity = minCapacity * (i + 1);
            int baseOffset = buddy.allocateOnePage();
            slabs[i] = new SlabChunk(pool, buddy, baseOffset, pageSize, elemCapacity);
        }

        ByteBuff buff = new PooledDirectByteBuff();
        for (int i = 0; i < maxCapacity / minCapacity; i++) {
            int capacity = minCapacity * (i + 1);
            for (int j = 0; j < pageSize / capacity; j++) {
                boolean rst = pool.allocate(buff, capacity, capacity);
                assertEquals(true, rst);
                if (j < pageSize / capacity - 1) {
                    assertEquals(pool.slabChunks[i], buff.poolChunk);
                } else {
                    assertEquals(null, pool.slabChunks[i]);
                }
            }
        }


    }
}
