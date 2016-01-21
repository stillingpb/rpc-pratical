package rpc.pool;

public class BuddyAllocatorTest {

    public static void printWeight(BuddyAllocator pool) {
        for (int i = 1, j = 0; i <= pool.maxLevel; i++) {
            for (; j <= (1 << i) - 2; j++) {
                System.out.print(pool.weights[j] + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        BuddyAllocator pool = new BuddyAllocator(4);
        System.out.println(pool.obtainIdelPagePosition(2));
        System.out.println(pool.obtainIdelPagePosition(1));
        System.out.println(pool.obtainIdelPagePosition(2));
        System.out.println(pool.obtainIdelPagePosition(1));
        printWeight(pool);
        pool.free(4, 2);
        printWeight(pool);
        pool.free(3, 1);
        printWeight(pool);
        pool.free(2, 1);
        printWeight(pool);
        pool.free(0, 2);
        printWeight(pool);
    }
}
