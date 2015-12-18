package rpc.ipc.client;

import rpc.ipc.util.RPCClientException;

public interface ConnectionPool {
    public Connection getConnection() throws RPCClientException;

    public void releaseConnection(Connection conn) throws RPCClientException;
}
