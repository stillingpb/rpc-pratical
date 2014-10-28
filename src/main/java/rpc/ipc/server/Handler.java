package rpc.ipc.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.SelectionKey;
import java.util.concurrent.BlockingQueue;

import rpc.io.ExceptionWritable;
import rpc.io.NullWritable;
import rpc.io.Writable;
import rpc.ipc.util.RPCServerException;

class Handler extends Thread {

	private ServerContext context;
	private BlockingQueue<Call> callQueue;
	private Object instance;
	private Responder responder;

	public Handler(ServerContext context) {
		this.context = context;
		this.instance = context.getInstance();
		this.callQueue = context.getCallQueue();
		this.responder = context.getResponder();
	}

	public void run() {
		while (context.running) {
			try {
				Call call = callQueue.take(); // 如果callQueue中没有数据，将会阻塞
				Writable result = invokeMethod(call);
				processResult(call.getAttach(), result);
			} catch (Exception e) {
				RPCServerException serverException = new RPCServerException("handler 抛出异常", e);
				serverException.printStackTrace();
			}
		}
	}

	/**
	 * 执行方法调用
	 * 
	 * @param call
	 * @return result 调用结果
	 * @throws RPCServerException
	 */
	private Writable invokeMethod(Call call) throws RPCServerException {
		String methodName = call.getMethodName();
		Writable[] parameters = call.getParameters();
		Class<? extends Writable> paramClass[] = new Class[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			Class<? extends Writable> clazz = parameters[i].getClass();
			paramClass[i] = clazz;
		}
		Writable result = null;
		try {
			Method method = instance.getClass().getDeclaredMethod(methodName, paramClass);
			method.setAccessible(true);
			result = (Writable) method.invoke(instance, parameters);
			if (result == null) // 如果调用结果是null
				result = new NullWritable();
		} catch (InvocationTargetException e) { // 执行方法抛出异常，也就是方法返回的异常
			Throwable targetException = e.getTargetException();
			if (!(targetException instanceof ExceptionWritable)) {
				throw new RPCServerException(targetException.getClass().getName() + " 不是 "
						+ ExceptionWritable.class + " 的子类", e);
			}
			ExceptionWritable exception = (ExceptionWritable) targetException;
			result = exception;
		} catch (Exception e) {
			Connection conn = call.getAttach();
			conn.close();
			throw new RPCServerException(methodName + " 调用异常", e);
		}
		return result;
	}

	/**
	 * 将结果存储到connection对象中，并在channel上注册一个write事件
	 * 
	 * @param conn
	 * @param result
	 * @throws RPCServerException
	 */
	private void processResult(Connection conn, Writable result) throws RPCServerException {
		SelectionKey key = null;
		try {
			responder.startAdd();
			conn.setResult(result);
			key = responder.registerChannel(conn.channel);
			conn.setWriteSelectionKey(key);
			key.attach(conn);
		} catch (RPCServerException e) { // 关闭channel，注册之前注册的read事件
			if (key != null)
				key.cancel();
			conn.close();
			throw e;
		} finally {
			responder.finishAdd();
		}
	}
}