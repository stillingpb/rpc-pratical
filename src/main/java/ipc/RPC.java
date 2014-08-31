package ipc;

import io.Writable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;

public class RPC {

	public static Server getServer(Object instance, String host, int port) {
		return new Server(instance, host, port);
	}

	/**
	 * 通过远程调用接口，和服务器url，创建远程访问对象
	 * 
	 * @param clazz
	 *            远程对象的借口类
	 * @param url
	 * @return
	 */
	public static <T> T getClientProxy(Class<T> clazz, URL url) {
		ClientInvocation handler = new ClientInvocation(url);
		return (T) Proxy.newProxyInstance(clazz.getClassLoader(),
				new Class[] { clazz }, handler);
	}

	public static class ClientInvocation implements InvocationHandler {
		private URL url;

		public ClientInvocation(URL url) {
			this.url = url;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			// 将object数组转换成writable数组
			Writable[] args2 = new Writable[args.length];
			for (int i = 0; i < args.length; i++)
				args2[i] = (Writable) args[i];

			// 调用远程方法
			Writable result = Client.call(method.getName(), args2, url);
			return result;
		}
	}

	public static void closeClientProxy() {
		Client.close();
	}

}
