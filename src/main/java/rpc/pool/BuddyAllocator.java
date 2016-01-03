package rpc.pool;

class BuddyAllocator {
    int maxLevel;
    int weightLen;
    int[] weights;

    public BuddyAllocator(int maxLevel) {
        this.maxLevel = maxLevel;
        this.weightLen = (1 << maxLevel) - 1;
        this.weights = new int[weightLen];
        for (int i = 1, j = 0; i <= maxLevel; i++) {
            int w = level2Weight(i - 1);
            for (; j <= (1 << i) - 2; j++) {
                weights[j] = w;
            }
        }
    }

    public int alloc(int size) {
        size = fixSize(size);
        if (weights[0] < size) {
            return -1;
        }
        int index = 0;
        while (index < weightLen) {
            int leftIndex = subNodeIndex(index, true);
            if (leftIndex >= weightLen) {
                break;
            }
            int rightIndex = subNodeIndex(index, false);
            int leftWeight = weights[leftIndex];
            int rightWeight = weights[rightIndex];

            if (leftWeight < size && rightWeight < size) {
                index = parNodeIndex(leftIndex);
                break;
            } else if (leftWeight >= size && rightWeight >= size) {
                index = leftWeight <= rightWeight ? leftIndex : rightIndex;
            } else {
                index = leftWeight >= rightWeight ? leftIndex : rightIndex;
            }
        }
        weights[index] = 0;
        int parIndex = parNodeIndex(index);
        while (parIndex >= 0) {
            int leftSubWeight = subNodeWeight(parIndex, true);
            int rightSubWeight = subNodeWeight(parIndex, false);
            int newParWeight = leftSubWeight >= rightSubWeight ? leftSubWeight : rightSubWeight;
            if (newParWeight == weights[parIndex]) {
                break;
            } else {
                weights[parIndex] = newParWeight;
                parIndex = parNodeIndex(parIndex);
            }
        }

        int offset = index2Offset(index);
        return offset;
    }

    private int index2Offset(final int index) {
        int level = -1;
        int tmp = index + 1;
        while (tmp != 0) {
            tmp >>= 1;
            level++;
        }
        int levelOffest = index - (1 << level) + 1;
        int weight = level2Weight(level);
        int offest = weight * levelOffest;
        return offest;
    }

    private int offset2Index(int offset, int weight) {
        int baseIndex = (1 << (maxLevel - 1)) / weight - 1;
        int levelOffest = offset / weight;
        return baseIndex + levelOffest;
    }

    private int level2Weight(int weight) {
        return 1 << (maxLevel - weight - 1);
    }

    /**
     * @param parIndex parent index
     * @param isLeft   left is true, right is false
     * @return sub node weight
     */
    private int subNodeWeight(int parIndex, boolean isLeft) {
        int subIndex = subNodeIndex(parIndex, isLeft);
        return weights[subIndex];
    }

    private int parNodeIndex(int subIndex) {
        return (subIndex - 1) >> 1;
    }

    /**
     * @param parIndex parent index
     * @param isLeft   left is true, right is false
     * @return sub node index
     */
    private int subNodeIndex(int parIndex, boolean isLeft) {
        if (isLeft) {
            return (parIndex << 1) + 1;
        } else {
            return (parIndex << 1) + 2;
        }
    }

    public void free(int offset, int size) {
        int weight = fixSize(size);
        int index = offset2Index(offset, weight);
        weights[index] = weight;
        int parIndex = parNodeIndex(index);
        weight <<= 1;
        while (parIndex >= 0) {
            int leftSubWeight = subNodeWeight(parIndex, true);
            int rightSubWeight = subNodeWeight(parIndex, false);
            if (leftSubWeight + rightSubWeight == weight) {
                weights[parIndex] = weight;
            } else {
                int maxWeight = leftSubWeight >= rightSubWeight ? leftSubWeight : rightSubWeight;
                if (maxWeight > weights[parIndex]) {
                    weights[parIndex] = maxWeight;
                } else {
                    break;
                }
            }

            weight <<= 1;
            parIndex = parNodeIndex(parIndex);
        }
    }

    int fixSize(int size) {
        if (size < 0) {
            throw new RuntimeException("size too small");
        }
        if (size > ((Integer.MAX_VALUE >> 1) + 1)) {
            throw new RuntimeException("size too big");
        }
        if ((size & (size - 1)) == 0) {
            return size;
        }
        size |= size >> 1;
        size |= size >> 2;
        size |= size >> 4;
        size |= size >> 8;
        size |= size >> 16;
        return size + 1;
    }
}