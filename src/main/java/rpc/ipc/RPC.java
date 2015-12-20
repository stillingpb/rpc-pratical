package rpc.ipc;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import rpc.ipc.client.ClientInvocation;
import rpc.ipc.server.ServerStub;
import rpc.ipc.util.RPCClientException;
import rpc.ipc.util.RPCServerException;

public class RPC {
    private static Map<String, ClientInvocation> clients = new HashMap<String, ClientInvocation>();
    private static Map<String, Boolean> clientsState = new HashMap<String, Boolean>();

    /**
     * 获取一个RPC server
     *
     * @param instance 供调用的对象
     * @param host
     * @param port
     * @return
     * @throws RPCServerException 创建rpcServer失败
     */
    public static ServerStub getServer(Object instance, String host, int port)
            throws RPCServerException {
        return new ServerStub(instance, host, port);
    }

    /**
     * 通过远程调用接口，和服务器套接字，创建远程访问对象
     *
     * @param clazz 远程对象的接口类
     * @param host
     * @param port
     * @return
     */
    public synchronized static <T> T getClientProxy(Class<T> clazz, String host, int port) {
        String socketAddress = host + ":" + port;
        ClientInvocation handler;
        Boolean isRunning = clientsState.get(socketAddress);
        if (isRunning != null && isRunning && clients.containsKey(socketAddress)) {
            handler = clients.get(socketAddress);
        } else {
            handler = new ClientInvocation(host, port);
            clients.put(socketAddress, handler);
            clientsState.put(socketAddress, true);
        }
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, handler);
    }

    /**
     * 直接关闭指定的客户端，不等待请求返回.
     *
     * @param host 连接的服务器端的host
     * @param port 连接的服务器端的port
     * @throws RPCClientException
     */
    public synchronized static void shutdownClientRoughly(String host, int port) throws RPCClientException {
        String socketAddress = host + ":" + port;
        Boolean isRunning = clientsState.get(socketAddress);
        if (isRunning != null && isRunning && clients.containsKey(socketAddress)) {
            clientsState.put(socketAddress, false);
            ClientInvocation handler = clients.remove(socketAddress);
            handler.shutdownRoughly();
        }
    }

    /**
     * 通知关闭指定的客户端,并立即返回结果.
     * 当本方法被调用后，会等待所有已经发起的请求完成调用，但不能发起新的请求。
     *
     * @param host 连接的服务器端的host
     * @param port 连接的服务器端的port
     * @throws RPCClientException
     */
    public synchronized static void shutdownClientSoftly(String host, int port) throws RPCClientException {
        String socketAddress = host + ":" + port;
        Boolean isRunning = clientsState.get(socketAddress);
        if (isRunning != null && isRunning && clients.containsKey(socketAddress)) {
            clientsState.put(socketAddress, false);
            ClientInvocation handler = clients.get(socketAddress);
            handler.shutdownSoftly();
        }
    }

    public synchronized static boolean isTerminited(String host, int port) {
        String socketAddress = host + ":" + port;
        Boolean isRunning = clientsState.get(socketAddress);
        if (isRunning == null) {
            return true;
        }
        if (isRunning == true) {
            return false;
        }
        ClientInvocation handler = clients.get(socketAddress);
        if (handler == null) {
            return true;
        }
        return handler.isTerminited();
    }
}
