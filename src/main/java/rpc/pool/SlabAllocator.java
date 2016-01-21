package rpc.pool;

public class SlabAllocator {
    private int bitMapLength;
    private long[] bitMap; // 0 - idle, 1 - used

    public SlabAllocator(int maxElemNum) {
        bitMapLength = (maxElemNum + 63) >>> 6;
        bitMap = new long[bitMapLength];
    }

    int obtainIdelPosition() {
        for (int i = 0; i < bitMapLength; i++) {
            if (~bitMap[i] != 0) {
                int pos = obtainIdelPosition(bitMap[i]);
                if (pos == -1) {
                    return -1;
                }
                bitMap[i] |= (1 << pos); // set idle state to used
                return i * 64 + pos;
            }
        }
        return -1;
    }

    private int obtainIdelPosition(long state) {
        for (int j = 0; j < 64; j++) {
            if ((state & 1) == 0) {
                return j;
            }
            state >>>= 1;
        }
        return -1;
    }

    public void free(int pos) {
        int i = pos / 64;
        int j = pos % 64;
        bitMap[i] &= (~(1 << j));
    }
}
