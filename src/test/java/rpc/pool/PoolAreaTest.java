package rpc.pool;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class PoolAreaTest {
    int pageSize = 1024;
    int maxLevel = 4;
    PoolArea area;
    SubpagePool subpagePool;
    BuddyChunkList q050;
    BuddyChunkList q025;
    BuddyChunkList qInit;
    BuddyChunkList q075;

    @Before
    public void init() {
        area = new PoolArea(pageSize, maxLevel, pageSize / 2, 16);
        try {
            Field f = PoolArea.class.getDeclaredField("subpagePool");
            f.setAccessible(true);
            subpagePool = (SubpagePool) f.get(area);

            f = PoolArea.class.getDeclaredField("qInit");
            f.setAccessible(true);
            qInit = (BuddyChunkList) f.get(area);
            f = PoolArea.class.getDeclaredField("q025");
            f.setAccessible(true);
            q025 = (BuddyChunkList) f.get(area);
            f = PoolArea.class.getDeclaredField("q050");
            f.setAccessible(true);
            q050 = (BuddyChunkList) f.get(area);
            f = PoolArea.class.getDeclaredField("q075");
            f.setAccessible(true);
            q075 = (BuddyChunkList) f.get(area);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void test() {
        ByteBuff buff = new ByteBuff();
        PoolChunk chunk = null;
        area.allocate(buff, pageSize, pageSize);
        chunk = buff.poolChunk;
        assertNotNull(buff.getByteBuffer());
        assertTrue(chunk instanceof BuddyChunk);
        assertEquals(1, getChunkNum(qInit));

        ByteBuff buff2 = new ByteBuff();
        area.allocate(buff2, 8 * pageSize - 1, 8 * pageSize);
        assertNotNull(buff2.getByteBuffer());
        assertEquals(chunk, buff2.poolChunk);
        assertEquals(1, getChunkNum(q025));
        assertEquals(8 * pageSize - 1, buff2.capacity);
        assertEquals(8 * pageSize, buff2.handle);

        area.allocate(new ByteBuff(), pageSize, pageSize);
        int elemCapacity = pageSize / 4;
        ByteBuff buff3 = new ByteBuff();
        area.allocate(buff3, elemCapacity, elemCapacity);
        assertEquals(elemCapacity, buff3.capacity);
        assertEquals(1, getChunkNum(q050));
        assertNotNull(buff3.getByteBuffer());
        assertTrue(buff3.poolChunk instanceof SlabChunk);
        assertNotNull(subpagePool.slabChunks[elemCapacity / 16 - 1]);

        ByteBuff buff4 = new ByteBuff();
        ByteBuff buff5 = new ByteBuff();
        ByteBuff buff6 = new ByteBuff();
        area.allocate(buff4, elemCapacity, elemCapacity);
        area.allocate(buff5, elemCapacity, elemCapacity);
        area.allocate(buff6, elemCapacity, elemCapacity);
        assertNull(subpagePool.slabChunks[elemCapacity / 16 - 1]);

        ByteBuff buff7 = new ByteBuff();
        area.allocate(buff7, elemCapacity, elemCapacity);
        assertNotNull(subpagePool.slabChunks[elemCapacity / 16 - 1]);

        area.free(buff2, 8 * pageSize);
        assertEquals(1, getChunkNum(q025));

        // free a slabChunk
        area.free(buff3, elemCapacity);
        area.free(buff4, elemCapacity);
        area.free(buff5, elemCapacity);
        area.free(buff6, elemCapacity);
        assertEquals(1, getChunkNum(qInit));
    }

    private int getChunkNum(BuddyChunkList list) {
        // test double linked list is ok.
        BuddyChunk cur = list.head;
        int count = 0;
        int num = 0;
        BuddyChunk tail = null;
        while (cur != null) {
            count++;
            num++;
            tail = cur;
            cur = cur.nextChunk;
        }
        cur = tail;
        while (cur != null) {
            num--;
            cur = cur.preChunk;
        }
        assert num == 0;

        return count;
    }
}
