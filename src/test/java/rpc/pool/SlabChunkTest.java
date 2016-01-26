package rpc.pool;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SlabChunkTest {

    @Test
    public void test() {
        int pageSize = 1024;
        int elemCapacity = 64;
        SubpagePool pool = new SubpagePool(pageSize / 2, 4);
        BuddyChunk buddy = new BuddyChunk(pool, pageSize, 2);
        assertEquals(buddy.allocate(pageSize), 0);
        SlabChunk slab = new SlabChunk(buddy, pageSize, elemCapacity);
        int baseOffset = slab.getBaseOffset();
        assertEquals(baseOffset, pageSize * 1);
        for (int i = 0; i < pageSize / elemCapacity; i++) {
            assertEquals(slab.allocate(elemCapacity), baseOffset + i * elemCapacity);
        }

        SlabSubpageAllocator allocator = slab.getSlabAllocator();
        System.out.println(allocator.toString());
        slab.free(baseOffset + 0 * elemCapacity, elemCapacity);
        slab.free(baseOffset + 5 * elemCapacity, elemCapacity);
        slab.free(baseOffset + 15 * elemCapacity, elemCapacity);
        System.out.println(allocator.toString());
    }

}
