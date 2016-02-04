package rpc.pool;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

public class ByteBuffTest {
    @Test
    public void test() {
        ByteBuff<byte[]> buff = new PooledHeapByteBuff();
        byte[] data = new byte[128];
        buff.init(null, data, 0, 128);
        ByteBuffer byteBuffer = buff.getByteBuffer();
        assertEquals(false, byteBuffer.isDirect());
        assertEquals(0, byteBuffer.position());
        assertEquals(128, byteBuffer.capacity());
        assertEquals(data, byteBuffer.array());
    }

    @Test
    public void test2(){
        ByteBuff<ByteBuffer> buff = new PooledDirectByteBuff();
        ByteBuffer data = ByteBuffer.allocateDirect(128);
        buff.init(null, data, 64, 64);
        ByteBuffer byteBuffer = buff.getByteBuffer();
        assertEquals(true, byteBuffer.isDirect());
        assertEquals(64, byteBuffer.position());
        assertEquals(128, byteBuffer.capacity());
    }
}
