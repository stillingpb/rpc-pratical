package rpc.ipc;

import java.lang.reflect.Proxy;

import rpc.ipc.client.ClientInvocation;
import rpc.ipc.server.ServerStub;
import rpc.ipc.util.RPCServerException;

public class RPC {

	/**
	 * 获取一个RPC server
	 * 
	 * @param instance
	 *            供调用的对象
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
	 * @param clazz
	 *            远程对象的接口类
	 * @param host
	 * @param port
	 * @return
	 */
	public static <T> T getClientProxy(Class<T> clazz, String host, int port) {
		ClientInvocation handler = new ClientInvocation(host, port);
		return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, handler);
	}
}
