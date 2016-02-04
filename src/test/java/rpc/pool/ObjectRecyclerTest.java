package rpc.pool;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class ObjectRecyclerTest {
    @Test
    public void testObjectCreator() {
        ObjectRecycler.ObjectFactory<ByteBuff> byteBuffCreator = getCreator();
        ByteBuff buff1 = byteBuffCreator.createNewObject();
        assertNotNull(buff1);
        ByteBuff buff2 = byteBuffCreator.createNewObject();
        assertNotEquals(buff1, buff2);
    }

    @Test
    public void testObjectRecycler() {
        ObjectRecycler.ObjectFactory<ByteBuff> byteBuffCreator = getCreator();
        ObjectRecycler<ByteBuff> recycler = new ObjectRecycler<ByteBuff>(2, byteBuffCreator);
        ByteBuff buff1 = recycler.get();
        ByteBuff buff2 = recycler.get();
        ByteBuff buff3 = recycler.get();
        assertNotNull(buff1);
        assertNotNull(buff2);
        assertNotNull(buff3);
        assertNotEquals(buff1, buff2);
        assertNotEquals(buff3, buff2);

        recycler.recycle(buff1);
        recycler.recycle(buff2);
        recycler.recycle(buff3);

        assertEquals(buff2, recycler.get());
        assertEquals(buff1, recycler.get());
        assertNotEquals(buff3, recycler.get());
    }

    @Test
    public void testThreadLocal() throws InterruptedException {
        ByteBuff buff = new PooledHeapByteBuff();
        ByteBuff buff1 = new PooledHeapByteBuff();
        ByteBuff buff2 = new PooledHeapByteBuff();
        ObjectRecycler.ObjectFactory<ByteBuff> byteBuffCreator = getCreator();
        ObjectRecycler<ByteBuff> recycler = new ObjectRecycler<ByteBuff>(2, byteBuffCreator);

        Processor processor1 = new Processor(recycler, buff, buff1);
        Thread t1 = new Thread(processor1);
        t1.start();
        t1.join();
        Processor processor2 = new Processor(recycler, buff, buff2);
        Thread t2 = new Thread(processor2);
        t2.start();
        t2.join();
        ByteBuff[] rst1 = processor1.rst;
        ByteBuff[] rst2 = processor2.rst;
        assertEquals(buff1, rst1[0]);
        assertEquals(buff2, rst2[0]);
        assertEquals(buff, rst1[1]);
        assertEquals(rst1[1], rst2[1]);
    }

    private static class Processor implements Runnable {
        private ByteBuff[] buffs;
        private ObjectRecycler<ByteBuff> recycler;
        ByteBuff[] rst;

        public Processor(ObjectRecycler<ByteBuff> recycler, ByteBuff... buffs) {
            this.recycler = recycler;
            this.buffs = buffs;
        }

        public void run() {
            for (int i = 0; i < buffs.length; i++) {
                ByteBuff buff = buffs[i];
                recycler.recycle(buff);
            }
            rst = new ByteBuff[buffs.length];
            for (int i = 0; i < rst.length; i++) {
                rst[i] = recycler.get();
            }
        }
    }

    private ObjectRecycler.ObjectFactory<ByteBuff> getCreator() {
        ObjectRecycler.ObjectFactory<ByteBuff> byteBuffCreator = new ObjectRecycler.ObjectFactory<ByteBuff>() {
            @Override
            public ByteBuff createNewObject() {
                return new PooledHeapByteBuff();
            }
        };
        return byteBuffCreator;
    }
}
