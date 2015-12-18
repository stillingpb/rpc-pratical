package rpc.ipc.client;

import java.util.concurrent.FutureTask;

import rpc.io.ExceptionWritable;
import rpc.io.NullWritable;
import rpc.io.Writable;
import rpc.ipc.util.RPCClientException;

public class ClientStub {
    private ConnectionPool connectionPool;

    public ClientStub(String host, int port) {
        ConnectionPool connectionPool = new DefaultConnectionPool(host, port, 20, 10, 0);
        this.connectionPool = connectionPool;
    }

    public ClientStub(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public Writable remoteCall(String methodName, Writable[] parameter) throws RPCClientException {
        Call call = new Call(methodName, parameter);
        Connection connection = connectionPool.getConnection();
        Writable result = connection.remoteCall(call);
        connection.close();
        return result;
    }

}
