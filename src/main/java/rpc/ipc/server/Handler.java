package rpc.ipc.server;

import java.lang.reflect.Method;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import rpc.io.Writable;

class Handler extends Thread {

	private BlockingQueue<Call> callQueue;
	private Object instance;
	private Responder responder;

	public Handler(ServerContext context) {
		this.instance = context.getInstance();
		this.callQueue = context.getCallQueue();
		this.responder = context.getResponder();
	}

	public void run() {
		while (ServerStub.running) {
			Call call = null;
			try {
				call = callQueue.take(); // 如果callQueue中没有数据，将会阻塞
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
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
			} catch (Exception e) {
				e.printStackTrace();
			}
			processResult(call.getAttach(), result);
		}
	}

	/**
	 * 将结果存储到connection对象中，并在channel上注册一个write事件
	 * 
	 * @param conn
	 * @param result
	 */
	private void processResult(Connection conn, Writable result) {
		try {
			responder.startAdd();
			conn.setResult(result);
			SelectionKey key = responder.registerChannel(conn.channel);
			key.attach(conn);
		} catch (ClosedChannelException e) {
			e.printStackTrace();
		} finally {
			responder.finishAdd();
		}
	}
}