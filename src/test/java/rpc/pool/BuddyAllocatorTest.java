package rpc.pool;

public class BuddyAllocatorTest {



    public static void main(String[] args) {
        BuddyPageAllocator pool = new BuddyPageAllocator(4);
        System.out.println(pool.obtainIdelPagePosition(2));
        System.out.println(pool.obtainIdelPagePosition(1));
        System.out.println(pool.obtainIdelPagePosition(4));
        System.out.println(pool.obtainIdelPagePosition(1));
        System.out.println(pool.toString());
        pool.free(4, 4);
        System.out.println(pool.toString());
        pool.free(3, 1);
        System.out.println(pool.toString());
        pool.free(2, 1);
        System.out.println(pool.toString());
        pool.free(0, 2);
        System.out.println(pool.toString());
    }
}
