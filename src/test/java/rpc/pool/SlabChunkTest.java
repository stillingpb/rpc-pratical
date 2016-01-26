package rpc.pool;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SlabChunkTest {

    @Test
    public void test() {
        int pageSize = 1024;
        int elemCapacity = 64;
        SubpagePool pool = new SubpagePool(pageSize / 2, elemCapacity);
        BuddyChunk buddy = new BuddyChunk(pageSize, 2);
        assertEquals(0, buddy.allocate(pageSize));
        SlabChunk slab = new SlabChunk(pool, buddy, pageSize, elemCapacity);
        int baseOffset = slab.getBaseOffset();
        assertEquals(pageSize * 1, baseOffset);
        for (int i = 0; i < pageSize / elemCapacity; i++) {
            assertEquals(baseOffset + i * elemCapacity, slab.allocate(elemCapacity));
        }

        SlabSubpageAllocator allocator = slab.getSlabAllocator();
        System.out.println(allocator.toString());
        slab.free(baseOffset + 0 * elemCapacity, elemCapacity);
        slab.free(baseOffset + 5 * elemCapacity, elemCapacity);
        slab.free(baseOffset + 15 * elemCapacity, elemCapacity);
        System.out.println(allocator.toString());
    }

}
