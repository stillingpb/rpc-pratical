package rpc.pool;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ThreadLocalCacheTest {
    PoolArena arena;
    ThreadLocalCache cache;

    @Before
    public void init() {
        int pageSize = 1024;
        int pageLevel = 11;
        int maxSubpageSize = 512;
        int minSubpageSize = 16;
        arena = new PoolArena(pageSize, pageLevel, maxSubpageSize, minSubpageSize);
        cache = new ThreadLocalCache(arena, maxSubpageSize / minSubpageSize, 2, 1 << pageLevel, 2);
    }

    @Test
    public void test() {
        ByteBuff buff1 = new ByteBuff();
        arena.allocate(buff1, 123, 128);
        assertTrue(cache.freeToCache(buff1));

        ByteBuff buff2 = new ByteBuff();
        arena.allocate(buff2, 124, 128);
        assertTrue(cache.freeToCache(buff2));


        ByteBuff buff3 = new ByteBuff();
        arena.allocate(buff3, 125, 128);
        assertFalse(cache.freeToCache(buff3));

        ByteBuff buff4 = new ByteBuff();
        assertTrue(cache.allocateFromCache(buff4, 114, 128));
        assertEquals(buff1.handle, buff4.handle);


        ByteBuff buff5 = new ByteBuff();
        assertTrue(cache.allocateFromCache(buff5, 115, 128));
        assertEquals(buff2.handle, buff5.handle);

        ByteBuff buff6 = new ByteBuff();
        assertFalse(cache.allocateFromCache(buff6, 116, 128));
    }

    @Test
    public void test2() {
        ByteBuff buff1 = new ByteBuff();
        arena.allocate(buff1, 1230, 2048);
        assertTrue(cache.freeToCache(buff1));

        ByteBuff buff2 = new ByteBuff();
        arena.allocate(buff2, 1240, 2048);
        assertTrue(cache.freeToCache(buff2));


        ByteBuff buff3 = new ByteBuff();
        arena.allocate(buff3, 1250, 2048);
        assertFalse(cache.freeToCache(buff3));

        ByteBuff buff4 = new ByteBuff();
        assertTrue(cache.allocateFromCache(buff4, 1140, 2048));
        assertEquals(buff1.handle, buff4.handle);


        ByteBuff buff5 = new ByteBuff();
        assertTrue(cache.allocateFromCache(buff5, 1150, 2048));
        assertEquals(buff2.handle, buff5.handle);

        ByteBuff buff6 = new ByteBuff();
        assertFalse(cache.allocateFromCache(buff6, 1160, 2048));
    }
}
