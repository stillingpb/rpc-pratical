package rpc.pool;

public class PoolUtil {

    /**
     * 将2的n次方转换成n.
     * 其中n需要大于等于0.
     *
     * @param power 2的n次方
     * @return n
     */
    public static int power2Level(int power) {
        for (int i = 0; i < 32; i++) {
            if ((power & (1 << i)) != 0) {
                return i;
            }
        }
        return -1;
    }
}
