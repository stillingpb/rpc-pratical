package ipc;

import io.IntWritable;
import io.ObjectWritable;
import io.Writable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {
	private Object instance; // 执行方法调用的对象

	// private volatile boolean running = false;
	private boolean running = false;

	private Listener listener = null;
	ExecutorService readPool = null;
	private Responder responder = null;
	private Handler[] handlers = null;

	private int default_reader_num = 1;
	private int default_handler_num = 2;

	BlockingQueue<Call> callQueue = new LinkedBlockingQueue<Call>(); // 调用队列

	private String host = "127.0.0.1";
	private int port = 9001;

	public Server(Object instance, String host, int port) {
		this.running = true;
		this.host = host;
		this.port = port;
		this.instance = instance;
		try {
			listener = new Listener();
			responder = new Responder();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		responder.start();
		listener.start();
		listener.startReader();

		handlers = new Handler[default_handler_num];
		for (int i = 0; i < default_handler_num; i++) {
			handlers[i] = new Handler();
			handlers[i].start();
		}
	}

	public void close() {
		this.running = false;
	}

	static class Call implements Writable {
		private String methodName;
		private Writable[] parameter;

		private Connection attach; // 附带的一个connection对象

		/**
		 * 创建一个无参构造器，供反序列化用
		 */
		public Call() {
		}

		@Override
		public void write(DataOutput out) throws IOException {
			// ipc服务器端call对象，不需要将数据序列化输出
		}

		@Override
		public void readFields(DataInput in) throws IOException {
			this.methodName = in.readUTF();
			int paramNum = in.readInt();
			parameter = new Writable[paramNum];
			for (int i = 0; i < paramNum; i++) {
				String paramClassName = in.readUTF();
				Class<? extends Writable> clazz = null;
				Writable param = null;
				try {
					clazz = (Class<? extends Writable>) Class
							.forName(paramClassName);
					Constructor<? extends Writable> constructor = clazz
							.getDeclaredConstructor();
					param = constructor.newInstance();
				} catch (Exception e) {
					e.printStackTrace();
				}
				param.readFields(in);
				parameter[i] = param;
			}
		}

		public String getMethodName() {
			return methodName;
		}

		public void setMethodName(String methodName) {
			this.methodName = methodName;
		}

		public Writable[] getParameter() {
			return parameter;
		}

		public void setParameter(Writable[] parameter) {
			this.parameter = parameter;
		}

		public Connection getAttach() {
			return attach;
		}

		public void setAttach(Connection attach) {
			this.attach = attach;
		}
	}

	class Listener extends Thread {
		private ServerSocketChannel acceptChannel;
		private Selector selector;

		private Reader[] readers;
		private int currentReader = 0;

		public Listener() throws IOException {
			InetSocketAddress address = new InetSocketAddress(host, port);
			acceptChannel = ServerSocketChannel.open();
			acceptChannel.configureBlocking(false);
			ServerSocket socket = acceptChannel.socket();
			socket.bind(address);

			readers = new Reader[default_reader_num];
			for (int i = 0; i < default_reader_num; i++) {
				Selector readSelector = Selector.open();
				Reader reader = new Reader(readSelector);
				readers[i] = reader;
			}

			selector = Selector.open();
			acceptChannel.register(selector, SelectionKey.OP_ACCEPT);
			this.setName("IPC Server listen on port:" + port);
			this.setDaemon(true);
		}

		/**
		 * 创建获取数据的reader的线程池
		 */
		void startReader() {
			readPool = Executors.newFixedThreadPool(default_reader_num);
			for (Reader reader : readers)
				readPool.execute(reader);
		}

		public void run() {
			while (running) {
				try {
					selector.select();
					Iterator<SelectionKey> iter = selector.selectedKeys()
							.iterator();
					while (iter.hasNext()) {
						SelectionKey key = iter.next();
						iter.remove();
						if (key.isValid() && key.isAcceptable())
							doAccept(key);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

		/**
		 * 负责处理一个accept事件，当accept事件发生后，分配一个Reader对象来处理后续的读事件
		 * 
		 * @param key
		 * @throws IOException
		 */
		private void doAccept(SelectionKey key) throws IOException {
			ServerSocketChannel socketChannel = (ServerSocketChannel) key
					.channel();
			SocketChannel channel = null;
			int i = 0;
			while ((channel = socketChannel.accept()) != null) {
				channel.configureBlocking(false);
				channel.socket().setTcpNoDelay(true);

				Reader reader = getReader();
				try {
					reader.startAdd();
					SelectionKey readKey = reader.registerChannel(channel);
					Connection conn = new Connection(readKey, channel);
					readKey.attach(conn);
				} finally {
					reader.finishAdd();
				}
			}
		}

		private Reader getReader() {
			currentReader = (currentReader + 1) % readers.length;
			return readers[currentReader];
		}
	}

	class Reader extends Thread {
		Selector readSelector;
		private boolean adding;

		public Reader(Selector selector) {
			this.readSelector = selector;
			adding = false;
		}

		public void startAdd() {
			adding = true;
			readSelector.wakeup();
		}

		public void finishAdd() {
			adding = false;
			synchronized (this) {
				this.notify();
			}
		}

		public synchronized SelectionKey registerChannel(SocketChannel channel)
				throws ClosedChannelException {
			return channel.register(readSelector, SelectionKey.OP_READ);
		}

		public void run() {
			synchronized (this) {
				while (running) {
					try {
						readSelector.select();
						while (adding) {
							try {
								this.wait(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						Iterator<SelectionKey> iter = readSelector
								.selectedKeys().iterator();
						while (iter.hasNext()) {
							SelectionKey key = iter.next();
							iter.remove();
							if (key.isValid() && key.isReadable())
								doRead(key);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		private void doRead(SelectionKey key) {
			Connection c = (Connection) key.attachment();
			try {
				Call call = c.readCall();
				if (call != null) {
					call.setAttach(c); // 将connection对象附属到call对象中，供后续返回时使用
					callQueue.add(call);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	class Handler extends Thread {

		public void run() {
			while (running) {
				Call call = null;
				try {
					call = callQueue.take(); // 如果callQueue中没有数据，将会阻塞
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				String methodName = call.getMethodName();
				Writable[] parameter = call.getParameter();
				Class<? extends Writable> paramClass[] = new Class[parameter.length];
				for (int i = 0; i < parameter.length; i++) {
					Class<? extends Writable> clazz = parameter[i].getClass();
					paramClass[i] = clazz;
				}
				Writable result = null;
				try {
					Method method = instance.getClass().getDeclaredMethod(
							methodName, paramClass);
					method.setAccessible(true);
					result = (Writable) method.invoke(instance, parameter);
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

	/**
	 * 负责维护一次调用，读写需要的参数
	 * 
	 * @author pb
	 * 
	 */
	class Connection {
		SocketChannel channel;
		private SelectionKey readKey;

		private static final int SIZE_LENGTH = 4;
		private ByteBuffer data;
		private ByteBuffer dataLength;
		private boolean lengthReaded = false; // data length 是否已经被获取过
		private boolean dataReaded = false;// data 是否已经被获取过

		/*********************************/

		private SelectionKey writeKey;
		private ByteBuffer writeBuffer;

		/*********************************/

		public Connection(SelectionKey readKey, SocketChannel channel) {
			this.readKey = readKey;
			this.channel = channel;
			// 4字节大小的buffer，获取data的长度
			dataLength = ByteBuffer.allocate(SIZE_LENGTH);
		}

		/**
		 * 将调用结果设置到connection对象中
		 * 
		 * @param result
		 */
		public void setResult(Writable result) {
			ByteArrayOutputStream response = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(response);
			// 将结果序列化
			try {
				out.writeUTF(result.getClass().getName());
				result.write(out);
			} catch (IOException e) {
				e.printStackTrace();
			}
			byte[] res = response.toByteArray();
			writeBuffer = ByteBuffer.allocate(res.length);
			writeBuffer.put(res);
			writeBuffer.flip(); // 为读数据做准备
		}

		/**
		 * 从连接中读取数据
		 * 
		 * @return 如果一次调用没有读完数据，那么返回null，如果这次调用的数据读完了，返回一个call对象
		 * @throws IOException
		 */
		public Call readCall() throws IOException {
			if (!lengthReaded && dataLength.remaining() > 0) {
				channel.read(dataLength);
				return null; // 还未读取完call对象
			}
			if (data == null) {
				lengthReaded = true;
				dataLength.flip();
				data = ByteBuffer.allocate(dataLength.getInt());
			}
			if (!dataReaded && data.remaining() > 0)
				channel.read(data);

			if (!dataReaded && data.remaining() == 0) {
				dataReaded = true;
				data.flip();
				DataInput dis = new DataInputStream(new ByteArrayInputStream(
						data.array()));
				Call call = (Call) ObjectWritable.readObject(dis, Call.class);
				return call;
			}
			return null;
		}

		/**
		 * 将result写出到channel
		 * 
		 * @throws IOException
		 */
		public void writeResult() throws IOException {
			if (writeBuffer.remaining() > 0)
				channel.write(writeBuffer);
			if (writeBuffer.remaining() <= 0) // 如果写完了数据，将channel关闭
				channel.close();
		}
	}

	/**
	 * 负责将调用完成的数据输出出去
	 * 
	 * @author pb
	 * 
	 */
	class Responder extends Thread {
		private Selector writeSelector;
		private boolean adding = false;

		public Responder() {
			try {
				this.writeSelector = Selector.open();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void startAdd() {
			adding = true;
			writeSelector.wakeup();
		}

		public void finishAdd() {
			adding = false;
			synchronized (this) {
				this.notify();
			}
		}

		public synchronized SelectionKey registerChannel(SocketChannel channel)
				throws ClosedChannelException {
			return channel.register(writeSelector, SelectionKey.OP_WRITE);
		}

		public void run() {
			synchronized (this) {
				while (running) {
					try {
						writeSelector.select();
						while (adding) { // 如果在往selector中注册channel时，线程停一会
							try {
								this.wait(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						Iterator<SelectionKey> iter = writeSelector
								.selectedKeys().iterator();
						while (iter.hasNext()) {
							SelectionKey key = iter.next();
							iter.remove();
							if (key.isValid() && key.isWritable())
								doResponse(key);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		/**
		 * 将数据输出出去
		 * 
		 * @param key
		 */
		public void doResponse(SelectionKey key) {
			Connection conn = (Connection) key.attachment();
			try {
				conn.writeResult();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
