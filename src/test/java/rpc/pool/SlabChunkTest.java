package rpc.pool;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SlabChunkTest {

    @Test
    public void test() {
        SubpagePool pool = new SubpagePool(512, 4);
        BuddyChunk buddy = new BuddyChunk(pool, 1024, 4);
        SlabChunk slab = new SlabChunk(buddy, 1024, 64);
        assertEquals(slab.allocate(64), 0);
    }
}
