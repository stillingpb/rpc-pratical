package rpc.ipc.client.connection;

import rpc.ipc.util.RPCClientException;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultConnectionPool implements ConnectionPool {
    private volatile boolean isRunning;

    private final int MAX_CONNECTION_SIZE; //最大连接数量
    private final int MAX_IDLE_CONNECTION; // 最大空闲连接数量
    private final int SOCKET_TIMEOUT;
    private AtomicInteger connectionCount = new AtomicInteger(0); //统计当前已生成的connection对象的数目
    private ReentrantLock lock = new ReentrantLock();
    private Condition productCondi = lock.newCondition();

    private String ipcHost;
    private Integer ipcPort;

    private Queue<Connection> pool = new LinkedList<Connection>();
    private Set<Connection> connections = new HashSet<Connection>();

    public DefaultConnectionPool(String ipcHost, Integer ipcPort) {
        this(ipcHost, ipcPort, 20, 10, 0);
    }

    public DefaultConnectionPool(String ipcHost, Integer ipcPort, int maxSize, int maxIdelSize, int socketTimeout) {
        this.isRunning = true;
        this.ipcHost = ipcHost;
        this.ipcPort = ipcPort;
        MAX_CONNECTION_SIZE = maxSize;
        MAX_IDLE_CONNECTION = maxIdelSize;
        SOCKET_TIMEOUT = socketTimeout;
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
            socket.setTcpNoDelay(true);
            socket.setKeepAlive(true);
            socket.setSoTimeout(SOCKET_TIMEOUT);
            Connection newConn = new ConnectionProxy(this, socket);
            connectionCount.incrementAndGet();
            connections.add(newConn);
            return newConn;
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
            if (isRunning) {
                if (pool.size() >= MAX_IDLE_CONNECTION) {
                    closeRealConnection(conn);
                } else {
                    pool.offer(conn);
                    productCondi.signal();
                }
            } else {
                closeRealConnection(conn);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void closeRoughly() throws RPCClientException {
        isRunning = false;
        Object[] copyConnections = new Connection[0];
        for (Object obj : copyConnections) {
            if (obj instanceof Connection) {
                Connection conn = (Connection) obj;
                closeRealConnection(conn);
            }
        }
        connections.clear();
    }

    @Override
    public void closeSoftly() throws RPCClientException {
        isRunning = false;
        lock.lock();
        try {
            while (!pool.isEmpty()) {
                Connection conn = pool.poll();
                closeRealConnection(conn);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isCloseComplemented() {
        return !isRunning && connectionCount.get() == 0;
    }

    private void closeRealConnection(Connection conn) throws RPCClientException {
        if (conn instanceof ConnectionProxy) {
            connectionCount.decrementAndGet();
            Connection realConn = ((ConnectionProxy) conn).getRealConnection();
            realConn.close();
            connections.remove(conn);
        }
    }
}
