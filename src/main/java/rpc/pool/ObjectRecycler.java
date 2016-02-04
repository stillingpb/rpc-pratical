package rpc.pool;

import java.util.Stack;

public class ObjectRecycler<T> {
    private static final int DEFAULT_RECYCLE_SIZE = 200;
    private final int recycleSize;

    private ThreadLocal<Stack<T>> threadRecyclers;
    private ObjectFactory<T> objectCreator;

    public ObjectRecycler(int recycleSize, ObjectFactory<T> objectCreator) {
        assert recycleSize > 0;
        this.recycleSize = Math.min(recycleSize, DEFAULT_RECYCLE_SIZE);
        this.objectCreator = objectCreator;
        this.threadRecyclers = new ThreadLocal<Stack<T>>();
    }

    public void recycle(T obj) {
        Stack<T> recycler = getThreadRecycler();
        if (recycler.size() < recycleSize) {
            recycler.push(obj);
        }
    }

    public T get() {
        Stack<T> recycler = getThreadRecycler();
        T obj;
        if (recycler.isEmpty()) {
            obj = objectCreator.createNewObject();
        } else {
            obj = recycler.pop();
        }
        return obj;
    }

    private Stack<T> getThreadRecycler() {
        Stack<T> recycler = threadRecyclers.get();
        if (recycler == null) {
            Stack<T> stack = createNewRecycler();
            threadRecyclers.set(stack);
            recycler = stack;
        }
        return recycler;
    }

    private Stack<T> createNewRecycler() {
        Stack<T> stack = new Stack<T>();
        return stack;
    }

    public static interface ObjectFactory<T> {
        public T createNewObject();

        public void freeObject(T obj);
    }
}
