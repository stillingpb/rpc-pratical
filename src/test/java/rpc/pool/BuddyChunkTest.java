package rpc.pool;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BuddyChunkTest {
    @Test
    public void test() {
        int pageSize = 512;
        BuddyChunk chunk = new BuddyChunk(pageSize, 4);
        int handle = chunk.allocate(pageSize * 2);
        assertEquals(0, handle);
        handle = chunk.allocate(pageSize);
        assertEquals(pageSize * 2, handle);
        handle = chunk.allocate(pageSize * 4);
        assertEquals(pageSize * 4, handle);
        handle = chunk.allocate(pageSize);
        assertEquals(pageSize * 3, handle);
        assertEquals(100 * 8 / 16, chunk.usage());

        chunk.free(pageSize * 3, pageSize);
        chunk.free(pageSize * 2, pageSize);
        assertEquals(100 * 6 / 16, chunk.usage());

        handle = chunk.allocate(pageSize * 2);
        assertEquals(pageSize * 2, handle);
        assertEquals(100 * 8 / 16, chunk.usage());
        chunk.allocate(pageSize * 8);
        assertEquals(100 * 16 / 16, chunk.usage());
    }
}
