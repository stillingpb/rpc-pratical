package rpc.ipc.server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {
	private ServerContext context;

	/**
	 * 控制服务器启停的变量
	 */
	static volatile boolean running = true;

	/**
	 * 调用队列
	 */
	private BlockingQueue<Call> callQueue;

	private static int DEFAULT_READER_NUM = 5;
	private static int DEFAULT_HANDLER_NUM = 5;

	private ExecutorService readPool;
	private ExecutorService handlerPool;

	private Listener listener = null;
	private Responder responder = null;
	private Reader[] readers;
	private Handler[] handlers = null;

	public Server(Object instance, String host, int port) {
		context = new ServerContext();
		context.setInstance(instance);
		context.setHost(host);
		context.setPort(port);

		callQueue = new LinkedBlockingQueue<Call>();
		context.setCallQueue(callQueue);

		responder = new Responder();
		context.setResponder(responder);

		readers = new Reader[DEFAULT_READER_NUM];
		for (int i = 0; i < DEFAULT_READER_NUM; i++) {
			readers[i] = new Reader(context);
		}
		context.setReaders(readers);

		listener = new Listener(context);

		handlers = new Handler[DEFAULT_HANDLER_NUM];
		for (int i = 0; i < DEFAULT_HANDLER_NUM; i++)
			handlers[i] = new Handler(context);
	}

	public void start() {
		responder.start();

		readPool = Executors.newFixedThreadPool(DEFAULT_READER_NUM);
		for (Reader reader : readers)
			readPool.execute(reader);

		listener.start();

		handlerPool = Executors.newFixedThreadPool(DEFAULT_HANDLER_NUM);
		for (Handler handler : handlers)
			handlerPool.execute(handler);
	}

	public void close() {
		running = false;
	}
}
