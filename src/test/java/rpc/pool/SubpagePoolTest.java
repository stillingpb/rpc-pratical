package rpc.pool;

import org.junit.Test;

public class SubpagePoolTest {

    @Test
    public void test() {
        int pageSize = 128;  // 2 ^ 7
        SubpagePool pool = new SubpagePool(pageSize >> 1, 4); // pool size: 2^4
        BuddyChunk buddy = new BuddyChunk(pageSize, 2);
        for (int i = 0; i < 16; i++) {
            int elemCapacity = 16 * i;
            SlabChunk slab = new SlabChunk(pool, buddy, pageSize, elemCapacity);
        }

        ByteBuff buff = new ByteBuff();
        pool.allocate(buff,16,16);
    }
}
