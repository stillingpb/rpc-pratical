package ipc;

import io.DataOutputBuffer;
import io.Writable;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Client {
	private static ExecutorService connectionPool;

	public static Writable call(String methodName, Writable[] parameter, URL url) {
		// 第一次远程方法调用时，创建连接池
		if (connectionPool == null)
			connectionPool = Executors.newCachedThreadPool();

		Call call = new Call(methodName, parameter);
		Future<Writable> future = connectionPool.submit(new Connection(call,
				url));
		try {
			return future.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 负责管理一次rpc，从发送数据，到获取数据的过程
	 * 
	 * @author pb
	 * 
	 */
	public static class Connection implements Callable<Writable> {
		private String ipcHost;
		private int ipcPort;
		private Call call;

		private Socket socket;
		private DataInput in;
		private DataOutput out;

		Connection(Call call, URL url) {
			this.call = call;
			ipcHost = url.getHost();
			ipcPort = url.getPort();
		}

		void createConnection() {
			try {
				socket = new Socket(ipcHost, ipcPort);
				in = new DataInputStream(socket.getInputStream());
				out = new DataOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				System.err.println("ipc连接异常");
				e.printStackTrace();
			}
		}

		void sendPackage() {
			DataOutputBuffer buffer = new DataOutputBuffer();
			try {
				call.write(buffer);
				int callLen = buffer.getLength();
				byte callData[] = buffer.getData();
				out.writeInt(callLen); // sizeof(int) == 4 byte
				out.write(callData);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * 获取调用结果
		 * 
		 * @return
		 */
		Writable getResult() {
			Writable instance = null;
			try {
				String className = in.readUTF();
				Class<? extends Writable> clazz = (Class<? extends Writable>) Class
						.forName(className);
				instance = clazz.getDeclaredConstructor().newInstance();
				instance.readFields(in);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return instance;
		}

		@Override
		public Writable call() throws Exception {
			createConnection();
			sendPackage();
			return getResult();
		}

	}

	/**
	 * 一次客户端调用，所用参数全部封装到Call对象中
	 * 
	 * @author pb
	 * 
	 */
	public static class Call implements Writable {

		private String methodName;
		private Writable[] parameter;

		public Call(String methodName, Writable[] parameter) {
			this.methodName = methodName;
			this.parameter = parameter;
		}

		@Override
		public void write(DataOutput out) throws IOException {
			out.writeUTF(methodName);
			out.writeInt(parameter.length);
			for (Writable para : parameter) {
				String paraClassName = para.getClass().getName();
				out.writeUTF(paraClassName);
				para.write(out);
			}
		}

		@Override
		public void readFields(DataInput in) throws IOException {
			// ipc客户端call对象，不需要从数据流中反序列化对象
		}
	}

	/**
	 * 关闭正在进行远程方法调用的线程
	 */
	public static void close() {
		connectionPool.shutdownNow();
	}
}
