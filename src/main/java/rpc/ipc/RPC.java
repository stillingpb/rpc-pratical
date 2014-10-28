package rpc.ipc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import rpc.io.Writable;
import rpc.ipc.client.ClientStub;
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

	private static class ClientInvocation implements InvocationHandler {
		private String host;
		private int port;

		public ClientInvocation(String host, int port) {
			this.host = host;
			this.port = port;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// 将object数组转换成writable数组
			Writable[] args2 = new Writable[args.length];
			for (int i = 0; i < args.length; i++) {
				if (args[i] instanceof Writable)
					args2[i] = (Writable) args[i];
				else
					throw new IllegalArgumentException("方法参数必须是Writable对象");
			}
			// 调用远程方法
			Writable result = ClientStub.call(method.getName(), args2, host, port);
			return result;
		}
	}

}
