package rpc.pool;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BuddyChunkTest {
    @Test
    public void test() {
        int pageSize = 512;
        SubpagePool pool = new SubpagePool(pageSize, 4);
        BuddyChunk chunk = new BuddyChunk(pool, pageSize, 4);
        int handle = chunk.allocate(pageSize * 2);
        assertEquals(handle, 0);
        handle = chunk.allocate(pageSize);
        assertEquals(handle, pageSize * 2);
        handle = chunk.allocate(pageSize * 4);
        assertEquals(handle, pageSize * 4);
        handle = chunk.allocate(pageSize);
        assertEquals(handle, pageSize * 3);

        chunk.free(pageSize * 3, pageSize);
        chunk.free(pageSize * 2, pageSize);
        handle = chunk.allocate(pageSize * 2);
        assertEquals(handle, pageSize * 2);
    }
}
