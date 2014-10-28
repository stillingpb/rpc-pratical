package rpc.ipc.client;

import java.util.concurrent.FutureTask;

import rpc.io.ExceptionWritable;
import rpc.io.NullWritable;
import rpc.io.Writable;
import rpc.ipc.util.RPCClientException;

public class ClientStub {
	public static Writable call(String methodName, Writable[] parameter, String host, int port)
			throws RPCClientException {
		Call call = new Call(methodName, parameter);
		FutureTask<Writable> callTask = new FutureTask<Writable>(new Connection(call, host, port));
		callTask.run();
		try {
			Writable result = callTask.get();
			return result;
		} catch (Exception e) {
			throw new RPCClientException("rpc调用异常", e);
		}
	}

}
