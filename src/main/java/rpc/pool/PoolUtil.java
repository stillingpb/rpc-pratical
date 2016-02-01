package rpc.pool;

public class PoolUtil {

    public static int log2(int val) {
        // compute the (0-based, with lsb = 0) position of highest set bit i.e, log2
        return Integer.SIZE - 1 - Integer.numberOfLeadingZeros(val);
    }

}
