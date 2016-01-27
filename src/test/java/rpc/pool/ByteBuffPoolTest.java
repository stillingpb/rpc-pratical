package rpc.pool;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
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

}
