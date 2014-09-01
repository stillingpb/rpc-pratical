package rpc.ipc.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import rpc.io.Writable;
import rpc.ipc.util.RPCClientException;

public class Client {
	private static ExecutorService connectionPool = Executors.newFixedThreadPool(1);

	public static Writable call(String methodName, Writable[] parameter, String host, int port)
			throws RPCClientException {
		Call call = new Call(methodName, parameter);
		Future<Writable> feture = connectionPool.submit(new Connection(call, host, port));
		try {
			return feture.get();
		} catch (Exception e) {
			throw new RPCClientException("rpc调用异常", e);
		}
	}

	/**
	 * 关闭正在进行远程方法调用的线程
	 */
	public static void close() {
		connectionPool.shutdownNow();
	}
}
