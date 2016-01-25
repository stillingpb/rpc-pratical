package rpc.pool;

public class SlabSubpageAllocatorTest {
    public static void main(String[] args) {
        int subpageNum = 150;
        SlabSubpageAllocator allocator = new SlabSubpageAllocator(subpageNum);
        for(int i=0; i<subpageNum;i++){
            allocator.obtainIdelPosition();
            System.out.println(allocator.toString());
        }

        allocator.free(0);
        allocator.free(63);
        allocator.free(64);
        allocator.free(65);
        allocator.free(119);
        allocator.free(120);
        allocator.free(121);
        allocator.free(149);
        System.out.println(allocator.toString());
    }
}
