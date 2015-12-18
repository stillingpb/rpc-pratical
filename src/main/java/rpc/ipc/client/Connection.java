package rpc.ipc.client;

import rpc.io.Writable;
import rpc.ipc.util.RPCClientException;

public interface Connection {
    public Writable remoteCall(Call call) throws RPCClientException;

    public void close() throws RPCClientException;
}
