package rpc.pool;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class ByteBuffPoolTest {
    @Test
    public void test0() {
        for (int i = 1; i <= 512; i++) {
            assertEquals((i + 15) / 16 * 16, ByteBuffPool.normalizeCapacity(i));
        }
        for (int i = 513; i <= 1024; i++) {
            assertEquals(1024, ByteBuffPool.normalizeCapacity(i));
        }
        for (int i = 1025; i <= 2048; i++) {
            assertEquals(2048, ByteBuffPool.normalizeCapacity(i));
        }
    }

    @Test
    public void test1() {// free with same thread
        ByteBuff buff1 = ByteBuffPool.allocateDirect(123);
        PoolChunk chunk1 = buff1.poolChunk;
        int handle1 = buff1.handle;

        ByteBuff buff2 = ByteBuffPool.allocateDirect(123);
        PoolChunk chunk2 = buff2.poolChunk;
        int handle2 = buff2.handle;

        buff1.free(); // free with same thread
        buff2.free(); // free with same thread

        ByteBuff buff3 = ByteBuffPool.allocateDirect(123); // predicate: allocate from cache
        assertEquals(chunk1, buff3.poolChunk);
        assertEquals(handle1, buff3.handle);

        ByteBuff buff4 = ByteBuffPool.allocateDirect(123); // predicate: allocate from cache
        assertEquals(chunk2, buff4.poolChunk);
        assertEquals(handle2, buff4.handle);
    }

    @Test
    public void test2() {        // free with different thread
        final ByteBuff buff1 = ByteBuffPool.allocateDirect(123);
        PoolChunk chunk1 = buff1.poolChunk;
        int handle1 = buff1.handle;

        ByteBuff buff2 = ByteBuffPool.allocateDirect(123);
        PoolChunk chunk2 = buff2.poolChunk;
        int handle2 = buff2.handle;

        // free with different thread
        try {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    buff1.free();
                }
            });
            t.start();
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        buff2.free(); // free with same thread

        ByteBuff buff3 = ByteBuffPool.allocateDirect(123); // predicate: allocate from cache
        assertEquals(chunk2, buff3.poolChunk);
        assertEquals(handle2, buff3.handle);

        ByteBuff buff4 = ByteBuffPool.allocateDirect(123); // predicate: allocate from chunk
        assertEquals(chunk1, buff4.poolChunk);
        assertEquals(handle1, buff4.handle);
    }

    @Test
    public void test3() {
        final ThreadLocalCache caches[] = new ThreadLocalCache[3];
        caches[0] = ByteBuffPool.obtainThreadLocalCache(true);
        caches[1] = ByteBuffPool.obtainThreadLocalCache(true);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                caches[2] = ByteBuffPool.obtainThreadLocalCache(true);
            }
        });
        try {
            t.start();
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(caches[0], caches[1]);
        assertNotEquals(caches[0], caches[2]);
    }
}
