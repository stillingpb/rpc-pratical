package rpc.pool;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

public class BuddyChunkListTest {
    int pageSize = 1024;
    int maxLevel = 3;
    BuddyChunkList q050;
    BuddyChunkList q025;
    BuddyChunkList qInit;
    BuddyChunkList q075;
    PoolArena area = new PoolArena(pageSize, maxLevel, pageSize / 2, 16, true);

    @Before
    public void init() {
        try {
            Field f = PoolArena.class.getDeclaredField("qInit");
            f.setAccessible(true);
            qInit = (BuddyChunkList) f.get(area);
            f = PoolArena.class.getDeclaredField("q025");
            f.setAccessible(true);
            q025 = (BuddyChunkList) f.get(area);
            f = PoolArena.class.getDeclaredField("q050");
            f.setAccessible(true);
            q050 = (BuddyChunkList) f.get(area);
            f = PoolArena.class.getDeclaredField("q075");
            f.setAccessible(true);
            q075 = (BuddyChunkList) f.get(area);

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test1() {
        ByteBuff buff = new PooledHeapByteBuff();
        BuddyChunk chunk = new BuddyChunk.BuddyHeapChunk(pageSize, maxLevel);
        qInit.addChunk(chunk);
        assertEquals(1, getChunkNum(qInit));

        qInit.allocate(buff, pageSize, pageSize); // chunk usage: 12.5%
        qInit.allocate(buff, pageSize, pageSize); // chunk usage: 25%
        qInit.allocate(buff, pageSize, pageSize); // chunk usage: 37.5%
        assertEquals(1, getChunkNum(qInit));

        qInit.allocate(buff, pageSize, pageSize); // chunk usage: 50%
        assertEquals(0, getChunkNum(qInit));
        assertEquals(1, getChunkNum(q025));

        q025.allocate(buff, pageSize, pageSize); // chunk usage: 62.5%
        assertEquals(1, getChunkNum(q025));

        q025.allocate(buff, pageSize, pageSize); // chunk usage: 75%
        assertEquals(0, getChunkNum(q025));
        assertEquals(1, getChunkNum(q050));


        q050.allocate(buff, pageSize, pageSize); // chunk usage: 87.5%
        assertEquals(1, getChunkNum(q050));

        q050.allocate(buff, pageSize, pageSize); // chunk usage: 100%
        assertEquals(0, getChunkNum(q050));
        assertEquals(1, getChunkNum(q075));

        buff.handle = 7 * pageSize;
        area.free(buff, pageSize); // chunk useage: 87.5%
        assertEquals(1, getChunkNum(q075));

        buff.handle = 6 * pageSize;
        area.free(buff, pageSize); // chunk useage: 75%
        assertEquals(1, getChunkNum(q075));

        buff.handle = 5 * pageSize;
        area.free(buff, pageSize); // chunk useage: 62.5%
        assertEquals(0, getChunkNum(q075));
        assertEquals(1, getChunkNum(q050));

        buff.handle = 4 * pageSize;
        area.free(buff, pageSize); // chunk useage: 50%
        assertEquals(1, getChunkNum(q050));

        buff.handle = 3 * pageSize;
        area.free(buff, pageSize); // chunk useage: 37.5%
        assertEquals(0, getChunkNum(q050));
        assertEquals(1, getChunkNum(q025));

        buff.handle = 2 * pageSize;
        area.free(buff, pageSize); // chunk useage: 25%
        assertEquals(1, getChunkNum(q025));

        buff.handle = 1 * pageSize;
        area.free(buff, pageSize); // chunk useage: 12.5%
        assertEquals(0, getChunkNum(q025));
        assertEquals(1, getChunkNum(qInit));

        buff.handle = 0 * pageSize;
        area.free(buff, pageSize); // chunk useage: 0%
        assertEquals(1, getChunkNum(qInit));
    }

    @Test
    public void test2() {
        ByteBuff buff = new PooledHeapByteBuff();
        BuddyChunk chunk1 = new BuddyChunk.BuddyHeapChunk(pageSize, maxLevel);
        BuddyChunk chunk2 = new BuddyChunk.BuddyHeapChunk(pageSize, maxLevel);
        BuddyChunk chunk3 = new BuddyChunk.BuddyHeapChunk(pageSize, maxLevel);
        qInit.addChunk(chunk1);
        qInit.addChunk(chunk2);
        qInit.addChunk(chunk3);
        assertEquals(3, getChunkNum(qInit));

        allocateFromChunkList(buff, qInit, pageSize, 3 * 4);
        assertEquals(0, getChunkNum(qInit));
        assertEquals(3, getChunkNum(q025));

        allocateFromChunkList(buff, q025, pageSize, 3 * 2);
        assertEquals(0, getChunkNum(q025));
        assertEquals(3, getChunkNum(q050));

        allocateFromChunkList(buff, q050, pageSize, 3 * 2);
        assertEquals(0, getChunkNum(q050));
        assertEquals(3, getChunkNum(q075));

        ByteBuff buff1 = new PooledHeapByteBuff();
        buff1.poolChunk = chunk1;
        ByteBuff buff2 = new PooledHeapByteBuff();
        buff2.poolChunk = chunk2;
        ByteBuff buff3 = new PooledHeapByteBuff();
        buff3.poolChunk = chunk3;
        ByteBuff[] buffs = {buff1, buff2, buff3};

        freeFromChunkList(buffs, pageSize, 0, 1, 2);
        assertEquals(0, getChunkNum(q075));
        assertEquals(3, getChunkNum(q050));

        freeFromChunkList(buffs, pageSize, 4, 6);
        assertEquals(0, getChunkNum(q050));
        assertEquals(3, getChunkNum(q025));

        freeFromChunkList(buffs, pageSize, 3, 7);
        assertEquals(0, getChunkNum(q025));
        assertEquals(3, getChunkNum(qInit));

        freeFromChunkList(buffs, pageSize, 5);
        assertEquals(3, getChunkNum(qInit));
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

    private void freeFromChunkList(ByteBuff[] buff, int normalCapacity, int... offset) {
        for (int i = 0; i < offset.length; i++) {
            int handle = normalCapacity * offset[i];
            for (int j = 0; j < buff.length; j++) {
                buff[j].handle = handle;
                area.free(buff[j], normalCapacity);
            }
        }
    }

    private void allocateFromChunkList(ByteBuff buff, BuddyChunkList list, int normalCapacity, int times) {
        for (int i = 0; i < times; i++) {
            list.allocate(buff, normalCapacity, normalCapacity);
        }
    }
}
