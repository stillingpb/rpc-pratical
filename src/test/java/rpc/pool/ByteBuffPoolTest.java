package rpc.pool;

public class ByteBuffPoolTest {
    public static void main(String[] args) {
        ByteBuffPool pool = new ByteBuffPool();
        ByteBuff buf = pool.allocate(2);
    }
}
