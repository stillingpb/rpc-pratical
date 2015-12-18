package rpc.ipc.client;

import rpc.ipc.util.RPCClientException;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultConnectionPool implements ConnectionPool {

    private final int MAX_CONNECTION_SIZE; //最大连接数量
    private final int MAX_IDLE_CONNECTION; // 最大空闲连接数量
    private AtomicInteger connectionCount = new AtomicInteger(0);
    private ReentrantLock lock = new ReentrantLock();
    private Condition productCondi = lock.newCondition();

    private String ipcHost;
    private Integer ipcPort;

    private Queue<Connection> pool = new LinkedList<Connection>();

    public DefaultConnectionPool(String ipcHost, Integer ipcPort) {
        this(ipcHost, ipcPort, 20, 10);
    }

    public DefaultConnectionPool(String ipcHost, Integer ipcPort, int maxSize, int maxIdelSize) {
        this.ipcHost = ipcHost;
        this.ipcPort = ipcPort;
        MAX_CONNECTION_SIZE = maxSize;
        MAX_IDLE_CONNECTION = maxIdelSize;
    }

    private Connection createConnection() throws RPCClientException {
        if (ipcHost == null) {
            throw new RPCClientException("没有设置需要连接的主机host");
        }
        if (ipcPort == null) {
            throw new RPCClientException("没有设置需要连接的主机的端口号port");
        }
        try {
            Socket socket = new Socket(ipcHost, ipcPort);
            return new ConnectionProxy(this, socket);
        } catch (IOException e) {
            throw new RPCClientException("创建socket连接出现异常", e);
        }
    }

    @Override
    public Connection getConnection() throws RPCClientException {
        lock.lock();
        try {
            if (!pool.isEmpty()) {
                return pool.poll();
            }
            if (connectionCount.get() < MAX_CONNECTION_SIZE) {
                Connection newConn = createConnection();
                connectionCount.incrementAndGet();
                return newConn;
            }
            while (pool.isEmpty()) {
                try {
                    productCondi.await();
                } catch (InterruptedException e) {
                    throw new RPCClientException("等待新connection时被中断", e);
                }
            }
            return pool.poll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void releaseConnection(Connection conn) throws RPCClientException {
        lock.lock();
        try {
            if (pool.size() >= MAX_IDLE_CONNECTION) {
                connectionCount.decrementAndGet();
                closeConnection(conn);
            } else {
                pool.offer(conn);
                productCondi.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    private void closeConnection(Connection conn) throws RPCClientException {
        conn.close();
    }
}
